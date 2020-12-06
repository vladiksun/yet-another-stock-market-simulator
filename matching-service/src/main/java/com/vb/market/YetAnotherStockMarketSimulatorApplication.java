package com.vb.market;

import akka.actor.typed.ActorSystem;
import com.vb.market.engine.TradeManagingActor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class YetAnotherStockMarketSimulatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(YetAnotherStockMarketSimulatorApplication.class, args);
	}

	@Bean
	public ActorSystem<TradeManagingActor.Command> akkaSupervisorActor() {
		return ActorSystem.create(TradeManagingActor.create(), "stock-market-system");
	}

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/**");
			}
		};
	}

}
