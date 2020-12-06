package com.vb.market.controller;

import com.vb.market.domain.CancelOrderRequest;
import com.vb.market.domain.CancelOrderResponse;
import com.vb.market.domain.PlaceOrderRequest;
import com.vb.market.domain.PlaceOrderResponse;
import com.vb.market.service.MarketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import javax.validation.Valid;
import java.util.concurrent.CompletionStage;

import static com.vb.market.controller.Status.FAILURE;
import static com.vb.market.controller.Status.SUCCESS;

@Controller
public class WebSocketController {

    private final MarketService marketService;

    @Autowired
    public WebSocketController(MarketService marketService) {
        this.marketService = marketService;
    }

    @MessageMapping("/placeorder")
    @SendToUser("/queue/reply")
    public CompletionStage<PlaceOrderResponse> placeOrder(@Valid PlaceOrderRequest placeOrderRequest) {
        return marketService.placeOrder(placeOrderRequest).handle((orderPlaced, throwable) -> {
            PlaceOrderResponse.Builder builder = PlaceOrderResponse.Builder.anOrderOperationStatus()
                    .withRequestedOrder(placeOrderRequest);

            if (orderPlaced != null) {
                return builder.but()
                        .withStatus(SUCCESS)
                        .build();
            } else {
                return builder.but()
                        .withStatus(FAILURE)
                        .withReason(throwable.getMessage())
                        .build();
            }
        });
    }

    @MessageMapping("/cancelorder")
    @SendToUser("/queue/reply")
    public CompletionStage<CancelOrderResponse> cancelOrder(@Valid CancelOrderRequest cancelOrderRequest) {
        return marketService.cancelOrder(cancelOrderRequest).handle((orderCanceled, throwable) -> {
            CancelOrderResponse.Builder builder = CancelOrderResponse.Builder.aCancelOrderResponse()
                    .withCancelOrderRequest(cancelOrderRequest);
            if (orderCanceled != null) {
                return builder.but()
                        .withMessage(SUCCESS)
                        .build();
            } else {
                return builder.but()
                        .withMessage(FAILURE)
                        .withReason(throwable.getMessage())
                        .build();
            }
        });
    }

    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    public String handleException(Throwable exception) {
        return exception.getMessage();
    }
}
