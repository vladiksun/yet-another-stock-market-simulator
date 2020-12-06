package com.vb.market.listeners;

import com.vb.market.domain.PlaceOrderRequest;
import com.vb.market.events.OrderPlacedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class AppListener implements ApplicationListener<OrderPlacedEvent> {

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Override
    public void onApplicationEvent(OrderPlacedEvent event) {
        PlaceOrderRequest placeOrderRequest = event.getOrderPlacedReply().placeOrderRequest;
        String message = "ORDER PLACED: " + placeOrderRequest.toString();
        simpMessagingTemplate.convertAndSend("/topic/eventlog", message);

        System.out.println("Order Placed !!!!!!!!");
    }
}
