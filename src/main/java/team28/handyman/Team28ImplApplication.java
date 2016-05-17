package team28.handyman;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement(proxyTargetClass=true)
@EnableScheduling
public class Team28ImplApplication {

	public static void main(String[] args) {
		SpringApplication.run(Team28ImplApplication.class, args);
	}
}
