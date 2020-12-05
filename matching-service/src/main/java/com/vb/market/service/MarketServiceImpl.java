package com.vb.market.service;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.AskPattern;
import com.vb.market.domain.CancelOrderRequest;
import com.vb.market.domain.PlaceOrderRequest;
import com.vb.market.engine.MatchingManagerActor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

@Service
public class MarketServiceImpl implements MarketService {

    private final static Duration AKKA_TIME_OUT = Duration.ofMinutes(5);

    private ActorSystem<MatchingManagerActor.Command> actorSystem;

    @Autowired
    public MarketServiceImpl(ActorSystem<MatchingManagerActor.Command> actorSystem) {
        this.actorSystem = actorSystem;
    }

    @Override
    public CompletionStage<MatchingManagerActor.PlaceOrderReply> placeOrder(PlaceOrderRequest placeOrderRequest) {
        return AskPattern.askWithStatus(
                actorSystem,
                replyTo -> new MatchingManagerActor.PlaceOrderMessage(placeOrderRequest, replyTo),
                AKKA_TIME_OUT,
                actorSystem.scheduler());
    }

    @Override
    public CompletionStage<MatchingManagerActor.CancelOrderReply> cancelOrder(CancelOrderRequest cancelOrderRequest) {
        return AskPattern.askWithStatus(
                actorSystem,
                replyTo -> new MatchingManagerActor.CancelOrderMessage(cancelOrderRequest, replyTo),
                AKKA_TIME_OUT,
                actorSystem.scheduler());
    }
}
