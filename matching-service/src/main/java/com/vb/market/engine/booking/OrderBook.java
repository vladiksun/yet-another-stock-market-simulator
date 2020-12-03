package com.vb.market.engine.booking;

import com.vb.market.domain.Side;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

/***
 *         Books must sort data according to Price/Time priority (FIFO)
 *
 *         * All orders at the same price level are filled according to time priority;
 *         * The first order at a price level is the first order matched.
 *
 *         The order for sorting by time is ascending for buy-side orders and descending for sell-side orders,
 *         so that the order with the highest priority is always in the center and priorities decrease
 *         outwards (up or down, depending on the side).
 *
 *         Orders are sorted with respect to price and when they have the same price
 *         they are sorted by their arrival time at the exchange
 *
 *         Input:
 *         Id   Side    Time   Qty   Price   Qty    Time   Side
 *         ---+------+-------+-----+-------+-----+-------+------
 *         #3                        23     200   09:05   SELL
 *         #1                        23     100   09:01   SELL
 *         #2                        22     100   09:03   SELL
 *         #5   BUY    09:08   200   21
 *         #4   BUY    09:06   100   20
 *         #6   BUY    09:09   200   20
 *
 *         New order comes: BUY 250 shares @ 24 ---> leads to result
 *         Id   Side    Time   Qty   Price   Qty    Time   Side
 *         ---+------+-------+-----+-------+-----+-------+------
 *         #3                        23     200   09:05   SELL
 *         #1                        23     100   09:01   SELL
 *         #2                        22     100   09:03   SELL
 *         #5   BUY    09:08   200   21
 *         #4   BUY    09:06   100   20
 *         #6   BUY    09:09   200   20
 *
 *         New order comes: BUY 250 shares @ 24 ---> leads to result
 *         Id   Side    Time   Qty   Price   Qty    Time   Side
 *         ---+------+-------+-----+-------+-----+-------+------
 *         #3                        23     200   09:05   SELL
 *         #1                        23     100   09:01   SELL
 *         #2                        22     100   09:03   SELL
 *         #7   BUY    09:12   250   24 <---------------------- !!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 *         #5   BUY    09:08   200   21
 *         #4   BUY    09:06   100   20
 *         #6   BUY    09:09   200   20
 *
 *          1) 100 shares @ 22
 *          2) 100 shares @ 23
 *          3) 50  shares @ 23
 *
 *          Id   Side    Time   Qty   Price   Qty    Time   Side
 *         ---+------+-------+-----+-------+-----+-------+------
 *         #3                        23     150   09:05   SELL
 *
 *         [ ************** fills out of the book ****************]
 *         #3                        23     50    09:05   SELL
 *         #1                        23     100   09:01   SELL
 *         #2                        22     100   09:03   SELL
 *         #7   BUY    09:12   250   24
 *         [ ************** fills out of the book ****************]
 *
 *         #5   BUY    09:08   200   21
 *         #4   BUY    09:06   100   20
 *         #6   BUY    09:09   200   20
 */

public class OrderBook {

    private Map<KeyPriority, BookEntry> limitBook;

    public OrderBook(Side side) {
        limitBook = new TreeMap<>(PriceComparator.of(side)
                                    .thenComparing(new EarliestSubmittedTimeFirst()));
    }

    static class KeyPriority {

        private Price price;
        private Long submittedTime;

        public KeyPriority(Price price, Long submittedTime) {
            this.price = price;
            this.submittedTime = submittedTime;
        }

        public Price getPrice() {
            return price;
        }

        public Long getSubmittedTime() {
            return submittedTime;
        }
    }

    static class BookEntry {
        private KeyPriority key;
        private Side side;
        private Integer quantity;
    }

    static class PriceComparator implements Comparator<KeyPriority> {
        private Side side;

        private PriceComparator(Side side) {
            this.side = side;
        }

        static PriceComparator of(Side side) {
            return new PriceComparator(side);
        }

        @Override
        public int compare(KeyPriority o1, KeyPriority o2) {
            return side.comparatorParameter() * o1.getPrice().compareTo(o2.getPrice());
        }
    }

    static class EarliestSubmittedTimeFirst implements Comparator<KeyPriority> {

        @Override
        public int compare(KeyPriority o1, KeyPriority o2) {
            return o1.getSubmittedTime().compareTo(o2.submittedTime);
        }
    }


}
