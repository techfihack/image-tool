package com.example.imagetools;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude={DataSourceAutoConfiguration.class})
public class ImageToolsApplication {

	public static void main(String[] args){
		SpringApplication.run(ImageToolsApplication.class, args);
	}

}
