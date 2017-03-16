package com.hkust;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
public class SimpleActivitiApplication extends SpringBootServletInitializer {
	
	@Autowired
	private EmailService wiser;
	
	@Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(SimpleActivitiApplication.class);
    }
	
	public static void main(String[] args) {
		SpringApplication.run(SimpleActivitiApplication.class, args);
	}
	
    @Bean
    public CommandLineRunner init() {
    	return new CommandLineRunner() {

			@Override
			public void run(String... arg0) throws Exception {
				//start email server
		        wiser.start();
			}
    	};
    }

}
