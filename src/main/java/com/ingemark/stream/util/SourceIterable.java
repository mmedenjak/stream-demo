package com.ingemark.stream.util;

import java.util.Iterator;

public class SourceIterable implements Iterable<Integer> {
    private final RandomIntSource s;

    public SourceIterable(RandomIntSource s) {
        this.s = s;
    }

    @Override
    public Iterator<Integer> iterator() {
        return new Iterator<Integer>() {
            @Override
            public boolean hasNext() {
                return s.hasNext();
            }

            @Override
            public Integer next() {
                return s.next();
            }
        };
    }
}
