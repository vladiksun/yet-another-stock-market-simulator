package com.vb.market.utils;

import java.util.concurrent.atomic.AtomicLong;

public class IdGen {

    private AtomicLong id;

    public IdGen() {
        id = new AtomicLong(0);
    }

    public Long getID() {
        return id.incrementAndGet();
    }
}
