package com.vb.market;

import akka.actor.typed.ActorSystem;
import com.vb.market.engine.MatchingManagerActor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class YetAnotherStockMarketSimulatorApplication {

	@Bean
	public ActorSystem<MatchingManagerActor.Command> akkaSupervisorActor() {
		return ActorSystem.create(MatchingManagerActor.create(), "stock-market-system");
	}

	public static void main(String[] args) {
		SpringApplication.run(YetAnotherStockMarketSimulatorApplication.class, args);
	}

}
