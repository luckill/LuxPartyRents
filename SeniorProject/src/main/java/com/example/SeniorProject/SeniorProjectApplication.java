package com.example.SeniorProject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.*;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class SeniorProjectApplication
{

	public static void main(String[] args)
	{
		SpringApplication.run(SeniorProjectApplication.class, args);
	}

}
