package com.vb.market.domain;

public enum Side {
    BUY {
        @Override
        public int comparatorParameter() {
            return HIGHEST_BUY_PRICE_FIRST;
        }
    },
    SELL {
        @Override
        public int comparatorParameter() {
            return LOWEST_SELL_PRICE_FIRST;
        }
    };

    public int HIGHEST_BUY_PRICE_FIRST = -1;
    public int LOWEST_SELL_PRICE_FIRST = 1;

    public abstract int comparatorParameter();
}
