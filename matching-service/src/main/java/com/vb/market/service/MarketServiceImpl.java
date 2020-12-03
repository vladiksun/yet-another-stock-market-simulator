package com.vb.market.service;

import akka.actor.typed.ActorRef;
import com.vb.market.engine.MatchingManager;
import org.springframework.stereotype.Service;

@Service
public class MarketServiceImpl implements MarketService {

    private ActorRef<MatchingManager.Command> matchingActor;

    public MarketServiceImpl(ActorRef<MatchingManager.Command> matchingActor) {
        this.matchingActor = matchingActor;
    }
}
