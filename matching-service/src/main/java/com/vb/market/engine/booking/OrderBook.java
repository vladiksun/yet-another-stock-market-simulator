package com.vb.market.engine.booking;

import com.vb.market.domain.CancelOrderRequest;
import com.vb.market.domain.PlaceOrderRequest;
import com.vb.market.domain.Side;
import com.vb.market.engine.MatchingManagerActor.CancelOrderReply;
import com.vb.market.engine.MatchingManagerActor.PlaceOrderReply;

import java.time.Instant;
import java.util.*;

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

    private NavigableMap<KeyPriority, BookEntry> limitBook;

    private Side side;

    private Comparator<KeyPriority> comparator;

    public OrderBook(Side side) {
        this.side = side;
        this.comparator = PriceComparator.of(side)
                                            .thenComparing(new EarliestSubmittedTimeFirst())
                                            .thenComparing(new EarliestEventFirst());
        this.limitBook = new TreeMap<>(comparator);
    }

    public PlaceOrderReply addOrder(PlaceOrderRequest request) {
        KeyPriority keyPriority = new KeyPriority(request.getEventId(), request.getPrice(), request.getSubmittedTime());
        BookEntry bookEntry = new BookEntry(keyPriority, side, request.getQuantity());
        limitBook.put(keyPriority, bookEntry);

        return new PlaceOrderReply(request);
    }

    public Optional<CancelOrderReply> cancelOrder(CancelOrderRequest cancelOrderRequest) {
        Long eventId = cancelOrderRequest.getEventId();

        Optional<KeyPriority> first = limitBook.keySet().stream()
                .filter(keyPriority -> keyPriority.eventId.equals(eventId))
                .findFirst();

        if (first.isPresent()) {
            KeyPriority keyPriority = first.get();
            limitBook.remove(keyPriority);
            return Optional.of(new CancelOrderReply(cancelOrderRequest));
        } else {
            return Optional.empty();
        }
    }

    public Map<KeyPriority, BookEntry> getBookEntries(int limit) {
        Map<KeyPriority, BookEntry> collect = limitBook.entrySet().stream()
                .limit(limit)
                .collect(() -> new TreeMap<>(comparator),
                        (m, e) -> m.put(e.getKey(), e.getValue()), Map::putAll);
        return collect;
    }

    public Map<KeyPriority, BookEntry> getBookEntries() {
        return limitBook;
    }



    public static class KeyPriority {
        private Long eventId;
        private Integer price;
        private Instant submittedTime;

        public KeyPriority(Long eventId, Integer price, Instant submittedTime) {
            this.eventId = eventId;
            this.price = price;
            this.submittedTime = submittedTime;
        }

        public Integer getPrice() {
            return price;
        }

        public Instant getSubmittedTime() {
            return submittedTime;
        }

        public Long getEventId() {
            return eventId;
        }

        @Override
        public String toString() {
            return "KeyPriority{" +
                    "eventId=" + eventId +
                    ", price=" + price +
                    ", submittedTime=" + submittedTime +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            KeyPriority that = (KeyPriority) o;
            return eventId.equals(that.eventId) &&
                    price.equals(that.price) &&
                    submittedTime.equals(that.submittedTime);
        }

        @Override
        public int hashCode() {
            return Objects.hash(eventId, price, submittedTime);
        }
    }

    public static class BookEntry {
        private KeyPriority key;
        private Side side;
        private Integer quantity;

        public BookEntry(KeyPriority key, Side side, Integer quantity) {
            this.key = key;
            this.side = side;
            this.quantity = quantity;
        }

        public boolean isFullyFilled() {
            return quantity == 0;
        }

        public KeyPriority getKey() {
            return key;
        }

        public Side getSide() {
            return side;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setKey(KeyPriority key) {
            this.key = key;
        }

        public void setSide(Side side) {
            this.side = side;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
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

    static class EarliestEventFirst implements Comparator<KeyPriority> {

        @Override
        public int compare(KeyPriority o1, KeyPriority o2) {
            return o1.getEventId().compareTo(o2.getEventId());
        }
    }

}
