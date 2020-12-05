package com.vb.market.engine.booking;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.pattern.StatusReply;
import com.vb.market.domain.PlaceOrderRequest;
import com.vb.market.domain.Side;
import com.vb.market.engine.MatchingManagerActor;
import com.vb.market.engine.MatchingManagerActor.BalanceBooksCommand;
import com.vb.market.engine.MatchingManagerActor.CancelOrderMessage;
import com.vb.market.engine.MatchingManagerActor.CancelOrderReply;
import com.vb.market.engine.MatchingManagerActor.PlaceOrderReply;
import com.vb.market.engine.MatchingManagerActor.PlaceOrderMessage;
import com.vb.market.engine.booking.OrderBook.BookEntry;
import com.vb.market.engine.booking.OrderBook.KeyPriority;
import com.vb.market.exceptions.ApplicationException;
import com.vb.market.exceptions.CommonCause;
import com.vb.market.utils.IdGen;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;


public class BooksActor extends AbstractBehavior<BooksActor.Command> {

    public interface Command {}

    public static final class GetBookEntriesMessage implements Command {
        public final int takeFirstN;
        public final Side side;
        public final ActorRef<BookEntriesReply> replyTo;

        public GetBookEntriesMessage(int takeFirstN, Side side, ActorRef<BookEntriesReply> replyTo) {
            this.takeFirstN = takeFirstN;
            this.side = side;
            this.replyTo = replyTo;
        }
    }

    public static final class BookEntriesReply implements Command {
        public final Map<OrderBook.KeyPriority, OrderBook.BookEntry> bookEntries;

        public BookEntriesReply(Map<OrderBook.KeyPriority, OrderBook.BookEntry> bookEntries) {
            this.bookEntries = bookEntries;
        }
    }

    private IdGen idGen;

    private String bookId;

    private OrderBook buyBook;

    private OrderBook sellBook;

    private ActorRef<TradeLedgerActor.Command> ledgerActor;

    public static Behavior<Command> create(String bookId, ActorRef<TradeLedgerActor.Command> ledgerActor) {
        return Behaviors.setup(context -> new BooksActor(context, bookId, ledgerActor));
    }

    private BooksActor(ActorContext<Command> context, String bookId, ActorRef<TradeLedgerActor.Command> ledgerActor) {
        super(context);
        this.ledgerActor = ledgerActor;
        this.idGen = new IdGen();
        this.bookId = bookId;
        this.buyBook = new OrderBook(Side.BUY);
        this.sellBook = new OrderBook(Side.SELL);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(PlaceOrderMessage.class, this::onPlaceOrder)
                .onMessage(MatchingManagerActor.CancelOrderMessage.class, this::onCancelOrderCommand)
                .onMessage(BalanceBooksCommand.class, this::onBookBalanceCommand)
                .onMessage(GetBookEntriesMessage.class, this::getBookEntries)
                .build();
    }

    private Behavior<Command> getBookEntries(GetBookEntriesMessage getBookEntriesMessage) {
        Map<KeyPriority, OrderBook.BookEntry> bookEntries;

        if (Side.BUY == getBookEntriesMessage.side) {
            bookEntries = buyBook.getBookEntries(getBookEntriesMessage.takeFirstN);
        } else {
            bookEntries = sellBook.getBookEntries(getBookEntriesMessage.takeFirstN);
        }

        getBookEntriesMessage.replyTo.tell(new BookEntriesReply(bookEntries));

        return this;
    }

    private Behavior<Command> onBookBalanceCommand(BalanceBooksCommand balanceBooksCommand) {
        balanceBooksRecursively();
        return this;
    }

    private void balanceBooksRecursively() {
        // Choose active side as a BUY side
        Optional<KeyPriority> first = buyBook.getBookEntries().keySet().stream().findFirst();

        if (first.isPresent()) {
            Optional<MatchResult> nextMatch = getNextMatch(first.get(), sellBook);

            if (nextMatch.isPresent()) {
                playMatchResult(nextMatch.get());
                balanceBooksRecursively();
            }
        }
    }

    private void playMatchResult(MatchResult nextMatch) {
        Trade trade = new Trade();

        OrderBook.BookEntry buyEntry = buyBook.getBookEntries().get(nextMatch.getBuySide());
        OrderBook.BookEntry sellEntry = sellBook.getBookEntries().get(nextMatch.getSellSide());

        trade.setBuyEventId(buyEntry.getKey().getEventId());
        trade.setSellEventId(sellEntry.getKey().getEventId());
        trade.setSymbol(bookId);

        Integer tradePrice = calculateTradePrice(buyEntry, sellEntry);
        trade.setTradePrice(tradePrice);

        if (isSellCanBeFullyFilled(buyEntry, sellEntry)) {
            int tradeQuantity = sellEntry.getQuantity();
            trade.setTradeQuantity(tradeQuantity);

            buyEntry.setQuantity(buyEntry.getQuantity() - tradeQuantity);
            sellEntry.setQuantity(0);
        } else {
            int tradeQuantity = buyEntry.getQuantity();
            trade.setTradeQuantity(tradeQuantity);

            sellEntry.setQuantity(sellEntry.getQuantity() - tradeQuantity);
            buyEntry.setQuantity(0);
        }

        clearFullyFilledOrders(buyEntry, sellEntry);

        ledgerActor.tell(new TradeLedgerActor.WriteTradeTransactionMessage(trade));
    }

    private void clearFullyFilledOrders(BookEntry buyEntry, BookEntry sellEntry) {
        if (buyEntry.isFullyFilled()) {
            buyBook.getBookEntries().remove(buyEntry.getKey());
        }

        if (sellEntry.isFullyFilled()) {
            sellBook.getBookEntries().remove(sellEntry.getKey());
        }
    }

    private boolean isSellCanBeFullyFilled(BookEntry buyEntry, OrderBook.BookEntry sellEntry) {
        return buyEntry.getQuantity() >= sellEntry.getQuantity();
    }

    private Integer calculateTradePrice(BookEntry buy, BookEntry sell) {
        //TODO:@vlbo - how do we decided it? Seems the bid price should win.
        return buy.getKey().getPrice();
    }

    private Optional<MatchResult> getNextMatch(KeyPriority buyKey, OrderBook sellBook) {
        Optional<MatchResult> matchResult = sellBook.getBookEntries().entrySet().stream()
                .findFirst()
                .map(Map.Entry::getKey)
                .filter(sellKey -> buyKey.getPrice() >= sellKey.getPrice())
                .map(keyPriority -> new MatchResult(buyKey, keyPriority));
        return matchResult;
    }


    private Behavior<Command> onPlaceOrder(PlaceOrderMessage placeOrderMessage) {
        PlaceOrderRequest placeOrderRequest = placeOrderMessage.placeOrderRequest;

        placeOrderRequest.setEventId(idGen.getID());
        Instant submissionTime = Instant.now();
        placeOrderRequest.setSubmittedTime(submissionTime);

        PlaceOrderReply placeOrderReply;

        if (Side.BUY == placeOrderRequest.getSide()) {
            placeOrderReply = buyBook.addOrder(placeOrderRequest);
        } else {
            placeOrderReply = sellBook.addOrder(placeOrderRequest);
        }

        placeOrderMessage.replyTo.tell(StatusReply.success(placeOrderReply));
        getContext().getLog().info("Order ID={} SYMBOL={} has been placed", placeOrderRequest.getEventId(), bookId);
        return this;
    }

    private Behavior<Command> onCancelOrderCommand(CancelOrderMessage cancelCommand) {
        Side side = cancelCommand.cancelOrderRequest.getSide();

        Optional<CancelOrderReply> successOrFail;

        if (Side.BUY == side) {
            successOrFail = buyBook.cancelOrder(cancelCommand.cancelOrderRequest);
        } else {
            successOrFail = sellBook.cancelOrder(cancelCommand.cancelOrderRequest);
        }

        if (successOrFail.isPresent()) {
            cancelCommand.replyTo.tell(StatusReply.success(successOrFail.get()));
        } else {
            cancelCommand.replyTo.tell(StatusReply.error(
                    new ApplicationException(CommonCause.ORDER_NOT_FOUND,CommonCause.ORDER_NOT_FOUND.getDescription())));
        }

        return this;
    }
}
