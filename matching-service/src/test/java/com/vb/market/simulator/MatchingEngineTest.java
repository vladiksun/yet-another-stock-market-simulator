package com.vb.market.simulator;

import akka.actor.testkit.typed.javadsl.LogCapturing;
import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import akka.pattern.StatusReply;
import com.vb.market.domain.Order;
import com.vb.market.domain.Order.Builder;
import com.vb.market.domain.Side;
import com.vb.market.engine.MatchingManager;
import com.vb.market.engine.MatchingManager.OrderReply;
import com.vb.market.engine.MatchingManager.PlaceOrderMessage;
import com.vb.market.engine.booking.Books;
import com.vb.market.engine.booking.Books.GetBookEntriesMessage;
import com.vb.market.engine.booking.OrderBook;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MatchingEngineTest {

    @ClassRule
    public static final TestKitJunitResource testKit = new TestKitJunitResource();

    @Rule
    public final LogCapturing logCapturing = new LogCapturing();

    @Test
    public void testBUYOrderMustBePlaced() {
        TestProbe<StatusReply<OrderReply>> testProbe = testKit.createTestProbe();

        ActorRef<MatchingManager.Command> matchingManager = testKit.spawn(MatchingManager.create());

        Order order = Order.Builder.anOrderRequest()
                .withClientId("Vlad")
                .withPrice(20)
                .withQuantity(100)
                .withSide(Side.BUY)
                .withSymbol("AAA")
                .build();

        matchingManager.tell(new PlaceOrderMessage(order, testProbe.getRef()));

        StatusReply<OrderReply> orderPlacedStatusReply = testProbe.receiveMessage();

        assertEquals(new Integer(20), orderPlacedStatusReply.getValue().order.getPrice());
        assertNotNull(orderPlacedStatusReply.getValue().submittedTime);
    }

    @Test
    public void testSELLOrderMustBePlaced() {
        TestProbe<StatusReply<OrderReply>> testProbe = testKit.createTestProbe();

        ActorRef<MatchingManager.Command> matchingManager = testKit.spawn(MatchingManager.create());

        Order order = Builder.anOrderRequest()
                .withOrderId(1L)
                .withClientId("Vlad")
                .withPrice(20)
                .withQuantity(100)
                .withSide(Side.SELL)
                .withSymbol("AAA")
                .build();

        matchingManager.tell(new PlaceOrderMessage(order, testProbe.getRef()));

        StatusReply<OrderReply> orderPlacedStatusReply = testProbe.receiveMessage();

        assertEquals(new Integer(20), orderPlacedStatusReply.getValue().order.getPrice());
        assertNotNull(orderPlacedStatusReply.getValue().submittedTime);
    }

    @Test
    public void testPriceTimePrioritySortingForBUY() {
        TestProbe<StatusReply<OrderReply>> testProbe = testKit.createTestProbe();
        TestProbe<Books.BookEntriesReply> testProbeN = testKit.createTestProbe();

        ActorRef<Books.Command> booksActor = testKit.spawn(Books.create("TEST"));

        Order order1 = Builder.anOrderRequest()
                .withPrice(20)
                .withSide(Side.BUY)
                .build();
        booksActor.tell(new PlaceOrderMessage(order1, testProbe.getRef()));

        Order order2 = Builder.anOrderRequest()
                .withPrice(20)
                .withSide(Side.BUY)
                .build();
        booksActor.tell(new PlaceOrderMessage(order2, testProbe.getRef()));

        Order order3 = Builder.anOrderRequest()
                .withPrice(21)
                .withSide(Side.BUY)
                .build();
        booksActor.tell(new PlaceOrderMessage(order3, testProbe.getRef()));

        Order order4 = Builder.anOrderRequest()
                .withPrice(21)
                .withSide(Side.BUY)
                .build();
        booksActor.tell(new PlaceOrderMessage(order4, testProbe.getRef()));

        Order order5 = Builder.anOrderRequest()
                .withPrice(25)
                .withSide(Side.BUY)
                .build();
        booksActor.tell(new PlaceOrderMessage(order5, testProbe.getRef()));

        booksActor.tell(new GetBookEntriesMessage(5, Side.BUY, testProbeN.getRef()));
        Books.BookEntriesReply bookEntriesReply = testProbeN.receiveMessage();

        Map<OrderBook.KeyPriority, OrderBook.BookEntry> bookEntries = bookEntriesReply.bookEntries;

        // 5 ---> 3 ---> 4 ---> 1 ---> 2
        List<Long> expected = Arrays.asList(5L, 3L, 4L, 1L, 2L);

        List<Long> actual = bookEntries.keySet()
                .stream()
                .map(OrderBook.KeyPriority::getEventId)
                .collect(Collectors.toList());

        assertEquals(expected, actual);
    }

    @Test
    public void testPriceTimePrioritySortingForBUYNoAkka() {
        OrderBook orderBook = new OrderBook(Side.BUY);

        Order order1 = Builder.anOrderRequest()
                .withPrice(20)
                .withSide(Side.BUY)
                .withOrderId(1L)
                .build();

        Order order2 = Builder.anOrderRequest()
                .withPrice(20)
                .withSide(Side.BUY)
                .withOrderId(2L)
                .build();

        Order order3 = Builder.anOrderRequest()
                .withPrice(21)
                .withSide(Side.BUY)
                .withOrderId(3L)
                .build();

        Order order4 = Builder.anOrderRequest()
                .withPrice(21)
                .withSide(Side.BUY)
                .withOrderId(4L)
                .build();

        Order order5 = Builder.anOrderRequest()
                .withPrice(25)
                .withSide(Side.BUY)
                .withOrderId(5L)
                .build();

        orderBook.addOrder(order1);
        orderBook.addOrder(order2);
        orderBook.addOrder(order3);
        orderBook.addOrder(order4);
        orderBook.addOrder(order5);

        // 5 ---> 3 ---> 4 ---> 1 ---> 2
        List<Long> expected = Arrays.asList(5L, 3L, 4L, 1L, 2L);

        Map<OrderBook.KeyPriority, OrderBook.BookEntry> bookEntries = orderBook.getBookEntries(5);

        List<Long> actual = bookEntries.keySet()
                .stream()
                .map(OrderBook.KeyPriority::getEventId)
                .collect(Collectors.toList());

        assertEquals(expected, actual);
    }

}
