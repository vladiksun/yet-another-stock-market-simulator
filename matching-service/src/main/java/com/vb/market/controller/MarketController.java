package com.vb.market.controller;

import com.vb.market.domain.OrderOperationStatus;
import com.vb.market.domain.Order;
import com.vb.market.service.MarketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletionStage;

@RestController
@RequestMapping("/market")
public class MarketController {

    private final MarketService marketService;

    @Autowired
    public MarketController(final MarketService marketService) {
        this.marketService = marketService;
    }

    @PostMapping(path = "/placeorder",
            consumes = {MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public CompletionStage<ResponseEntity<OrderOperationStatus>> placeOrder(@RequestBody Order order) {
        return marketService.placeOrder(order).handle((orderPlaced, throwable) -> {
            OrderOperationStatus.Builder builder = OrderOperationStatus.Builder.anOrderOperationStatus()
                    .withRequestedOrder(order);

            if (orderPlaced != null) {
                OrderOperationStatus success = builder.but()
                        .withMessage("SUCCESS")
                        .build();
                return new ResponseEntity<>(success, HttpStatus.OK);
            } else {
                OrderOperationStatus failure = builder.but()
                        .withMessage("FAILURE")
                        .withReason(throwable.getMessage())
                        .build();
                return new ResponseEntity<>(failure, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        });

    }
}
