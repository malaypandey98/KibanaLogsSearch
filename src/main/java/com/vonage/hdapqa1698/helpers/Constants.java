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

    public static final String UNIQUE_USERNAMES_FILE_PATH = "/UNIQUE-USERS.txt";

    public static final String INFO = "INFO";

    public static final String REQUEST_LOG_FILTER_CLASS = "[com.vocalocity.hdap.logging.RequestLogFilter]";

    public static final String LOG_SERVICE_CLASS = "[org.restlet.Component.LogService]";

    public static final String UNIQUE_APIS = "UNIQUE_APIS.csv";

    public static final String PLTFRM_LOG_REQUEST = "{\n" +
            "    \"params\": {\n" +
            "        \"index\": \"vbc_platform_services_team*\",\n" +
            "        \"body\": {\n" +
            "            \"sort\": [\n" +
            "                {\n" +
            "                    \"timestamp\": {\n" +
            "                        \"order\": \"desc\",\n" +
            "                        \"unmapped_type\": \"boolean\"\n" +
            "                    }\n" +
            "                }\n" +
            "            ],\n" +
            "            \"size\": 500,\n" +
            "             \"from\":{page_number}, \n" +
            "            \"version\": true,\n" +
            "            \"aggs\": {\n" +
            "                \"2\": {\n" +
            "                    \"date_histogram\": {\n" +
            "                        \"field\": \"timestamp\",\n" +
            "                        \"fixed_interval\": \"12h\",\n" +
            "                        \"time_zone\": \"UTC\",\n" +
            "                        \"min_doc_count\": 1\n" +
            "                    }\n" +
            "                }\n" +
            "            },\n" +
            "            \"stored_fields\": [\n" +
            "                \"*\"\n" +
            "            ],\n" +
            "            \"script_fields\": {},\n" +
            "            \"docvalue_fields\": [\n" +
            "                {\n" +
            "                    \"field\": \"ef-json-message.request_time\",\n" +
            "                    \"format\": \"date_time\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"field\": \"processed_timestamp\",\n" +
            "                    \"format\": \"date_time\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"field\": \"timestamp\",\n" +
            "                    \"format\": \"date_time\"\n" +
            "                }\n" +
            "            ],\n" +
            "            \"_source\": {\n" +
            "                \"excludes\": []\n" +
            "            },\n" +
            "            \"query\": {\n" +
            "                \"bool\": {\n" +
            "                    \"must\": [],\n" +
            "                    \"filter\": [\n" +
            "                        {\n" +
            "                            \"multi_match\": {\n" +
            "                                \"type\": \"phrase\",\n" +
            "                                \"query\": \"{search_param}\",\n" +
            "                                \"lenient\": true\n" +
            "                            }\n" +
            "                        },\n" +
            "                        {\n" +
            "                            \"range\": {\n" +
            "                                \"timestamp\": {\n" +
            "                                    \"gte\": \"2024-06-06T14:23:43.577Z\",\n" +
            "                                    \"lte\": \"2024-06-20T14:23:43.577Z\",\n" +
            "                                    \"format\": \"strict_date_optional_time\"\n" +
            "                                }\n" +
            "                            }\n" +
            "                        }\n" +
            "                    ],\n" +
            "                    \"should\": [],\n" +
            "                    \"must_not\": []\n" +
            "                }\n" +
            "            },\n" +
            "            \"highlight\": {\n" +
            "                \"pre_tags\": [\n" +
            "                    \"@opensearch-dashboards-highlighted-field@\"\n" +
            "                ],\n" +
            "                \"post_tags\": [\n" +
            "                    \"@/opensearch-dashboards-highlighted-field@\"\n" +
            "                ],\n" +
            "                \"fields\": {\n" +
            "                    \"*\": {}\n" +
            "                },\n" +
            "                \"fragment_size\": 2147483647\n" +
            "            }\n" +
            "        },\n" +
            "        \"preference\": 1718883921612\n" +
            "    }\n" +
            "}";

    public static final String CERBERUS_LOG_REQUEST = "{\n" +
            "    \"params\": {\n" +
            "        \"index\": \"vbc_cerberus*\",\n" +
            "        \"body\": {\n" +
            "            \"sort\": [\n" +
            "                {\n" +
            "                    \"timestamp\": {\n" +
            "                        \"order\": \"{order_type}\",\n" +
            "                        \"unmapped_type\": \"boolean\"\n" +
            "                    }\n" +
            "                }\n" +
            "            ],\n" +
            "            \"size\": 10000,\n" +
            "            \"version\": true,\n" +
            "            \"aggs\": {\n" +
            "                \"2\": {\n" +
            "                    \"date_histogram\": {\n" +
            "                        \"field\": \"timestamp\",\n" +
            "                        \"fixed_interval\": \"12h\",\n" +
            "                        \"time_zone\": \"UTC\",\n" +
            "                        \"min_doc_count\": 1\n" +
            "                    }\n" +
            "                }\n" +
            "            },\n" +
            "            \"stored_fields\": [\n" +
            "                \"*\"\n" +
            "            ],\n" +
            "            \"script_fields\": {},\n" +
            "            \"docvalue_fields\": [\n" +
            "                {\n" +
            "                    \"field\": \"@timestamp\",\n" +
            "                    \"format\": \"date_time\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"field\": \"annotations.kubectl_kubernetes_io/restartedAt\",\n" +
            "                    \"format\": \"date_time\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"field\": \"date\",\n" +
            "                    \"format\": \"date_time\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"field\": \"ef-json-message.headers.X-Von-Date\",\n" +
            "                    \"format\": \"date_time\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"field\": \"ef-json-message.time\",\n" +
            "                    \"format\": \"date_time\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"field\": \"msg-json.time\",\n" +
            "                    \"format\": \"date_time\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"field\": \"msg-json.timestamp\",\n" +
            "                    \"format\": \"date_time\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"field\": \"processed_timestamp\",\n" +
            "                    \"format\": \"date_time\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"field\": \"time\",\n" +
            "                    \"format\": \"date_time\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"field\": \"timestamp\",\n" +
            "                    \"format\": \"date_time\"\n" +
            "                }\n" +
            "            ],\n" +
            "            \"_source\": {\n" +
            "                \"excludes\": []\n" +
            "            },\n" +
            "            \"query\": {\n" +
            "                \"bool\": {\n" +
            "                    \"must\": [],\n" +
            "                    \"filter\": [\n" +
            "                        {\n" +
            "                            \"multi_match\": {\n" +
            "                                \"type\": \"phrase\",\n" +
            "                                \"query\": \"{search_param}\",\n" +
            "                                \"lenient\": true\n" +
            "                            }\n" +
            "                        },\n" +
            "                        {\n" +
            "                            \"match_phrase\": {\n" +
            "                                \"siem_environment\": \"prd\"\n" +
            "                            }\n" +
            "                        }, \n" +
            "                        {\n" +
            "                            \"range\": {\n" +
            "                                \"timestamp\": {\n" +
            "                                    \"gte\": \"{gte}Z\",\n" +
            "                                    \"lte\": \"{lte}Z\",\n" +
            "                                    \"format\": \"strict_date_optional_time\"\n" +
            "                                }\n" +
            "                            }\n" +
            "                        }\n" +
            "                    ],\n" +
            "                    \"should\": [],\n" +
            "                    \"must_not\": []\n" +
            "                }\n" +
            "            },\n" +
            "            \"highlight\": {\n" +
            "                \"pre_tags\": [\n" +
            "                    \"@opensearch-dashboards-highlighted-field@\"\n" +
            "                ],\n" +
            "                \"post_tags\": [\n" +
            "                    \"@/opensearch-dashboards-highlighted-field@\"\n" +
            "                ],\n" +
            "                \"fields\": {\n" +
            "                    \"*\": {}\n" +
            "                },\n" +
            "                \"fragment_size\": 2147483647\n" +
            "            }\n" +
            "        },\n" +
            "        \"preference\": 1718976705435\n" +
            "    }\n" +
            "}";
}
