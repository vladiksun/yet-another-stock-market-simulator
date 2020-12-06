package com.vb.market.service;

import com.vb.market.domain.CancelOrderRequest;
import com.vb.market.domain.PlaceOrderRequest;
import com.vb.market.engine.TradeManagingActor.CancelOrderReply;
import com.vb.market.engine.TradeManagingActor.OrderPlacedReply;

import java.util.concurrent.CompletionStage;

public interface MarketService {

    CompletionStage<OrderPlacedReply> placeOrder(PlaceOrderRequest placeOrderRequest);

    CompletionStage<CancelOrderReply> cancelOrder(CancelOrderRequest cancelOrderRequest);
}
