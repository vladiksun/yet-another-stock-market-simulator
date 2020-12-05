package com.vb.market.service;

import com.vb.market.domain.CancelOrderRequest;
import com.vb.market.domain.PlaceOrderRequest;
import com.vb.market.engine.MatchingManagerActor;

import java.util.concurrent.CompletionStage;

public interface MarketService {

    CompletionStage<MatchingManagerActor.PlaceOrderReply> placeOrder(PlaceOrderRequest placeOrderRequest);

    CompletionStage<MatchingManagerActor.CancelOrderReply> cancelOrder(CancelOrderRequest cancelOrderRequest);
}
