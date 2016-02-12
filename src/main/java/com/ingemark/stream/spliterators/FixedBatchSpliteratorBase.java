package com.ingemark.stream.spliterators;

import java.util.Comparator;
import java.util.Spliterator;
import java.util.function.Consumer;

import static java.util.Spliterators.spliterator;

/**
 * An abstract {@code Spliterator} that implements {@link #trySplit} such that batches of
 * configurable size are produced. Each batch will be a strict prefix of this
 * spliterator's remaining elements.
 * <p/>
 * An extending class need only implement {@link #tryAdvance(Consumer)
 * tryAdvance}. The extending class may also override
 * {@link #forEachRemaining(Consumer) forEach} if it can provide a more
 * performant implementation.
 *
 * @param <T> the type of elements produced by this spliterator
 * @author Marko Topolnik
 * @apiNote This class is a useful aid for creating spliterators from I/O-based or other
 * sequential data sources. The sequential nature of the source precludes
 * ballanced parallel computation; however, good parallelism can be achieved by
 * providing a batch size such that the processing of one batch takes 1-10
 * milliseconds. Fetching the data from the spliterator is an essentially
 * sequential operation, therefore parallelization can help performance only as
 * long as the time to fetch an element is negligible compared to the CPU time
 * needed to process it divided by the level of parallelism.
 */
public abstract class FixedBatchSpliteratorBase<T> implements Spliterator<T> {
    public static final int DEFAULT_BATCH_SIZE = 64;
    private final int batchSize;
    private final int characteristics;
    private long est;

    /**
     * Creates a spliterator which will split off its strict prefix in batches of the
     * given size and report the given characteristics and estimated size.
     *
     * @param additionalCharacteristics properties of this spliterator's source of
     *                                  elements. If {@link #SIZED} is reported then this spliterator will
     *                                  additionally report {@link #SUBSIZED}.
     * @param batchSize                 the size of the batches which will be split off this spliterator
     * @param est                       the estimated size of this spliterator if known, otherwise
     *                                  {@code Long.MAX_VALUE}.
     */
    protected FixedBatchSpliteratorBase(int additionalCharacteristics, int batchSize, long est) {
        this.characteristics = ((additionalCharacteristics & SIZED) != 0) ?
                additionalCharacteristics | SUBSIZED : additionalCharacteristics;
        this.batchSize = batchSize;
        this.est = est;
    }

    /**
     * Creates a spliterator of unknown size which will split off its strict prefix in
     * batches of the given size and report the given characteristics.
     *
     * @param characteristics properties of this spliterator's source of
     *                                  elements. If {@link #SIZED} is reported then this spliterator will
     *                                  additionally report {@link #SUBSIZED}.
     * @param batchSize                 the size of the batches which will be split off this spliterator
     */
    protected FixedBatchSpliteratorBase(int characteristics, int batchSize) {
        this(characteristics, batchSize, Long.MAX_VALUE);
    }

    /**
     * Creates a spliterator of unknown size which will split off its strict prefix in
     * batches of {@link #DEFAULT_BATCH_SIZE} and report the given characteristics.
     *
     * @param characteristics properties of this spliterator's source of
     *                                  elements. If {@link #SIZED} is reported then this spliterator will
     *                                  additionally report {@link #SUBSIZED}.
     */
    protected FixedBatchSpliteratorBase(int characteristics) {
        this(characteristics, 64, Long.MAX_VALUE);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * This implementation permits good parallel speedup provided that:
     * <ol>
     * <li>time taken by {@code tryAdvance} to fetch one element is negligible compared to
     * the time needed to process it in the stream pipeline divided by the level of
     * parallelism;
     * <li>the batch size is appropriately chosen such that the processing of one batch
     * takes about 1 to 10 milliseconds.</li>
     * </ol>
     */
    @Override
    public Spliterator<T> trySplit() {
        final HoldingConsumer<T> holder = new HoldingConsumer<>();
        if (!tryAdvance(holder)) return null;
        final Object[] a = new Object[batchSize];
        int j = 0;
        do a[j] = holder.value;
        while (++j < batchSize && tryAdvance(holder));
        if (est != Long.MAX_VALUE) est -= j;
        return spliterator(a, 0, j, characteristics());
    }

    @Override
    public Comparator<? super T> getComparator() {
        if (hasCharacteristics(SORTED)) return null;
        throw new IllegalStateException();
    }

    /**
     * {@inheritDoc}
     *
     * @implSpec This implementation returns the estimated size as reported when created
     * and, if the estimated size is known, decreases in size when split.
     */
    @Override
    public long estimateSize() {
        return est;
    }

    /**
     * {@inheritDoc}
     *
     * @implSpec This implementation returns the characteristics as reported when created.
     */
    @Override
    public int characteristics() {
        return characteristics;
    }

    protected static final class HoldingConsumer<T> implements Consumer<T> {
        T value;

        @Override
        public void accept(T value) {
            this.value = value;
        }
    }
}
