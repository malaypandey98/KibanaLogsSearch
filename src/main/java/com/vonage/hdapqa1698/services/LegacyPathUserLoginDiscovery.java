package com.vonage.hdapqa1698.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.sun.org.apache.xpath.internal.operations.Or;
import com.vonage.hdapqa1698.helpers.Constants;
import com.vonage.hdapqa1698.helpers.Order;
import com.vonage.hdapqa1698.pojo.IcebergUserLoginData;
import com.vonage.hdapqa1698.pojo.KibanaResponse;
import com.vonage.hdapqa1698.pojo.NestedHits;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.jni.User;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
@AllArgsConstructor
public class LegacyPathUserLoginDiscovery {

    private final RestTemplate restTemplate;

    private final FileService fileService;

    private List<IcebergUserLoginData> getAllLogs(){
        try {
            File file = new File(Objects.requireNonNull(getClass().getResource(Constants.ICEBERG_USER_LEGACY_LOGIN_LOGS)).getFile());
            List<IcebergUserLoginData> loginData = Constants.MAPPER.readValue(file, new TypeReference<List<IcebergUserLoginData>>() {
            });
            log.info("Found {}/{} user login data.", loginData.size(), Constants.MAPPER.readValue(file, new TypeReference<List<Object>>() {}).size());
            return loginData;
        } catch (IOException e) {
            log.error("Failed to read log file", e);
        }
        return new ArrayList<>();
    }

    public Map<String, Long> getUserLoginFrequency(){
        List<IcebergUserLoginData> loginData = getAllLogs();
        Map<String, Long> frequency = new HashMap<>();
        loginData.forEach(userLoginData -> frequency.put(userLoginData.getUsername(), frequency.getOrDefault(userLoginData.getUsername(), 0L) + 1));
        return frequency;
    }

//    public void getNumberOfInvocationPerUsername(Order order) throws JsonProcessingException {
//        ExecutorService executorService = Executors.newFixedThreadPool(10);
//        Map<String, Long> usernameHitFrequency = new HashMap<>();
//        String searchParam = "regular login request have succeed for user";
//        int totalRecords = 0;
//        KibanaResponse firstResponse = null;
//        try{
//            firstResponse = callKibanaAPI(Constants.CERBERUS_LOG_REQUEST.replace("{order_type}", order.getType()), searchParam, 0, null);
//            totalRecords = firstResponse.getRawResponse().getHits().getTotal();
//        } catch (Exception e){
//            log.error("Unable to fetch response from Kibana API");
//            return;
//        }
//        if(totalRecords == 0){
//            log.info("No record found for search param {}.", searchParam);
//            return;
//        }
//        log.info("Total record found for search param {}.", totalRecords);
//        Map<String, Object> payload = Constants.MAPPER.readValue(Constants.CERBERUS_LOG_REQUEST.replace("{order_type}", order.getType()), new TypeReference<Map<String, Object>>() {});
//        List<NestedHits> hits = firstResponse.getRawResponse().getHits().getHits();
//        int crawledRecords = hits.size();
//        List<Object> lastSort = hits.get(crawledRecords-1).getSort();
//        while(crawledRecords <= totalRecords){
//            log.info("Duration of logs, {} - {}", hits.get(0).getSource().getTimestamp(), hits.get(hits.size()-1).getSource().getTimestamp());
//            log.info("Log ids, {} - {}", hits.get(0).getId(), hits.get(hits.size()-1).getId());
//            log.info("Crawling {}/{} records.", crawledRecords, totalRecords);
//            for(NestedHits hit : firstResponse.getRawResponse().getHits().getHits()){
//                String message = hit.getSource().getMessage();
//                String username = message.substring(message.indexOf(searchParam) + searchParam.length()).trim();
//                usernameHitFrequency.put(username, usernameHitFrequency.getOrDefault(username, 0L) + 1);
//            }
//            String mapToJson = Constants.MAPPER.writeValueAsString(usernameHitFrequency);
//            executorService.submit(() -> write(mapToJson));
//            usernameHitFrequency.clear();
//            ((Map<String, Object>)((Map<String, Object>) payload.get("params")).get("body")).put("search_after", lastSort);
//            KibanaResponse nextPageResponse = null;
//            log.info("Getting next page...");
//            while (nextPageResponse == null){
//                nextPageResponse = callKibanaAPI(Constants.MAPPER.writeValueAsString(payload), searchParam, 0);
//            }
//            int responseSize = nextPageResponse.getRawResponse().getHits().getHits().size();
//            crawledRecords += responseSize;
//            lastSort = nextPageResponse.getRawResponse().getHits().getHits().get(responseSize-1).getSort();
//            hits = nextPageResponse.getRawResponse().getHits().getHits();
//        }
//    }

    public void getNumberOfInvocationPerUsername() {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        String searchParam = "regular login request have succeed for user";
        LocalDateTime end = LocalDateTime.parse("2024-06-19T13:34:25.448");
        LocalDateTime start = end.minusDays(20L);
        do{
            LocalDateTime finalEnd = end;
            executorService.submit(() -> getAndProcessRecords(finalEnd.minusHours(1L), finalEnd, searchParam));
//            getAndProcessRecords(finalEnd.minusHours(1L), finalEnd, searchParam);
            end = end.minusHours(1L);
        }
        while(!start.equals(end));
    }

    public void getAndProcessRecords(LocalDateTime start, LocalDateTime end, String searchParam){
        try{
            List<Object> sort = null;
            int recordsFound = 0, totalRecords = -1;
            do{
                Map<String, Long> usernameHitFrequency = new HashMap<>();
                KibanaResponse kibanaResponse = null;
                log.info("Searching logs between {} - {}...", start, end);
                while (kibanaResponse == null){
                    kibanaResponse = callKibanaAPI(
                            searchParam,
                            0,
                            start,
                            end,
                            Order.DESC,
                            sort
                    );
                }
                List<NestedHits> hits = kibanaResponse.getRawResponse().getHits().getHits();
                if(hits.isEmpty()){
                    return;
                }
                if(totalRecords == -1) totalRecords = kibanaResponse.getRawResponse().getHits().getTotal();
                recordsFound += hits.size();
                sort = hits.get(hits.size()-1).getSort();
                NestedHits firstHit = hits.get(0);
                NestedHits lastHit = hits.get(hits.size()-1);
                log.info("Duration of logs, {} - {}", firstHit.getSource().getTimestamp(), lastHit.getSource().getTimestamp());
                log.info("Log ids, {} - {}", firstHit.getId(), lastHit.getId());
                log.info("Log size: {}/{}", recordsFound, totalRecords);
                for(NestedHits hit : kibanaResponse.getRawResponse().getHits().getHits()){
                    String message = hit.getSource().getMessage();
                    String username = message.substring(message.indexOf(searchParam) + searchParam.length()).trim();
                    usernameHitFrequency.put(username, usernameHitFrequency.getOrDefault(username, 0L) + 1);
                }
                write(Constants.MAPPER.writeValueAsString(usernameHitFrequency));
            }while (recordsFound < totalRecords);
        }catch (Exception e){
            log.error("Failed to get user login frequency. ERROR: {}", e.getMessage());
        }
    }

    private synchronized void write(String mapToJson) {
        try {
            fileService.writeResult( new File(Paths.get("").toAbsolutePath() + Constants.UNIQUE_USERNAMES_FILE_PATH), false, mapToJson + ",\n");
        } catch (IOException e) {
            log.error("Failed to write unique usernames file", e);
            log.info("Loging user hit frequency so that the data is not lost. \n ------------------ \n {} ------------------ \n", mapToJson);
        }
    }

    public void getUniqueUsernames(){
        Map<String, Long> frequency = getUserLoginFrequency();
        StringBuilder txtBuilder =  new StringBuilder().append("USERNAME").append("\n");
        frequency.entrySet().forEach(entry -> txtBuilder.append("'" + entry.getKey().trim() + "',").append("\n"));
        try {
            fileService.writeResult( new File(Paths.get("").toAbsolutePath() + Constants.UNIQUE_USERNAMES_FILE_PATH), true, txtBuilder.toString());
        } catch (IOException e) {
            log.error("Failed to write unique user login data", e);
        }
    }

    @SuppressWarnings("unchecked")
    public KibanaResponse callKibanaAPI(String searchParam, Integer pageNumber, LocalDateTime start, LocalDateTime end, Order order, List<Object> sort) throws IOException {
        log.info("Fetching log for \"{}\"", searchParam);
        HttpHeaders headers = new HttpHeaders();
        headers.add("osd-version", "2.11.0");
        headers.add("osd-xsrf", "osd-fetch");
        headers.add("cookie", "security_authentication_saml1=Fe26.2**7fd10a50d3f8b438834d48ef3cbbfcedbb6da00fe35d7606b3ded06ac4365d06*RjW53J9r4S0zKDeXiYs9qQ*69KvYz4hoghGb5gfDN63xRAJSO1DLpzI2Gs6CFdJSY6BP0b5oX9ekd8BfPmMF_xiE71lViWjObThdQhVFEf8znLib8Tby1U_1TM8A0mCiROk5i7slT28ZH4e1isQWzs-3085Fk9gHhcrqKNaTArnn4ol-imbKSfFz2bSFChtPAy4WwzxnKkcpF4F43ta5n8i0P3phwZ8dVLzIXKtJJ420721BRp1yI612ik8EwPbCZbBtOwrl5LvcHugJEqMoSB33zf8MYg8bjz189UeYpZB1MWKR8Q5-VJO0FlF_cD4fzDcjgasYeWMH8nGVO_R231ggT9w4xzIdgsYe277OH9kjBDR4GEXDWcsuEiESgdW2u7Rf7bY2TRWyZ1s9xPb0aW_CzPhKwH-uuvtj8cuh0efhuRK09asXb7LsVPS6Zyy5XG04TmVS-UTd0iSAF-AXPDOZPBsIeObOsZY6qGaFdQBJ74wFp4C-Rv1OBO6hItjjiwZNvFR464OtPiG1GU1oc1awjpbPbqtrEDYbuy85Oo0Uue5iZSSMyx-X6-cpX99T3MfFvHuCYv44GQ6TtPiKAwd**a523e13608b4951fd029475a24e4da6bb3da48c93c4c5c37ecc574a4cd60c199*7L39QjIv08lYZN8p3EL4-6mIR9eP8CkucyeO7tZq0LA; security_authentication=Fe26.2**9cf4cae9408e9ca78ead67f151c991b0d789a663bb3ca0a4ea59c5cd5464602c*_vsSgnhTG2xR9-Dr9maWVA*URpVQ8_MbcSaAuiSfoCJrMWT-0O3YRlpYTczhU-zoXYmum6mJOLAWaXoWLYprpvvNuUbWxESJaPiEJv3lSTZVNdwG5pvbZrRIEVgvofvRqGax5nLBiZtQPo6-21oBWfwR95djQ-CbuP4JvszQBS0geFUUZuOtzIcRs3SF8dc-RRxobA0q-BHSgWZ81Cp9Rhl**32fdc92e81c9fc49ae3db961498c99b6bfb414ec49ad262381442326fede8d0b*wBywYrxmUGoRz1gCvSHNnIK0-E_pB7jl7CGoRCqlYdA; s_ecid=MCMID%7C88637971609207160601510393130151969564; _gcl_au=1.1.1671583636.1713420500; cjConsent=MHxOfDB8Tnww; _fbp=fb.1.1713420501237.1770913534; _hjSessionUser_2882478=eyJpZCI6IjIyZmY4YzJiLWQ2MjEtNTI5ZC1hZGMwLTBmOTBjMjMzNTQ1ZSIsImNyZWF0ZWQiOjE3MTM0MjA1MDE0MzMsImV4aXN0aW5nIjp0cnVlfQ==; OptanonAlertBoxClosed=2024-04-18T06:08:23.388Z; FPID=3856e46c-e68a-4383-816c-773be80ae5d7; OptanonConsent=isGpcEnabled=0&datestamp=Mon+Jun+03+2024+11%3A58%3A57+GMT%2B0530+(India+Standard+Time)&version=202404.1.0&browserGpcFlag=0&isIABGlobal=false&hosts=&landingPath=NotLandingPage&groups=C0004%3A1%2CC0002%3A1%2CC0003%3A1%2CC0001%3A1%2CC0010%3A1&geolocation=IN%3BKA&AwaitingReconsent=false; dtm_token_sc=AQADJKC_6z5FGwFKUK69AQA_0wABAQCO7UZa5AEBAI7tRlrk; dtm_token=AQADJKC_6z5FGwFKUK69AQA_0wABAQCO7UZa5AEBAI7tRlrk; cjLiveRampLastCall=2024-06-14T09:00:52.045Z; cf_clearance=661yj8gOGlw8I3OXIZh0bXEg9jtGXWzxD8qNYFkM_o4-1718605516-1.0.1.1-JTAQrdbUJbh1bq1yejal.N4LcThdws7s6eZITkac3B8Lmi2.YHWpx95rhx8GobRPdOrexjg_phR6AjLe3vi9eA; mbox=PC#cad6617529ea41d6bddbe428da8d6496.34_0#1781850324|session#44bfe9a95ebf43b698908e040c584ed4#1718607384; _uetvid=0ebdbaf0fd4a11eea73ffd4ace3b6e90; AMCV_null%40AdobeOrg=-1124106680%7CMCMID%7C88637971609207160601510393130151969564%7CMCIDTS%7C19892%7CMCCIDH%7C-1720305254%7CMCOPTOUT-1718612725s%7CNONE%7CvVersion%7C5.2.0; _rdt_uuid=1713420499245.38d26b82-8606-4ffd-80af-c37ad1103531; AMCVS_A8833BC75245AF9E0A490D4D%40AdobeOrg=1; s_cc=true; _ga_EXYSW53ZZK=GS1.1.1718796495.24.1.1718796495.60.0.0; s_nr=1718844978937-Repeat; s_sq=%5B%5BB%5D%5D; _ga_8P345284G5=GS1.1.1718844980.11.0.1718844980.60.0.0; datadome=Eo5cSPsETNGemxarFs_amDnerA2DfRqccAeod5_uBiuYKGwLqVywAUh45ROLxy~Ti_PwsTV3_qaYrKlaK9dfgR3GszS3Bak3z6Th80qR8sk0XcorglOukQCjw2sElX3g; amp_f477e8=Fayd1shexAVpTRD1h_BO6-...1i0q080du.1i0q1btvj.5b.35.8g; _ga=GA1.1.272996441.1713416589; _ga_2FRRGV82Y0=GS1.1.1718953620.36.0.1718953624.0.0.0; AMCV_A8833BC75245AF9E0A490D4D%40AdobeOrg=179643557%7CMCIDTS%7C19895%7CMCMID%7C88637971609207160601510393130151969564%7CMCAAMLH-1719572194%7C12%7CMCAAMB-1719572194%7CRKhpRz8krg2tLO6pguXWp5olkAcUniQYPHaMWWgdJ3xzPWQmdj0y%7CMCOPTOUT-1718974594s%7CNONE%7CMCAID%7CNONE%7CvVersion%7C5.5.0; s_nr365=1719139338581-Repeat; amplitude_id_80d4ab359b88b06abffba0ea237ef107vonage.com=eyJkZXZpY2VJZCI6IjJkZTFiZDY0LTgxZDctNDA1OC1hZmUyLTE0ZmI2ZWFlMGQ2ZFIiLCJ1c2VySWQiOiJfbXBhbmRleSIsIm9wdE91dCI6ZmFsc2UsInNlc3Npb25JZCI6MTcxOTEzODc0NjUzMiwibGFzdEV2ZW50VGltZSI6MTcxOTEzOTMzOTI0NywiZXZlbnRJZCI6NjYzLCJpZGVudGlmeUlkIjoyMDcsInNlcXVlbmNlTnVtYmVyIjo4NzB9");
        try{
            String payload = Constants.CERBERUS_LOG_REQUEST
                    .replace("{order_type}", Order.DESC.getType())
                    .replace("{gte}", start.toString())
                    .replace("{lte}", end.toString())
                    .replace("{search_param}", searchParam)
                    .replace("{page_number}", String.valueOf(pageNumber));
            if(sort != null && !sort.isEmpty()){
                Map<String, Object> payloadMap = Constants.MAPPER.readValue(payload, new TypeReference<Map<String, Object>>() {});
                ((Map<String, Object>)((Map<String, Object>) payloadMap.get("params")).get("body")).put("search_after", sort);
                payload = Constants.MAPPER.writeValueAsString(payloadMap);
            }
            String responseStr = restTemplate.exchange(
                    "https://virginia-cl.prod.logs.vonage.com/_dashboards/internal/search/opensearch",
                    HttpMethod.POST,
                    new HttpEntity<String>(payload, headers),
                    String.class
            ).getBody();
            KibanaResponse kibanaResponse = Constants.MAPPER.readValue(responseStr, KibanaResponse.class);
            return kibanaResponse;

        }catch (Exception e){
            fileService.writeResult(new File(Paths.get("").toAbsolutePath() + "app.log"), false, "Error while getting response from kibana. Error: " + e.getMessage() + ",\n");
        }
        return null;
    }
}
