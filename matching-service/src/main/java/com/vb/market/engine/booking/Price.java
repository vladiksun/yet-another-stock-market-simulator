package com.vb.market.engine.booking;

import com.vb.market.domain.Side;

import java.util.Comparator;

public class Price implements Comparable<Price> {

    private Integer value;

    public Price(Integer value) {
        this.value = value;
    }

    @Override
    public int compareTo(Price other) {
        return value.compareTo(other.value);
    }
}
