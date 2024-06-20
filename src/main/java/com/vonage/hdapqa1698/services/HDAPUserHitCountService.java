package com.vonage.hdapqa1698.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.vonage.hdapqa1698.helpers.Constants;
import com.vonage.hdapqa1698.pojo.HDAPUserData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@Slf4j
public class HDAPUserHitCountService {

    private final FileService fileService;

    public HDAPUserHitCountService(FileService fileService) {
        this.fileService = fileService;
    }

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
}
