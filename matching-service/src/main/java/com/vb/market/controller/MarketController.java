package com.vb.market.controller;

import com.vb.market.domain.CancelOrderRequest;
import com.vb.market.domain.CancelOrderResponse;
import com.vb.market.domain.PlaceOrderResponse;
import com.vb.market.domain.PlaceOrderRequest;
import com.vb.market.service.MarketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.concurrent.CompletionStage;

import static com.vb.market.controller.Status.FAILURE;
import static com.vb.market.controller.Status.SUCCESS;

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
    public CompletionStage<ResponseEntity<PlaceOrderResponse>> placeOrder(@Valid @RequestBody PlaceOrderRequest placeOrderRequest) {
        return marketService.placeOrder(placeOrderRequest).handle((orderPlaced, throwable) -> {
            PlaceOrderResponse.Builder builder = PlaceOrderResponse.Builder.anOrderOperationStatus()
                    .withRequestedOrder(placeOrderRequest);

            if (orderPlaced != null) {
                PlaceOrderResponse success = builder.but()
                        .withStatus(SUCCESS)
                        .build();
                return new ResponseEntity<>(success, HttpStatus.OK);
            } else {
                PlaceOrderResponse failure = builder.but()
                        .withStatus(FAILURE)
                        .withReason(throwable.getMessage())
                        .build();
                return new ResponseEntity<>(failure, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        });
    }

    @PostMapping(path = "/cancelorder",
            consumes = {MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public CompletionStage<ResponseEntity<CancelOrderResponse>> cancelOrder(@Valid @RequestBody CancelOrderRequest cancelOrderRequest) {
        return marketService.cancelOrder(cancelOrderRequest).handle((orderCanceled, throwable) -> {
            CancelOrderResponse.Builder builder = CancelOrderResponse.Builder.aCancelOrderResponse()
                    .withCancelOrderRequest(cancelOrderRequest);

            if (orderCanceled != null) {
                CancelOrderResponse success = builder.but()
                        .withMessage(SUCCESS)
                        .build();
                return new ResponseEntity<>(success, HttpStatus.OK);
            } else {
                CancelOrderResponse failure = builder.but()
                        .withMessage(FAILURE)
                        .withReason(throwable.getMessage())
                        .build();
                return new ResponseEntity<>(failure, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        });
    }
}
