package de.codingsolo.ui;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SpringBootApplication
public class ElasticSearchSpringApplication {
	private static  final Logger LOGGER = LogManager.getLogger(ElasticSearchSpringApplication.class);
	
	public static void main(String[] args) {

		SpringApplication.run(ElasticSearchSpringApplication.class, args);

	}
}
