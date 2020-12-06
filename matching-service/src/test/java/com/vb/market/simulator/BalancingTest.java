package com.vb.market.simulator;

import akka.actor.testkit.typed.javadsl.LogCapturing;
import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import akka.pattern.StatusReply;
import com.vb.market.YetAnotherStockMarketSimulatorApplication;
import com.vb.market.controller.MarketController;
import com.vb.market.domain.PlaceOrderRequest;
import com.vb.market.domain.PlaceOrderRequest.Builder;
import com.vb.market.domain.Side;
import com.vb.market.engine.TradeManagingActor.BalanceBooksCommand;
import com.vb.market.engine.TradeManagingActor.OrderPlacedReply;
import com.vb.market.engine.TradeManagingActor.PlaceOrderMessage;
import com.vb.market.engine.booking.BookKeepingActor;
import com.vb.market.engine.booking.OrderBook;
import com.vb.market.engine.booking.TradeLedgerActor;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = YetAnotherStockMarketSimulatorApplication.class)
@AutoConfigureMockMvc
public class BalancingTest {

    @ClassRule
    public static final TestKitJunitResource testKit = new TestKitJunitResource();

    @Rule
    public final LogCapturing logCapturing = new LogCapturing();

    /***
     *      Simulate a cul-de-sac situation. Nobody can buy or sell
     *
     *         Input:
     *         Id   Side    Time   Qty   Price   Qty    Time   Side
     *         ---+------+-------+-----+-------+-----+-------+------
     *         #6                        23     200   09:05   SELL
     *         #4                        23     100   09:01   SELL
     *         #5                        22     100   09:03   SELL
     *                      wait the highest bid to happen
     *         #2   BUY    09:08   200   21
     *         #1   BUY    09:06   100   20
     *         #3   BUY    09:09   200   20
     *
     */

    @Test
    public void balanceShouldRemainTheSameWhileWaitingTheHighestBid() {
        ActorRef<TradeLedgerActor.Command> ledger = testKit.spawn(TradeLedgerActor.create(), "akka-ledger");
        ActorRef<BookKeepingActor.Command> booksActor = testKit.spawn(BookKeepingActor.create("TEST", ledger));

        TestProbe<BookKeepingActor.BookEntriesReply> testProbeN = testKit.createTestProbe();

        placeBUYOrdersScenario1(booksActor);
        placeSELLOrdersScenario1(booksActor);

        booksActor.tell(BalanceBooksCommand.INSTANCE);

        booksActor.tell(new BookKeepingActor.GetBookEntriesMessage(3, Side.BUY, testProbeN.getRef()));
        BookKeepingActor.BookEntriesReply buyReply = testProbeN.receiveMessage();
        Map<OrderBook.KeyPriority, OrderBook.BookEntry> buyBookEntries = buyReply.bookEntries;

        booksActor.tell(new BookKeepingActor.GetBookEntriesMessage(3, Side.SELL, testProbeN.getRef()));
        BookKeepingActor.BookEntriesReply sellReply = testProbeN.receiveMessage();
        Map<OrderBook.KeyPriority, OrderBook.BookEntry> selBookEntries = sellReply.bookEntries;

        List<Long> actualBuyBookEntries = buyBookEntries.keySet()
                .stream()
                .map(OrderBook.KeyPriority::getEventId)
                .collect(Collectors.toList());

        List<Long> actualSellBookEntries = selBookEntries.keySet()
                .stream()
                .map(OrderBook.KeyPriority::getEventId)
                .collect(Collectors.toList());

        List<Long> expectedBuyOrders = Arrays.asList(2L, 1L, 3L);
        List<Long> expectedSellOrders = Arrays.asList(5L, 4L, 6L);

        assertEquals(expectedBuyOrders, actualBuyBookEntries);
        assertEquals(expectedSellOrders, actualSellBookEntries);
    }

    /***
     *      Simulate a cul-de-sac situation. Nobody can buy or sell
     *
     *         Input:
     *         Id   Side    Time   Qty   Price   Qty    Time   Side
     *         ---+------+-------+-----+-------+-----+-------+------
     *         #6                        23     200   09:05   SELL
     *         #4                        23     100   09:01   SELL
     *         #5                        22     100   09:03   SELL
     *                      New order comes: BUY 250 shares @ 24 ---> leads to result
     *         #2   BUY    09:08   200   21
     *         #1   BUY    09:06   100   20
     *         #3   BUY    09:09   200   20
     *
     *
     *         Id   Side    Time   Qty   Price   Qty    Time   Side
     *         ---+------+-------+-----+-------+-----+-------+------
     *         #6                        23     150   09:05   SELL
     *
     *         [ ************** fills out of the book ****************]
     *         #6                        23     50    09:05   SELL
     *         #4                        23     100   09:01   SELL
     *         #5                        22     100   09:03   SELL
     *         #7   BUY    09:12   250   24
     *         [ ************** fills out of the book ****************]
     *
     *         #2   BUY    09:08   200   21
     *         #1   BUY    09:06   100   20
     *         #3   BUY    09:09   200   20
     *
     */
    @Test
    public void shouldBalanceTheOrdersWhenNewBuyOrderComesIn() {
        ActorRef<TradeLedgerActor.Command> ledger = testKit.spawn(TradeLedgerActor.create(), "akka-ledger");
        ActorRef<BookKeepingActor.Command> booksActor = testKit.spawn(BookKeepingActor.create("TEST", ledger));

        TestProbe<BookKeepingActor.BookEntriesReply> testProbeN = testKit.createTestProbe();
        TestProbe<StatusReply<OrderPlacedReply>> testProbe = testKit.createTestProbe();

        placeBUYOrdersScenario1(booksActor);
        placeSELLOrdersScenario1(booksActor);

        booksActor.tell(BalanceBooksCommand.INSTANCE);

        PlaceOrderRequest buyPlaceOrderRequest7 = Builder.anOrderRequest()
                .withEventId(7L)
                .withPrice(24)
                .withQuantity(250)
                .withSide(Side.BUY)
                .build();
        booksActor.tell(new PlaceOrderMessage(buyPlaceOrderRequest7, testProbe.getRef()));

        booksActor.tell(BalanceBooksCommand.INSTANCE);

        booksActor.tell(new BookKeepingActor.GetBookEntriesMessage(3, Side.BUY, testProbeN.getRef()));
        BookKeepingActor.BookEntriesReply buyReply = testProbeN.receiveMessage();
        Map<OrderBook.KeyPriority, OrderBook.BookEntry> buyBookEntries = buyReply.bookEntries;

        booksActor.tell(new BookKeepingActor.GetBookEntriesMessage(3, Side.SELL, testProbeN.getRef()));
        BookKeepingActor.BookEntriesReply sellReply = testProbeN.receiveMessage();
        Map<OrderBook.KeyPriority, OrderBook.BookEntry> selBookEntries = sellReply.bookEntries;

        List<Long> actualBuyBookEntries = buyBookEntries.keySet()
                .stream()
                .map(OrderBook.KeyPriority::getEventId)
                .collect(Collectors.toList());

        List<Long> actualSellBookEntries = selBookEntries.keySet()
                .stream()
                .map(OrderBook.KeyPriority::getEventId)
                .collect(Collectors.toList());

        List<Long> expectedBuyOrders = Arrays.asList(2L, 1L, 3L);
        List<Long> expectedSellOrders = Arrays.asList(6L);

        assertEquals(expectedBuyOrders, actualBuyBookEntries);
        assertEquals(expectedSellOrders, actualSellBookEntries);
    }

    private void placeBUYOrdersScenario1(ActorRef<BookKeepingActor.Command> booksActor) {
        TestProbe<StatusReply<OrderPlacedReply>> testProbe = testKit.createTestProbe();

        PlaceOrderRequest buyPlaceOrderRequest1 = Builder.anOrderRequest()
                .withEventId(1L)
                .withPrice(20)
                .withQuantity(100)
                .withSide(Side.BUY)
                .build();
        booksActor.tell(new PlaceOrderMessage(buyPlaceOrderRequest1, testProbe.getRef()));

        PlaceOrderRequest buyPlaceOrderRequest2 = Builder.anOrderRequest()
                .withEventId(2L)
                .withPrice(21)
                .withQuantity(200)
                .withSide(Side.BUY)
                .build();
        booksActor.tell(new PlaceOrderMessage(buyPlaceOrderRequest2, testProbe.getRef()));

        PlaceOrderRequest buyPlaceOrderRequest3 = Builder.anOrderRequest()
                .withEventId(3L)
                .withPrice(20)
                .withQuantity(200)
                .withSide(Side.BUY)
                .build();

        booksActor.tell(new PlaceOrderMessage(buyPlaceOrderRequest3, testProbe.getRef()));
    }

    private void placeSELLOrdersScenario1(ActorRef<BookKeepingActor.Command> booksActor) {
        TestProbe<StatusReply<OrderPlacedReply>> testProbe = testKit.createTestProbe();

        PlaceOrderRequest sellPlaceOrderRequest1 = Builder.anOrderRequest()
                .withEventId(4L)
                .withPrice(23)
                .withQuantity(100)
                .withSide(Side.SELL)
                .build();
        booksActor.tell(new PlaceOrderMessage(sellPlaceOrderRequest1, testProbe.getRef()));

        PlaceOrderRequest sellPlaceOrderRequest2 = Builder.anOrderRequest()
                .withEventId(5L)
                .withPrice(22)
                .withQuantity(100)
                .withSide(Side.SELL)
                .build();
        booksActor.tell(new PlaceOrderMessage(sellPlaceOrderRequest2, testProbe.getRef()));

        PlaceOrderRequest sellPlaceOrderRequest3 = Builder.anOrderRequest()
                .withEventId(6L)
                .withPrice(23)
                .withQuantity(200)
                .withSide(Side.SELL)
                .build();
        booksActor.tell(new PlaceOrderMessage(sellPlaceOrderRequest3, testProbe.getRef()));
    }

}
