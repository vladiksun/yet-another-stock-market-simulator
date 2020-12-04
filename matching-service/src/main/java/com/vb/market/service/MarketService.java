package com.vb.market.service;

import com.vb.market.domain.Order;
import com.vb.market.engine.MatchingManager;

import java.util.concurrent.CompletionStage;

public interface MarketService {

    CompletionStage<MatchingManager.OrderReply> placeOrder(Order order);

}
