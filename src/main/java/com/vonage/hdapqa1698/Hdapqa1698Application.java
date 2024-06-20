package com.vonage.hdapqa1698;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vonage.hdapqa1698.helpers.Constants;
import com.vonage.hdapqa1698.services.FileService;
import com.vonage.hdapqa1698.services.HDAPUserHitCountService;
import com.vonage.hdapqa1698.services.LegacyPathUserLoginDiscovery;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.util.Objects;

@SpringBootApplication
@RequiredArgsConstructor
public class Hdapqa1698Application implements CommandLineRunner {

	private final LegacyPathUserLoginDiscovery legacyPathUserLoginDiscovery;

	private final HDAPUserHitCountService hdapUserHitCountService;

	private final FileService fileService;

	public static void main(String[] args) throws JsonProcessingException {
		SpringApplication.run(Hdapqa1698Application.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
//		legacyPathUserLoginDiscovery.getNumberOfInvocationPerUsername();
//		legacyPathUserLoginDiscovery.getUniqueUsernames();

		hdapUserHitCountService.getUserLogHits();
	}
}
