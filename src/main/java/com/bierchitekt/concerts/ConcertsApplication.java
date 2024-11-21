package com.bierchitekt.concerts;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.sql.DataSource;

@SpringBootApplication
@EnableScheduling
public class ConcertsApplication {

	@Autowired
	Environment env;

	public static void main(String[] args) {
		SpringApplication.run(ConcertsApplication.class, args);
	}

	@Bean
	public DataSource dataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName(this.env.getProperty("driverClassName"));
		dataSource.setUrl(this.env.getProperty("url"));
		dataSource.setUsername(this.env.getProperty("user"));
		dataSource.setPassword(this.env.getProperty("password"));
		return dataSource;
	}
}
