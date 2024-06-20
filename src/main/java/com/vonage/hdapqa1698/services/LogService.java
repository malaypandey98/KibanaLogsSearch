package com.vonage.hdapqa1698.services;

import com.vonage.hdapqa1698.helpers.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LogService {

    public String getUsernameFromLog(String logMessage) {
        if(logMessage.contains(Constants.SEARCH_PARAM)){
            return logMessage.split(Constants.SEARCH_PARAM)[1];
        }
        log.info("The log {} does not contain search param \"{}\"", logMessage, Constants.SEARCH_PARAM);
        return "";
    }
}
