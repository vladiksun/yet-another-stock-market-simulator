package com.vb.market.engine;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.*;
import akka.pattern.StatusReply;
import com.vb.market.domain.Order;
import com.vb.market.engine.MatchingManager.Command;
import com.vb.market.engine.booking.Books;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class MatchingManager extends AbstractBehavior<Command> {

    public interface Command {}

    // protocol start
    public static final class PlaceOrderMessage implements Command, Books.Command {
        public final Order order;
        public final ActorRef<StatusReply<OrderReply>> replyTo;

        public PlaceOrderMessage(Order order, ActorRef<StatusReply<OrderReply>> replyTo) {
            this.order = order;
            this.replyTo = replyTo;
        }
    }

    public static final class OrderReply {
        public final Order order;
        public final Instant submittedTime;

        public OrderReply(Order order, Instant submittedTime) {
            this.order = order;
            this.submittedTime = submittedTime;
        }
    }

    private static class BooksActorTerminatedMessage implements Command {
        public final String bookId;

        BooksActorTerminatedMessage(String bookId) {
            this.bookId = bookId;
        }
    }

    public enum BalanceBooksCommand implements Command, Books.Command {
        INSTANCE
    }
    // protocol end


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

    private Behavior<MatchingManager.Command> onPlaceOrderMessage(PlaceOrderMessage placeOrderMessage) {
        String bookId = placeOrderMessage.order.getSymbol();
        ActorRef<Books.Command> bookActorRef = bookIdToActor.get(bookId);

        if (bookActorRef != null) {
            bookActorRef.tell(placeOrderMessage);
        } else {
            getContext().getLog().info("Creating booking actor for symbol {}", bookId);
            bookActorRef = getContext().spawn(Books.create(bookId), "book-" + bookId);
            getContext().watchWith(bookActorRef, new BooksActorTerminatedMessage(bookId));

            bookIdToActor.put(bookId, bookActorRef);
            bookActorRef.tell(placeOrderMessage);
        }
        return this;
    }

    private MatchingManager onBalanceBooksCommand(BalanceBooksCommand balanceBooksCommand) {
        getContext().getLog().info("Time to balance books....");

        bookIdToActor.forEach((bookId, bookActorRef) -> {
            bookActorRef.tell(balanceBooksCommand);
        });

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
