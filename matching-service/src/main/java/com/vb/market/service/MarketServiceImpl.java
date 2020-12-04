package com.vb.market.service;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.AskPattern;
import com.vb.market.domain.Order;
import com.vb.market.engine.MatchingManager;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

@Service
public class MarketServiceImpl implements MarketService {

    private ActorSystem<MatchingManager.Command> actorSystem;

    public MarketServiceImpl(ActorSystem<MatchingManager.Command> actorSystem) {
        this.actorSystem = actorSystem;
    }

    @Override
    public CompletionStage<MatchingManager.OrderReply> placeOrder(Order order) {
        return AskPattern.askWithStatus(
                actorSystem,
                replyTo -> new MatchingManager.PlaceOrderMessage(order, replyTo),
                Duration.ofMinutes(5),
                actorSystem.scheduler());
    }
}
