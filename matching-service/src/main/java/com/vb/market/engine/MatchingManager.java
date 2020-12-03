package com.vb.market.engine;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.*;
import akka.pattern.StatusReply;
import com.vb.market.domain.OrderRequest;
import com.vb.market.engine.booking.Books;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class MatchingManager extends AbstractBehavior<MatchingManager.Command> {

    public interface Command {}

    public static final class PlaceOrderMessage implements MatchingManager.Command, Books.Command {
        public final OrderRequest orderRequest;
        public final ActorRef<StatusReply<Reply>> replyTo;

        public PlaceOrderMessage(OrderRequest orderRequest, ActorRef<StatusReply<Reply>> replyTo) {
            this.orderRequest = orderRequest;
            this.replyTo = replyTo;
        }
    }

    private static class BooksActorTerminatedMessage implements MatchingManager.Command {
        public final String bookId;

        BooksActorTerminatedMessage(String bookId) {
            this.bookId = bookId;
        }
    }

    public enum BalanceBooksCommand implements MatchingManager.Command {
        INSTANCE
    }

    interface Reply {}

    public enum OrderPlaced implements MatchingManager.Reply {
        INSTANCE
    }

    private final Map<String, ActorRef<Books.Command>> bookIdToActor = new HashMap<>();

    public static Behavior<Command> create() {
        return Behaviors.setup(context ->
                    Behaviors.withTimers(timers -> new MatchingManager(context, timers)));
    }

    private MatchingManager(ActorContext<Command> context, TimerScheduler<Command> timers) {
        super(context);

        timers.startTimerWithFixedDelay(BalanceBooksCommand.INSTANCE, Duration.ofSeconds(3));

        context.getLog().info("MatchingManager started");
    }

    private MatchingManager onPlaceOrderMessage(PlaceOrderMessage placeOrderMessage) {
        String bookId = placeOrderMessage.orderRequest.getSymbol();
        ActorRef<Books.Command> bookActorRef = bookIdToActor.get(bookId);

        if (bookActorRef != null) {
            bookActorRef.tell(placeOrderMessage);
        } else {
            getContext().getLog().info("Creating booking actor for symbol {}", bookId);
            ActorRef<Books.Command> booksActor = getContext().spawn(Books.create(), "book-" + bookId);
            getContext().watchWith(booksActor, new BooksActorTerminatedMessage(bookId));

            bookActorRef.tell(placeOrderMessage);
            bookIdToActor.put(bookId, booksActor);
        }
        return this;
    }

    private MatchingManager onBalanceBooksCommand(BalanceBooksCommand balanceBooksCommand) {
        //TODO:@vlbo - trigger books balancing
        getContext().getLog().info("Time to balance books....");
        return this;
    }

    private MatchingManager onBooksActorTerminated(BooksActorTerminatedMessage message) {
        getContext().getLog().info("Booking actor for symbol {} terminated", message.bookId);
        return this;
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(PlaceOrderMessage.class, this::onPlaceOrderMessage)
                .onMessage(BooksActorTerminatedMessage.class, this::onBooksActorTerminated)
                .onMessage(BalanceBooksCommand.class, this::onBalanceBooksCommand)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    private MatchingManager onPostStop() {
        getContext().getLog().info("MatchingManager actor stopped");
        return this;
    }


}
