package com.ingemark.stream.util;

import com.ingemark.stream.spliterators.FixedBatchSpliteratorBase;

import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.stream.StreamSupport.stream;

public class SourceSpliteratorEnhanced extends FixedBatchSpliteratorBase<Integer> {
    public static final int BATCH_SIZE = 100;
    private final RandomIntSource s;

    public SourceSpliteratorEnhanced(RandomIntSource s) {
        super(NONNULL | IMMUTABLE, BATCH_SIZE);
        this.s = s;
    }

    public static Stream<Integer> sourceStream(RandomIntSource s) {
        return stream(new SourceSpliteratorEnhanced(s), true);
    }

    @Override
    public boolean tryAdvance(Consumer<? super Integer> action) {
        if (s.hasNext()) {
            action.accept(s.next());
            return true;
        }
        return false;
    }
}
