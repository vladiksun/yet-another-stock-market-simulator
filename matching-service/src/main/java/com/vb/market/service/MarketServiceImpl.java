package com.vb.market.service;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.AskPattern;
import akka.japi.function.Function;
import akka.pattern.StatusReply;
import com.vb.market.domain.CancelOrderRequest;
import com.vb.market.domain.PlaceOrderRequest;
import com.vb.market.engine.TradeManagingActor.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

@Service
public class MarketServiceImpl implements MarketService {

    private final static Duration AKKA_TIME_OUT = Duration.ofMinutes(5);

    private ActorSystem<Command> actorSystem;

    @Autowired
    public MarketServiceImpl(ActorSystem<Command> actorSystem) {
        this.actorSystem = actorSystem;
    }

    @Override
    public CompletionStage<OrderPlacedReply> placeOrder(PlaceOrderRequest placeOrderRequest) {
        return AskPattern.askWithStatus(
                actorSystem,
                (Function<ActorRef<StatusReply<OrderPlacedReply>>, Command>)
                replyTo -> new PlaceOrderMessage(placeOrderRequest, replyTo),
                AKKA_TIME_OUT,
                actorSystem.scheduler());
    }

    @Override
    public CompletionStage<CancelOrderReply> cancelOrder(CancelOrderRequest cancelOrderRequest) {
        return AskPattern.askWithStatus(
                actorSystem,
                (Function<ActorRef<StatusReply<CancelOrderReply>>, Command>)
                replyTo -> new CancelOrderMessage(cancelOrderRequest, replyTo),
                AKKA_TIME_OUT,
                actorSystem.scheduler());
    }
}
