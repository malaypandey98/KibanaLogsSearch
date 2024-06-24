package com.vonage.hdapqa1698.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.vonage.hdapqa1698.helpers.Constants;
import com.vonage.hdapqa1698.pojo.HDAPUserData;
import com.vonage.hdapqa1698.pojo.KibanaResponse;
import com.vonage.hdapqa1698.pojo.NestedHits;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class HDAPUserHitCountService {

    private final FileService fileService;

    private final RestTemplate restTemplate;

    public List<HDAPUserData> getAllLogs(){
        try {
            File file = new File(Objects.requireNonNull(getClass().getResource(Constants.HDAP_USER_HIT_LOGS)).getFile());
            List<HDAPUserData> loginData = Constants.MAPPER.readValue(file, new TypeReference<List<HDAPUserData>>() {
            });
            log.info("Found {}/{} user login data.", loginData.size(), Constants.MAPPER.readValue(file, new TypeReference<List<Object>>() {}).size());
            return loginData;
        } catch (IOException e) {
            log.error("Failed to read log file", e);
        }
        return new ArrayList<>();
    }

    public void getUserLogHits() throws IOException {
        List<HDAPUserData> hdpUserData = getAllLogs();
        String usernamesFromFile = fileService.readFrom(new File(Objects.requireNonNull(Objects.requireNonNull(getClass().getResource(Constants.USERNAME_SET)).getFile())));
        List<String> usernames = Arrays.asList(
                usernamesFromFile.split("\n")
        );
        Map<String, Integer> usernameHitFrequency = new HashMap<>();
        usernames.stream().parallel().map(username -> usernameHitFrequency.put(username, getHitCounts(hdpUserData, username))).collect(Collectors.toList());
        StringBuilder csvBuilder = new StringBuilder();
        csvBuilder.append("USERNAME, HITS\n");
        usernameHitFrequency.entrySet().stream().map(entry -> csvBuilder.append(entry.getKey()).append(", ").append(entry.getValue()).append("\n")).collect(Collectors.joining("\n"));
        fileService.writeResult(new File(Paths.get("").toAbsolutePath() + Constants.USER_HIT_RESULT), true, csvBuilder.toString());
    }

    private Integer getHitCounts(List<HDAPUserData> hdapUserData, String searchParam){
        AtomicReference<Integer> count = new AtomicReference<>(0);
        hdapUserData.forEach(data -> {
            if(data.getLogMessage().contains(searchParam)) count.getAndSet(count.get() + 1);
        });
        return count.get();
    }

    public void getUserLogHitsV2() throws IOException {
        List<String> usernames = Arrays.asList(fileService.readFrom(new File(Objects.requireNonNull(Objects.requireNonNull(getClass().getResource(Constants.USERNAME_SET)).getFile()))).split("\n"));
        StringBuilder csvBuilder = new StringBuilder();
        csvBuilder.append("USERNAME, HITS\n");
        Map<String, Integer> usernameHitFrequency = new ConcurrentHashMap<>();
        usernames.stream().parallel().map(username ->
                {
                    KibanaResponse kibanaResponse = callKibanaAPI(Constants.PLTFRM_LOG_REQUEST, username, 0);
                    return usernameHitFrequency.put(username, Objects.nonNull(kibanaResponse) ? kibanaResponse.getRawResponse().getHits().getTotal() : -1);
                }
        ).collect(Collectors.toList());
        log.info("\n\n {} \n\n", usernameHitFrequency);
        usernameHitFrequency.entrySet().forEach(entry -> csvBuilder.append(entry.getKey()).append(", ").append(entry.getValue()).append("\n"));
        fileService.writeResult(new File(Paths.get("").toAbsolutePath() + Constants.USER_HIT_RESULT), true, csvBuilder.toString());
    }

    public void getMostAPIsCalled() throws IOException {
        List<String> usernames = Arrays.asList(fileService.readFrom(new File(Objects.requireNonNull(Objects.requireNonNull(getClass().getResource(Constants.USERNAME_SET)).getFile()))).split("\n"));
        Map<String, Set<String>> uniqueAPIs = new ConcurrentHashMap<>();
        usernames.stream().parallel().map(username ->
                {
                    KibanaResponse kibanaResponse = callKibanaAPI(Constants.PLTFRM_LOG_REQUEST, username, 0);
                    try {
                        return uniqueAPIs.put(username, getSetOfUniqueAPIForKibanaResponse(kibanaResponse));
                    } catch (JsonProcessingException e) {
                        log.error("Unable to fetch uniques apis for {}, ERROR: {}", username, e.getMessage());
                    }
                    return null;
                }
        ).collect(Collectors.toList());
        StringBuilder csvBuilder = new StringBuilder();
        csvBuilder.append("USERNAME, HITS\n");
        uniqueAPIs.entrySet().stream().map(entry -> csvBuilder.append(entry.getKey()).append(", ").append(entry.getValue().toString().replace(",", ";")).append("\n")).collect(Collectors.joining("\n"));
        fileService.writeResult(new File(Paths.get("").toAbsolutePath() + Constants.UNIQUE_APIS), true, csvBuilder.toString());

    }

    public Set<String> getSetOfUniqueAPIForKibanaResponse(KibanaResponse kibanaResponse) throws JsonProcessingException {
        List<NestedHits> hits = kibanaResponse.getRawResponse().getHits().getHits();
        Set<String> uniqueAPIs = new HashSet<>();
        for(NestedHits hit : hits) {
            String message = hit.getSource().getMessage();
            if(message.contains(Constants.REQUEST_LOG_FILTER_CLASS)){
                Map<String, Object> loggedResponse = Constants.MAPPER.readValue(
                        message.substring(message.indexOf(Constants.REQUEST_LOG_FILTER_CLASS) + Constants.REQUEST_LOG_FILTER_CLASS.length()),
                        new TypeReference<Map<String, Object>>() {}
                );
                uniqueAPIs.add(loggedResponse.get("uri").toString());
            }
            else if(message.contains(Constants.LOG_SERVICE_CLASS)){
                String[] split = message.split("\t");
                for(String component : split){
                    if(component.startsWith("/")){
                        uniqueAPIs.add(component);
                        break;
                    }
                }
            }
        }
        return uniqueAPIs;
    }

    public String prepareUserSearchParam() throws IOException {
        String usernamesFromFile = fileService.readFrom(new File(Objects.requireNonNull(Objects.requireNonNull(getClass().getResource(Constants.USERNAME_SET)).getFile())));
        StringBuilder orSeparatedBuilder = new StringBuilder();
        Arrays.asList(usernamesFromFile.split("\n")).forEach(username -> orSeparatedBuilder.append("\"").append(username).append("\"").append(" or "));
        return orSeparatedBuilder.toString();
    }

    public KibanaResponse callKibanaAPI(String payload, String searchParam, Integer pageNumber) {
        log.info("Fetching log for {}", searchParam);
        HttpHeaders headers = new HttpHeaders();
        headers.add("osd-version", "2.11.0");
        headers.add("osd-xsrf", "osd-fetch");
        headers.add("cookie", "security_authentication_saml1=Fe26.2**5da77b5fdc05351471701e917691f92ea071142e14e0ab92d8da46657a7135ce*aztyF8Svw-eYHZaEzoeK6g*uS8lJimDP7Qxay5cG5mK2UsJ_PVhxbmPULfHP2VOpHFqGKbuPmyEtm3ZWm8UYTV6_OrHJk9OlZKQtVnphWmIJPJwrsbI_9W_AvP_XRGsUCsaudqh8xHTs_U2AGVOarY7Jp7CDpWDb7qIYE_BeJ9c-mLugb0rFiCbqZu4kA_ebSM3M88cXXeW8HopZhEG5ggN1pVNkGWLuRKtt7elPI8W3NVbDMFvQ4kCQz5Gs_SE5ekIy8GiM4m_d8eGFQrBDZctSQ1r2V7MXKIHEH1zpFmNuKQHJedxSp9yrbdtz2PM-sXmJd0J6ganYlOrTgWiPY9RDDrtVFG3IDqUM2twAJzgVKHJjRAxFKd9Pn-y7eCG6l3kKZ5XD0xyXviXChAv4licJ-5BIQ0ooWCqWQduK3nnklmtO2Hi24K2aXAKf8aVLX2PIbsJHYaBqGWJYEpGxHiEmQQMFHBPUqkn9aQQJHVyqfaIb_gDS9hl9OP5928yvG_Tq9Sj3ZkvbSDVABKXSaNhzSX3oYyITgTp1OFh_b6Ducxjvx9n7LWWcIxb07Q4y4M**6f2ceb653f71d563e04291d3630bc2fbd47151d72682f9446a04ba6281c87776*Yme1vROfPhCOJwf94iGf5-cFzL8fhDpqnF3J7lU5khg; security_authentication=Fe26.2**31e5ab1256b312214b89690a147499008df800f0534c9b832ef2c89b8ff9cad0*oRkNmKTFDbjcowyTh5mt9g*zZiCUjPDzhnIKcLufvWC1Sagv_3gUpOA96oItXNt86QYDeFVcyP2dfgQ2Uj-VRmJVFNTZ7MlZhLDt8jNCP2L3bcrLdEo7Bi0TyRs5a0AakwpDW9c6FhR_W9QhgYcbXZueZtUFQuD5neCfWqmoDs891FmARYXjBK-Z--ot7ro_QqkblEUYMA1VcEl9H7TTW14**2be4e56155ca4102547895ecacde10502a629419b304e1ab7fc05236b082c58a*GXikvP7_N6Dis_GaOG95KtU-40kCnoovL411j4wdYyw; s_ecid=MCMID%7C88637971609207160601510393130151969564; _gcl_au=1.1.1671583636.1713420500; cjConsent=MHxOfDB8Tnww; _fbp=fb.1.1713420501237.1770913534; _hjSessionUser_2882478=eyJpZCI6IjIyZmY4YzJiLWQ2MjEtNTI5ZC1hZGMwLTBmOTBjMjMzNTQ1ZSIsImNyZWF0ZWQiOjE3MTM0MjA1MDE0MzMsImV4aXN0aW5nIjp0cnVlfQ==; OptanonAlertBoxClosed=2024-04-18T06:08:23.388Z; FPID=3856e46c-e68a-4383-816c-773be80ae5d7; OptanonConsent=isGpcEnabled=0&datestamp=Mon+Jun+03+2024+11%3A58%3A57+GMT%2B0530+(India+Standard+Time)&version=202404.1.0&browserGpcFlag=0&isIABGlobal=false&hosts=&landingPath=NotLandingPage&groups=C0004%3A1%2CC0002%3A1%2CC0003%3A1%2CC0001%3A1%2CC0010%3A1&geolocation=IN%3BKA&AwaitingReconsent=false; dtm_token_sc=AQADJKC_6z5FGwFKUK69AQA_0wABAQCO7UZa5AEBAI7tRlrk; dtm_token=AQADJKC_6z5FGwFKUK69AQA_0wABAQCO7UZa5AEBAI7tRlrk; cjLiveRampLastCall=2024-06-14T09:00:52.045Z; cf_clearance=661yj8gOGlw8I3OXIZh0bXEg9jtGXWzxD8qNYFkM_o4-1718605516-1.0.1.1-JTAQrdbUJbh1bq1yejal.N4LcThdws7s6eZITkac3B8Lmi2.YHWpx95rhx8GobRPdOrexjg_phR6AjLe3vi9eA; mbox=PC#cad6617529ea41d6bddbe428da8d6496.34_0#1781850324|session#44bfe9a95ebf43b698908e040c584ed4#1718607384; _uetvid=0ebdbaf0fd4a11eea73ffd4ace3b6e90; AMCV_null%40AdobeOrg=-1124106680%7CMCMID%7C88637971609207160601510393130151969564%7CMCIDTS%7C19892%7CMCCIDH%7C-1720305254%7CMCOPTOUT-1718612725s%7CNONE%7CvVersion%7C5.2.0; _rdt_uuid=1713420499245.38d26b82-8606-4ffd-80af-c37ad1103531; AMCVS_A8833BC75245AF9E0A490D4D%40AdobeOrg=1; s_cc=true; _ga_EXYSW53ZZK=GS1.1.1718796495.24.1.1718796495.60.0.0; s_nr=1718844978937-Repeat; s_sq=%5B%5BB%5D%5D; _ga_8P345284G5=GS1.1.1718844980.11.0.1718844980.60.0.0; datadome=Eo5cSPsETNGemxarFs_amDnerA2DfRqccAeod5_uBiuYKGwLqVywAUh45ROLxy~Ti_PwsTV3_qaYrKlaK9dfgR3GszS3Bak3z6Th80qR8sk0XcorglOukQCjw2sElX3g; amp_f477e8=Fayd1shexAVpTRD1h_BO6-...1i0q080du.1i0q1btvj.5b.35.8g; _ga=GA1.1.272996441.1713416589; _ga_2FRRGV82Y0=GS1.1.1718953620.36.0.1718953624.0.0.0; amplitude_id_80d4ab359b88b06abffba0ea237ef107vonage.com=eyJkZXZpY2VJZCI6IjJkZTFiZDY0LTgxZDctNDA1OC1hZmUyLTE0ZmI2ZWFlMGQ2ZFIiLCJ1c2VySWQiOiJfbXBhbmRleSIsIm9wdE91dCI6ZmFsc2UsInNlc3Npb25JZCI6MTcxODk2NzM5MzU0MCwibGFzdEV2ZW50VGltZSI6MTcxODk2NzM5MzU1MSwiZXZlbnRJZCI6NjU1LCJpZGVudGlmeUlkIjoyMDYsInNlcXVlbmNlTnVtYmVyIjo4NjF9; AMCV_A8833BC75245AF9E0A490D4D%40AdobeOrg=179643557%7CMCIDTS%7C19895%7CMCMID%7C88637971609207160601510393130151969564%7CMCAAMLH-1719572194%7C12%7CMCAAMB-1719572194%7CRKhpRz8krg2tLO6pguXWp5olkAcUniQYPHaMWWgdJ3xzPWQmdj0y%7CMCOPTOUT-1718974594s%7CNONE%7CMCAID%7CNONE%7CvVersion%7C5.5.0; s_nr365=1718967394871-Repeat");
        try{
            String responseStr = restTemplate.exchange(
                    "https://virginia-cl.prod.logs.vonage.com/_dashboards/internal/search/opensearch",
                    HttpMethod.POST,
                    new HttpEntity<String>(payload
                            .replace("{search_param}", searchParam)
                            .replace("{page_number}", String.valueOf(pageNumber)), headers),
                    String.class
            ).getBody();
            KibanaResponse kibanaResponse = Constants.MAPPER.readValue(responseStr, KibanaResponse.class);
            return kibanaResponse;

        }catch (Exception e){
            log.info("Unable to fetch logs for {}, ERROR: {}", searchParam, e.getMessage());
        }
        return new KibanaResponse();
    }
}
