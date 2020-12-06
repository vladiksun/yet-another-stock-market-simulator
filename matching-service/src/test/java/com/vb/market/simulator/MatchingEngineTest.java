package com.vb.market.simulator;

import akka.actor.testkit.typed.javadsl.LogCapturing;
import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import akka.pattern.StatusReply;
import com.vb.market.YetAnotherStockMarketSimulatorApplication;
import com.vb.market.domain.PlaceOrderRequest;
import com.vb.market.domain.PlaceOrderRequest.Builder;
import com.vb.market.domain.Side;
import com.vb.market.engine.TradeManagingActor;
import com.vb.market.engine.TradeManagingActor.OrderPlacedReply;
import com.vb.market.engine.TradeManagingActor.PlaceOrderMessage;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = YetAnotherStockMarketSimulatorApplication.class)
@AutoConfigureMockMvc
public class MatchingEngineTest {

    @ClassRule
    public static final TestKitJunitResource testKit = new TestKitJunitResource();

    @Rule
    public final LogCapturing logCapturing = new LogCapturing();

    @Test
    public void testBUYOrderMustBePlaced() {
        TestProbe<StatusReply<OrderPlacedReply>> testProbe = testKit.createTestProbe();

        ActorRef<TradeManagingActor.Command> matchingManager = testKit.spawn(TradeManagingActor.create());

        PlaceOrderRequest placeOrderRequest = PlaceOrderRequest.Builder.anOrderRequest()
                .withClientId("Vlad")
                .withPrice(20)
                .withQuantity(100)
                .withSide(Side.BUY)
                .withSymbol("AAA")
                .build();

        matchingManager.tell(new PlaceOrderMessage(placeOrderRequest, testProbe.getRef()));

        StatusReply<OrderPlacedReply> orderPlacedStatusReply = testProbe.receiveMessage();

        assertEquals(new Integer(20), orderPlacedStatusReply.getValue().placeOrderRequest.getPrice());
        assertNotNull(orderPlacedStatusReply.getValue().placeOrderRequest.getSubmittedTime());
    }

    @Test
    public void testSELLOrderMustBePlaced() {
        TestProbe<StatusReply<OrderPlacedReply>> testProbe = testKit.createTestProbe();

        ActorRef<TradeManagingActor.Command> matchingManager = testKit.spawn(TradeManagingActor.create());

        PlaceOrderRequest placeOrderRequest = Builder.anOrderRequest()
                .withEventId(1L)
                .withClientId("Vlad")
                .withPrice(20)
                .withQuantity(100)
                .withSide(Side.SELL)
                .withSymbol("AAA")
                .build();

        matchingManager.tell(new PlaceOrderMessage(placeOrderRequest, testProbe.getRef()));

        StatusReply<OrderPlacedReply> orderPlacedStatusReply = testProbe.receiveMessage();

        assertEquals(new Integer(20), orderPlacedStatusReply.getValue().placeOrderRequest.getPrice());
        assertNotNull(orderPlacedStatusReply.getValue().placeOrderRequest.getSubmittedTime());
    }
}
