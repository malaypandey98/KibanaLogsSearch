package com.vonage.hdapqa1698;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.vonage.hdapqa1698.helpers.Constants;
import com.vonage.hdapqa1698.helpers.Order;
import com.vonage.hdapqa1698.pojo.KibanaResponse;
import com.vonage.hdapqa1698.services.FileService;
import com.vonage.hdapqa1698.services.HDAPUserHitCountService;
import com.vonage.hdapqa1698.services.LegacyPathUserLoginDiscovery;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

@SpringBootApplication
@RequiredArgsConstructor
public class Hdapqa1698Application implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(Hdapqa1698Application.class);
	private final LegacyPathUserLoginDiscovery legacyPathUserLoginDiscovery;

	private final HDAPUserHitCountService hdapUserHitCountService;

	private final FileService fileService;

	public static void main(String[] args) throws JsonProcessingException {
		SpringApplication.run(Hdapqa1698Application.class, args);
	}

	@Override
	public void run(String... args) throws Exception {


//		LocalDateTime date = LocalDateTime.parse("2024-06-21T10:34:25.448");
//		legacyPathUserLoginDiscovery.callKibanaAPI(
//				"regular login request have succeed for user",
//				0,
//				date,
//				date.plusHours(1L),
//				Order.DESC,
//				null);

		legacyPathUserLoginDiscovery.getNumberOfInvocationPerUsername();
//		legacyPathUserLoginDiscovery.getUniqueUsernames();

//		KibanaResponse response = hdapUserHitCountService.callKibanaAPI("regular login request have succeed for user", 0);

	}
}