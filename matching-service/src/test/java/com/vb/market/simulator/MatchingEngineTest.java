package com.vb.market.simulator;

import akka.actor.testkit.typed.javadsl.LogCapturing;
import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import akka.pattern.StatusReply;
import com.vb.market.domain.PlaceOrderRequest;
import com.vb.market.domain.PlaceOrderRequest.Builder;
import com.vb.market.domain.Side;
import com.vb.market.engine.MatchingManagerActor;
import com.vb.market.engine.MatchingManagerActor.PlaceOrderReply;
import com.vb.market.engine.MatchingManagerActor.PlaceOrderMessage;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MatchingEngineTest {

    @ClassRule
    public static final TestKitJunitResource testKit = new TestKitJunitResource();

    @Rule
    public final LogCapturing logCapturing = new LogCapturing();

    @Test
    public void testBUYOrderMustBePlaced() {
        TestProbe<StatusReply<PlaceOrderReply>> testProbe = testKit.createTestProbe();

        ActorRef<MatchingManagerActor.Command> matchingManager = testKit.spawn(MatchingManagerActor.create());

        PlaceOrderRequest placeOrderRequest = PlaceOrderRequest.Builder.anOrderRequest()
                .withClientId("Vlad")
                .withPrice(20)
                .withQuantity(100)
                .withSide(Side.BUY)
                .withSymbol("AAA")
                .build();

        matchingManager.tell(new PlaceOrderMessage(placeOrderRequest, testProbe.getRef()));

        StatusReply<PlaceOrderReply> orderPlacedStatusReply = testProbe.receiveMessage();

        assertEquals(new Integer(20), orderPlacedStatusReply.getValue().placeOrderRequest.getPrice());
        assertNotNull(orderPlacedStatusReply.getValue().placeOrderRequest.getSubmittedTime());
    }

    @Test
    public void testSELLOrderMustBePlaced() {
        TestProbe<StatusReply<PlaceOrderReply>> testProbe = testKit.createTestProbe();

        ActorRef<MatchingManagerActor.Command> matchingManager = testKit.spawn(MatchingManagerActor.create());

        PlaceOrderRequest placeOrderRequest = Builder.anOrderRequest()
                .withEventId(1L)
                .withClientId("Vlad")
                .withPrice(20)
                .withQuantity(100)
                .withSide(Side.SELL)
                .withSymbol("AAA")
                .build();

        matchingManager.tell(new PlaceOrderMessage(placeOrderRequest, testProbe.getRef()));

        StatusReply<PlaceOrderReply> orderPlacedStatusReply = testProbe.receiveMessage();

        assertEquals(new Integer(20), orderPlacedStatusReply.getValue().placeOrderRequest.getPrice());
        assertNotNull(orderPlacedStatusReply.getValue().placeOrderRequest.getSubmittedTime());
    }
}
