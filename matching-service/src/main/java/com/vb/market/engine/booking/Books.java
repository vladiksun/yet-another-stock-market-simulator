package com.vb.market.engine.booking;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.pattern.StatusReply;
import com.vb.market.domain.Order;
import com.vb.market.domain.Side;
import com.vb.market.engine.MatchingManager.BalanceBooksCommand;
import com.vb.market.engine.MatchingManager.OrderReply;
import com.vb.market.engine.MatchingManager.PlaceOrderMessage;

import java.util.Map;

public class Books extends AbstractBehavior<Books.Command> {

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

    private String bookId;

    private OrderBook buyBook;

    private OrderBook sellBook;

    public static Behavior<Command> create(String bookId) {
        return Behaviors.setup(context -> new Books(context, bookId));
    }

    private Books(ActorContext<Command> context, String bookId) {
        super(context);
        this.bookId = bookId;
        this.buyBook = new OrderBook(Side.BUY);
        this.sellBook = new OrderBook(Side.SELL);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(PlaceOrderMessage.class, this::onPlaceOrder)
                .onMessage(BalanceBooksCommand.class, this::onBookBalanceCommand)
                .onMessage(GetBookEntriesMessage.class, this::getBookEntries)
                .build();
    }

    private Behavior<Command> getBookEntries(GetBookEntriesMessage getBookEntriesMessage) {
        Map<OrderBook.KeyPriority, OrderBook.BookEntry> bookEntries;

        if (Side.BUY == getBookEntriesMessage.side) {
            bookEntries = buyBook.getBookEntries(getBookEntriesMessage.takeFirstN);
        } else {
            bookEntries = sellBook.getBookEntries(getBookEntriesMessage.takeFirstN);
        }

        getBookEntriesMessage.replyTo.tell(new BookEntriesReply(bookEntries));

        return this;
    }

    private Behavior<Command> onBookBalanceCommand(BalanceBooksCommand balanceBooksCommand) {
        balanceBooks();
        return this;
    }

    private void balanceBooks() {
        //TODO:@vlbo - implement algorithm
    }


    private Behavior<Command> onPlaceOrder(PlaceOrderMessage placeOrderMessage) {
        Order order = placeOrderMessage.order;

        OrderReply orderReply;

        if (Side.BUY == order.getSide()) {
            orderReply = buyBook.addOrder(order);
        } else {
            orderReply = sellBook.addOrder(order);
        }

        placeOrderMessage.replyTo.tell(StatusReply.success(orderReply));
        getContext().getLog().info("Order ID={} SYMBOL={} has been placed", order.getEventId(), bookId);
        return this;
    }


}
