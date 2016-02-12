package com.ingemark.stream.spliterators;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class PartitionBySpliterator<E> extends AbstractSpliterator<List<E>> {
    private final Spliterator<E> spliterator;
    private final Function<? super E, ?> partitionBy;
    private HoldingConsumer<E> holder;

    public PartitionBySpliterator(Spliterator<E> toWrap, Function<? super E, ?> partitionBy) {
        super(Long.MAX_VALUE, toWrap.characteristics());
        this.spliterator = toWrap;
        this.partitionBy = partitionBy;
    }

    public static <E> Stream<List<E>> partitionBy(Function<E, ?> partitionBy, Stream<E> in) {
        return StreamSupport.stream(new PartitionBySpliterator<>(in.spliterator(), partitionBy), false);
    }

    @Override
    public boolean tryAdvance(Consumer<? super List<E>> action) {
        final HoldingConsumer<E> h;
        if (holder == null) {
            h = new HoldingConsumer<>();
            if (!spliterator.tryAdvance(h)) return false;
            holder = h;
        } else h = holder;
        final ArrayList<E> partition = new ArrayList<>();
        final Object partitionKey = partitionBy.apply(h.value);
        boolean didAdvance;
        do partition.add(h.value);
        while ((didAdvance = spliterator.tryAdvance(h))
                && Objects.equals(partitionBy.apply(h.value), partitionKey));
        if (!didAdvance) holder = null;
        action.accept(partition);
        return true;
    }

    static final class HoldingConsumer<T> implements Consumer<T> {
        T value;

        @Override
        public void accept(T value) {
            this.value = value;
        }
    }
}
