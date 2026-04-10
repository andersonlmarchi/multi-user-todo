package br.com.jtech.tasklist;

import br.com.jtech.tasklist.config.security.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class StartTasklist {

	public static void main(String[] args) {
		SpringApplication.run(StartTasklist.class, args);
	}

}
