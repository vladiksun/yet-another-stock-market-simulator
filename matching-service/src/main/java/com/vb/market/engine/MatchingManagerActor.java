package com.vb.market.engine;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.*;
import akka.pattern.StatusReply;
import com.vb.market.domain.CancelOrderRequest;
import com.vb.market.domain.PlaceOrderRequest;
import com.vb.market.engine.MatchingManagerActor.Command;
import com.vb.market.engine.booking.BooksActor;
import com.vb.market.engine.booking.TradeLedgerActor;
import com.vb.market.exceptions.ApplicationCause;
import com.vb.market.exceptions.ApplicationException;
import com.vb.market.exceptions.CommonCause;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class MatchingManagerActor extends AbstractBehavior<Command> {

    public interface Command {}

    // protocol start
    public static final class PlaceOrderMessage implements Command, BooksActor.Command {
        public final PlaceOrderRequest placeOrderRequest;
        public final ActorRef<StatusReply<PlaceOrderReply>> replyTo;

        public PlaceOrderMessage(PlaceOrderRequest placeOrderRequest, ActorRef<StatusReply<PlaceOrderReply>> replyTo) {
            this.placeOrderRequest = placeOrderRequest;
            this.replyTo = replyTo;
        }
    }

    public static final class PlaceOrderReply {
        public final PlaceOrderRequest placeOrderRequest;

        public PlaceOrderReply(PlaceOrderRequest placeOrderRequest) {
            this.placeOrderRequest = placeOrderRequest;
        }
    }

    public static final class CancelOrderMessage implements Command, BooksActor.Command {
        public final CancelOrderRequest cancelOrderRequest;
        public final ActorRef<StatusReply<CancelOrderReply>> replyTo;

        public CancelOrderMessage(CancelOrderRequest cancelOrderRequest, ActorRef<StatusReply<CancelOrderReply>> replyTo) {
            this.cancelOrderRequest = cancelOrderRequest;
            this.replyTo = replyTo;
        }
    }

    public static final class CancelOrderReply {
        public final CancelOrderRequest cancelOrderRequest;

        public CancelOrderReply(CancelOrderRequest cancelOrderRequest) {
            this.cancelOrderRequest = cancelOrderRequest;
        }
    }

    private static class BooksActorTerminatedMessage implements Command {
        public final String bookId;

        BooksActorTerminatedMessage(String bookId) {
            this.bookId = bookId;
        }
    }

    public enum BalanceBooksCommand implements Command, BooksActor.Command {
        INSTANCE
    }
    // protocol end


    private final Map<String, ActorRef<BooksActor.Command>> bookIdToActor = new HashMap<>();

    private ActorRef<TradeLedgerActor.Command> ledgerActor;

    public static Behavior<Command> create() {
        return Behaviors.setup(context ->
                    Behaviors.withTimers(timers -> new MatchingManagerActor(context, timers)));
    }

    private MatchingManagerActor(ActorContext<Command> context, TimerScheduler<Command> timers) {
        super(context);

        this.ledgerActor = getContext().spawn(TradeLedgerActor.create(), "akka-ledger");

        //timers.startTimerWithFixedDelay(BalanceBooksCommand.INSTANCE, Duration.ofMinutes(1));
        timers.startTimerWithFixedDelay(BalanceBooksCommand.INSTANCE, Duration.ofSeconds(5));

        context.getLog().info("MatchingManager started");
    }

    private Behavior<Command> onPlaceOrderMessage(PlaceOrderMessage placeOrderMessage) {
        String bookId = placeOrderMessage.placeOrderRequest.getSymbol();
        ActorRef<BooksActor.Command> bookActorRef = bookIdToActor.get(bookId);

        if (bookActorRef != null) {
            bookActorRef.tell(placeOrderMessage);
        } else {
            getContext().getLog().info("Creating booking actor for symbol {}", bookId);
            bookActorRef = getContext().spawn(BooksActor.create(bookId, ledgerActor), "akka-book-" + bookId);
            getContext().watchWith(bookActorRef, new BooksActorTerminatedMessage(bookId));

            bookIdToActor.put(bookId, bookActorRef);
            bookActorRef.tell(placeOrderMessage);
        }
        return this;
    }

    private Behavior<Command> onCancelOrderCommand(CancelOrderMessage cancelCommand) {
        String bookId = cancelCommand.cancelOrderRequest.getSymbol();
        ActorRef<BooksActor.Command> bookActor = bookIdToActor.get(bookId);

        if (bookActor != null) {
            bookActor.tell(cancelCommand);
        } else {
            cancelCommand.replyTo.tell(StatusReply.error(new ApplicationException(CommonCause.SYMBOL_NOT_EXISTS,
                    CommonCause.SYMBOL_NOT_EXISTS.getDescription())));
        }

        return this;
    }

    private MatchingManagerActor onBalanceBooksCommand(BalanceBooksCommand balanceBooksCommand) {
        getContext().getLog().info("Time to balance books....");

        bookIdToActor.forEach((bookId, bookActorRef) -> {
            bookActorRef.tell(balanceBooksCommand);
        });

        return this;
    }

    private MatchingManagerActor onBooksActorTerminated(BooksActorTerminatedMessage message) {
        getContext().getLog().info("Booking actor for symbol {} terminated", message.bookId);
        return this;
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(PlaceOrderMessage.class, this::onPlaceOrderMessage)
                .onMessage(BooksActorTerminatedMessage.class, this::onBooksActorTerminated)
                .onMessage(BalanceBooksCommand.class, this::onBalanceBooksCommand)
                .onMessage(CancelOrderMessage.class, this::onCancelOrderCommand)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    private MatchingManagerActor onPostStop() {
        getContext().getLog().info("MatchingManager actor stopped");
        return this;
    }


}
