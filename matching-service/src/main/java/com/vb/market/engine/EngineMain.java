package com.vb.market.engine;

import akka.actor.typed.ActorSystem;

import java.io.IOException;

public class EngineMain {

    public static void main(String[] args) {
        ActorSystem<MatchingManagerActor.Command> commandActorSystem =
                ActorSystem.create(MatchingManagerActor.create(), "stock-market-system");

        try {
            System.out.println(">>> Press ENTER to exit <<<");
            System.in.read();
        } catch (IOException ignored) {
            commandActorSystem.terminate();
        }
    }


}
