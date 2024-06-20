package com.vonage.hdapqa1698.helpers;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.format.DateTimeFormatter;

public final class Constants {

    private Constants(){}

    public static final ObjectMapper MAPPER = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).findAndRegisterModules();

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy @ HH:mm:ss.[SSS][SS][S]");

    public static final String PERIOD = ".";

    public static final String UNDERSCORE = "_";

    public static final String SOURCE = "source";

    public static final String TIME = "time";

    public static final String TIMESTAMP = "timestamp";

    public static final String ACCOUNT_ID = "account_id";

    public static final String LOG = "log";

    public static final String MESSAGE = "message";

    public static final String ICEBERG_USER_LEGACY_LOGIN_LOGS = "/HDAPQA-1698-USER-LOGIN-DATA.json";

    public static final String USERNAME_SET = "/USERNAMES.txt";

    public static final String HDAP_USER_HIT_LOGS = "/USER_HIT_DATA.json";

    public static final String USER_HIT_RESULT = "/USER_HIT_RESULT.csv";

    public static final String SEARCH_PARAM = "regular login request have succeed for user";

    public static final String UNIQUE_USER_LOGIN_DATA_FILE_PATH = "/UNIQUE-USER-LOGIN-DATA.csv";

    public static final String UNIQUE_USERNAMES_FILE_PATH = "/UNIQUE-USERNAMES.txt";

    public static final String INFO = "INFO";
}
