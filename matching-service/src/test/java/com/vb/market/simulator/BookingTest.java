package com.vb.market.simulator;

import akka.actor.testkit.typed.javadsl.LogCapturing;
import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import akka.pattern.StatusReply;
import com.vb.market.domain.PlaceOrderRequest;
import com.vb.market.domain.PlaceOrderRequest.Builder;
import com.vb.market.domain.Side;
import com.vb.market.engine.MatchingManagerActor.PlaceOrderReply;
import com.vb.market.engine.MatchingManagerActor.PlaceOrderMessage;
import com.vb.market.engine.booking.BooksActor;
import com.vb.market.engine.booking.BooksActor.GetBookEntriesMessage;
import com.vb.market.engine.booking.OrderBook;
import com.vb.market.engine.booking.TradeLedgerActor;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class BookingTest {

    @ClassRule
    public static final TestKitJunitResource testKit = new TestKitJunitResource();

    @Rule
    public final LogCapturing logCapturing = new LogCapturing();

    @Test
    public void testPriceTimePrioritySortingForBUY() {
        TestProbe<StatusReply<PlaceOrderReply>> testProbe = testKit.createTestProbe();
        TestProbe<BooksActor.BookEntriesReply> testProbeN = testKit.createTestProbe();

        ActorRef<TradeLedgerActor.Command> ledger = testKit.spawn(TradeLedgerActor.create(), "akka-ledger");
        ActorRef<BooksActor.Command> booksActor = testKit.spawn(BooksActor.create("TEST", ledger));

        PlaceOrderRequest placeOrderRequest1 = Builder.anOrderRequest()
                .withPrice(20)
                .withSide(Side.BUY)
                .build();
        booksActor.tell(new PlaceOrderMessage(placeOrderRequest1, testProbe.getRef()));

        PlaceOrderRequest placeOrderRequest2 = Builder.anOrderRequest()
                .withPrice(20)
                .withSide(Side.BUY)
                .build();
        booksActor.tell(new PlaceOrderMessage(placeOrderRequest2, testProbe.getRef()));

        PlaceOrderRequest placeOrderRequest3 = Builder.anOrderRequest()
                .withPrice(21)
                .withSide(Side.BUY)
                .build();
        booksActor.tell(new PlaceOrderMessage(placeOrderRequest3, testProbe.getRef()));

        PlaceOrderRequest placeOrderRequest4 = Builder.anOrderRequest()
                .withPrice(21)
                .withSide(Side.BUY)
                .build();
        booksActor.tell(new PlaceOrderMessage(placeOrderRequest4, testProbe.getRef()));

        PlaceOrderRequest placeOrderRequest5 = Builder.anOrderRequest()
                .withPrice(25)
                .withSide(Side.BUY)
                .build();
        booksActor.tell(new PlaceOrderMessage(placeOrderRequest5, testProbe.getRef()));

        booksActor.tell(new GetBookEntriesMessage(5, Side.BUY, testProbeN.getRef()));
        BooksActor.BookEntriesReply bookEntriesReply = testProbeN.receiveMessage();

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
    public void testPriceTimePrioritySortingForSELL() {
        TestProbe<StatusReply<PlaceOrderReply>> testProbe = testKit.createTestProbe();
        TestProbe<BooksActor.BookEntriesReply> testProbeN = testKit.createTestProbe();

        ActorRef<TradeLedgerActor.Command> ledger = testKit.spawn(TradeLedgerActor.create(), "akka-ledger");
        ActorRef<BooksActor.Command> booksActor = testKit.spawn(BooksActor.create("TEST", ledger));

        PlaceOrderRequest placeOrderRequest1 = Builder.anOrderRequest()
                .withPrice(22)
                .withSide(Side.SELL)
                .withEventId(1L)
                .build();
        booksActor.tell(new PlaceOrderMessage(placeOrderRequest1, testProbe.getRef()));

        PlaceOrderRequest placeOrderRequest2 = Builder.anOrderRequest()
                .withPrice(23)
                .withSide(Side.SELL)
                .withEventId(2L)
                .build();
        booksActor.tell(new PlaceOrderMessage(placeOrderRequest2, testProbe.getRef()));

        PlaceOrderRequest placeOrderRequest3 = Builder.anOrderRequest()
                .withPrice(22)
                .withSide(Side.SELL)
                .withEventId(3L)
                .build();
        booksActor.tell(new PlaceOrderMessage(placeOrderRequest3, testProbe.getRef()));

        PlaceOrderRequest placeOrderRequest4 = Builder.anOrderRequest()
                .withPrice(25)
                .withSide(Side.SELL)
                .withEventId(4L)
                .build();
        booksActor.tell(new PlaceOrderMessage(placeOrderRequest4, testProbe.getRef()));

        PlaceOrderRequest placeOrderRequest5 = Builder.anOrderRequest()
                .withPrice(25)
                .withSide(Side.SELL)
                .withEventId(5L)
                .build();
        booksActor.tell(new PlaceOrderMessage(placeOrderRequest5, testProbe.getRef()));

        booksActor.tell(new GetBookEntriesMessage(5, Side.SELL, testProbeN.getRef()));
        BooksActor.BookEntriesReply bookEntriesReply = testProbeN.receiveMessage();

        Map<OrderBook.KeyPriority, OrderBook.BookEntry> bookEntries = bookEntriesReply.bookEntries;

        // 1 ---> 3 ---> 2 ---> 4 ---> 5
        List<Long> expected = Arrays.asList(1L, 3L, 2L, 4L, 5L);

        List<Long> actual = bookEntries.keySet()
                .stream()
                .map(OrderBook.KeyPriority::getEventId)
                .collect(Collectors.toList());

        assertEquals(expected, actual);
    }

}
