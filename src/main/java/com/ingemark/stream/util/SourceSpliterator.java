package com.ingemark.stream.util;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.Spliterators.spliterator;
import static java.util.stream.StreamSupport.stream;

public class SourceSpliterator implements Spliterator<Integer> {
    public static final int BATCH_SIZE = 100;
    private final RandomIntSource s;

    public SourceSpliterator(RandomIntSource s) {
        this.s = s;
    }

    public static Stream<Integer> sourceStream(RandomIntSource s) {
        return stream(new SourceSpliterator(s), true);
    }

    @Override
    public boolean tryAdvance(Consumer<? super Integer> action) {
        if (s.hasNext()) {
            action.accept(s.next());
            return true;
        }
        return false;
    }

    @Override
    public Spliterator<Integer> trySplit() {
        final HoldingConsumer<Integer> holder = new HoldingConsumer<>();
        if (!tryAdvance(holder)) return null;

        final Object[] a = new Object[BATCH_SIZE];
        int j = 0;
        do a[j] = holder.value;
        while (++j < BATCH_SIZE && tryAdvance(holder));
        return spliterator(a, 0, j, characteristics());
    }

    @Override
    public long estimateSize() {
        return Long.MAX_VALUE;
    }

    @Override
    public int characteristics() {
        return NONNULL | IMMUTABLE;
    }

    protected static final class HoldingConsumer<T> implements Consumer<T> {
        T value;

        @Override
        public void accept(T value) {
            this.value = value;
        }
    }

//    @Override
//    public void forEachRemaining(Consumer<? super Integer> action) {
//
//    }
//
//    @Override
//    public long getExactSizeIfKnown() {
//        return 0;
//    }
//
//    @Override
//    public boolean hasCharacteristics(int characteristics) {
//        return false;
//    }
//
//    @Override
//    public Comparator<? super Integer> getComparator() {
//        return null;
//    }
}
