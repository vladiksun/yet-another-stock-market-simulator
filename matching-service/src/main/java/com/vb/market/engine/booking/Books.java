package com.vb.market.engine.booking;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class Books extends AbstractBehavior<Books.Command> {

    public interface Command {}

    private String bookId;

    private OrderBook buyBook;

    private OrderBook sellBook;

    public static Behavior<Command> create() {
        return Behaviors.setup(Books::new);
    }

    private Books(ActorContext<Command> context) {
        super(context);
    }

    @Override
    public Receive<Command> createReceive() {
        return null;
    }

}
