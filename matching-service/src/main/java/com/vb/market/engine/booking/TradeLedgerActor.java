package com.vb.market.engine.booking;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.vb.market.utils.IdGen;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public class TradeLedgerActor extends AbstractBehavior<TradeLedgerActor.Command> {

    public interface Command {}

    public static final class WriteTradeTransactionMessage implements Command {
        public final Trade trade;

        public WriteTradeTransactionMessage(Trade trade) {
            this.trade = trade;
        }
    }

    private IdGen idGen;

    private Map<Long, Trade> ledger = new LinkedHashMap<>();

    public static Behavior<TradeLedgerActor.Command> create() {
        return Behaviors.setup(TradeLedgerActor::new);
    }

    private TradeLedgerActor(ActorContext<TradeLedgerActor.Command> context) {
        super(context);
        idGen = new IdGen();
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(WriteTradeTransactionMessage.class, this::onWriteTransaction)
                .build();
    }

    private Behavior<Command> onWriteTransaction(WriteTradeTransactionMessage message) {
        Trade trade = message.trade;
        Long id = idGen.getID();
        trade.setTradeId(id);
        trade.setSubmittedTime(Instant.now());
        ledger.put(id, message.trade);

        //TODO:@vlbo - websocket
        getContext().getLog().info(String.format("TRADE transaction complete %s", trade.toString()));
        return this;
    }
}
