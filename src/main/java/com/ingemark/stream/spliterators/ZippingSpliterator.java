package com.ingemark.stream.spliterators;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ZippingSpliterator extends FixedBatchSpliteratorBase<Object[]> {

    private final Spliterator<?>[] spliterators;

    public ZippingSpliterator(Spliterator<?>... spliterators) {
        super(ORDERED, 64, Integer.MAX_VALUE);
        this.spliterators = spliterators;
    }

    public static Stream<Object[]> zip(Stream<?>... streams) {
        @SuppressWarnings("unchecked")
        final Spliterator<?>[] splits = Stream.of(streams).map(Stream::spliterator).toArray(Spliterator[]::new);
        return StreamSupport.stream(new ZippingSpliterator(splits), false);
    }

    @Override
    public boolean tryAdvance(Consumer<? super Object[]> action) {
        final Object[] subSpliteratorResults = new Object[spliterators.length];
        final HoldingConsumer<Object> holder = new HoldingConsumer<>();

        for (int i = 0; i < spliterators.length; i++) {
            final boolean ok = spliterators[i].tryAdvance(holder);
            if (!ok)
                return false;
            else
                subSpliteratorResults[i] = holder.value;
        }
        action.accept(subSpliteratorResults);
        return true;
    }


}
