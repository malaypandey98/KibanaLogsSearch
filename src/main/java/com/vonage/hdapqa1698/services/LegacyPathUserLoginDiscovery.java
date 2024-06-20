package com.vonage.hdapqa1698.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.vonage.hdapqa1698.helpers.Constants;
import com.vonage.hdapqa1698.pojo.IcebergUserLoginData;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

@Service
@Slf4j
@AllArgsConstructor
public class LegacyPathUserLoginDiscovery {

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

    public void getNumberOfInvocationPerUsername(){
        Map<String, Long> frequency = getUserLoginFrequency();
        StringBuilder csvBuilder =  new StringBuilder().append("USERNAME, INVOCATION").append("\n");
        frequency.entrySet().forEach(entry -> csvBuilder
                .append(entry.getKey())
                .append(",")
                .append(entry.getValue())
                .append("\n"));
        try {
            fileService.writeResult(new File(Paths.get("").toAbsolutePath() + Constants.UNIQUE_USER_LOGIN_DATA_FILE_PATH), true, csvBuilder.toString());
        } catch (IOException e) {
            log.error("Failed to write unique user login data", e);
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
}
