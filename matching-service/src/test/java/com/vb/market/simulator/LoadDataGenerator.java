package com.vb.market.simulator;

import org.apache.commons.lang3.RandomUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LoadDataGenerator {

    public static void main(String[] args) throws FileNotFoundException {

        new LoadDataGenerator().generateLoadData();
    }

    public void generateLoadData() throws FileNotFoundException {
        File csvOutputFile = new File("loadData.csv");

        List<String> users = Arrays.asList("Vlad", "Tom", "Sara");
        List<String> symbols = Arrays.asList("AAA", "BBB", "CCC", "FFF", "EEE", "TTT");
        List<String> sides = Arrays.asList("BUY", "SELL");

        List<String> data = IntStream.range(0, 1000).mapToObj(value -> {
            String clientId = users.get(RandomUtils.nextInt(0, 3));
            String symbol = symbols.get(RandomUtils.nextInt(0, 6));
            String side = sides.get(RandomUtils.nextInt(0, 2));

            int price = RandomUtils.nextInt(20, 22);
            int volume = RandomUtils.nextInt(10, 20);

            return clientId + "," +
                    symbol + "," +
                    price + "," +
                    volume + "," +
                    side;
        }).collect(Collectors.toList());

        try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
            data.forEach(pw::println);
        }
    }


}
