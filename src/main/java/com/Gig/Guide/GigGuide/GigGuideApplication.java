package com.Gig.Guide.GigGuide;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class GigGuideApplication {

	public static void main(String[] args) {
		SpringApplication.run(GigGuideApplication.class, args);
	}

}
