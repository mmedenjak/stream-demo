package com.ingemark.stream.util;

import java.util.Random;

public class RandomIntSource {
    private final int[] source;
    private int currentIdx = 0;

    public RandomIntSource() {
        final Random rnd = new Random();
        this.source = rnd.ints(rnd.nextInt(10_000), 0, 10).toArray();
    }

    public boolean hasNext() {
        return source.length > currentIdx;
    }

    public int next() {
//        System.out.println("Producing on " + Thread.currentThread());
//        Util.uncheckRun(() -> Thread.sleep(20));
        return source[currentIdx++];
    }
}
