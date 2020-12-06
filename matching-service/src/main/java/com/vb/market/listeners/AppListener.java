package com.vb.market.listeners;

import com.vb.market.domain.CancelOrderRequest;
import com.vb.market.domain.PlaceOrderRequest;
import com.vb.market.events.BookBalanceEvent;
import com.vb.market.events.OrderCanceledEvent;
import com.vb.market.events.OrderPlacedEvent;
import com.vb.market.events.TradeTransactionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class AppListener {

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @EventListener(OrderPlacedEvent.class)
    public void onOrderPlacedEvent(OrderPlacedEvent event) {
        PlaceOrderRequest placeOrderRequest = event.getOrderPlacedReply().placeOrderRequest;
        String message = "ORDER PLACED: " + placeOrderRequest.toString();
        simpMessagingTemplate.convertAndSend("/topic/eventlog", message);
    }

    @EventListener(OrderCanceledEvent.class)
    public void onOrderCanceledEvent(OrderCanceledEvent event) {
        CancelOrderRequest cancelOrderRequest = event.getCancelOrderReply().cancelOrderRequest;
        String message = "ORDER CANCELED: " + cancelOrderRequest.toString();
        simpMessagingTemplate.convertAndSend("/topic/eventlog", message);
    }

    @EventListener(BookBalanceEvent.class)
    public void onBookBalanceEvent(BookBalanceEvent event) {
        simpMessagingTemplate.convertAndSend("/topic/eventlog", "Time to balance books");
    }

    public void onTradeTransactionEvent(TradeTransactionEvent event) {
        simpMessagingTemplate.convertAndSend("/topic/eventlog", String.format("TRADE transaction complete %s", event.getTrade()));
    }
}
