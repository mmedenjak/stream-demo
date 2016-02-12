package com.ingemark.stream.spliterators;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.ScrollableResults;

import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.hibernate.ScrollMode.FORWARD_ONLY;

/**
 * Adapts Hibernate's {@code ScrollableResults} into a {@code Spliterator} which
 * implements {@link #trySplit} such that batches of configurable size are produced. Each
 * batch will be a strict prefix of this spliterator's remaining elements. The spliterator
 * is {@link #ORDERED} and {@link #NONNULL}.
 * <p/>
 * The type of elements produced by this spliterator is either {@code Object[]} for
 * results whose {@code get()} method returns an array of length above 1, or the type of
 * the sole member when the array is of length 1 (the element will be unwrapped). The type
 * conforming with the above logic must be passed into the constructor.
 * <p/>
 * This spliterator can be used to achieve good parallel speedup, provided that:
 * <ol>
 * <li>typical time taken to fetch one element from the underlying
 * {@code ScrollableResults} is negligible compared to the time needed to process it in
 * the stream pipeline;
 * <li>the batch size is appropriately chosen such that the processing of one batch takes
 * about 1 to 10 milliseconds (fetch time is not included in this).</li>
 * </ol>
 *
 * @param <T> The type of elements produced by this Spliterator
 * @author Marko Topolnik
 */
public class ScrollableResultsSpliterator<T> extends FixedBatchSpliteratorBase<T> {
    private final ScrollableResults results;
    private boolean closed;
    private Boolean canUnwrap;

    /**
     * Creates a spliterator from {@code ScrollableResults} obtained by invoking
     * {@code scroll(ScrollMode.FORWARD_ONLY)} on the supplied {@code Query}.
     * Spliterator's {@link #trySplit} method will split off its strict prefix with the
     * default batch size (defined by {@link #DEFAULT_BATCH_SIZE}).
     *
     * @param clazz the type of the elements which will be produced by the spliterator.
     * @param q the Hibernate query.
     */
    public ScrollableResultsSpliterator(Class<T> clazz, Query q) {
        this(clazz, q.scroll(FORWARD_ONLY));
    }

    /**
     * Creates a spliterator from {@code ScrollableResults} obtained by invoking
     * {@code scroll(ScrollMode.FORWARD_ONLY)} on the supplied {@code Criteria}.
     * Spliterator's {@link #trySplit} method will split off its strict prefix with the
     * default batch size (defined by {@link #DEFAULT_BATCH_SIZE}).
     *
     * @param clazz    the type of the elements which will be produced by the spliterator.
     * @param criteria the Hibernate criteria.
     */
    public ScrollableResultsSpliterator(Class<T> clazz, Criteria criteria) {
        this(clazz, criteria.scroll(FORWARD_ONLY));
    }

    /**
     * Creates a spliterator from Hibernate's {@code ScrollableResults}. Spliterator's
     * {@link #trySplit} method will split off its strict prefix with the default batch
     * size (defined by {@link #DEFAULT_BATCH_SIZE}). Callers should be careful in
     * choosing the scroll mode for the {@code ScrollableResults} because the JDBC driver
     * implementation may need to retrieve the entire result set for modes other than
     * {@code FORWARD_ONLY}.
     *
     * @param clazz   the type of the elements which will be produced by the spliterator.
     * @param results the Hibernate scrollable results.
     */
    public ScrollableResultsSpliterator(Class<T> clazz, ScrollableResults results) {
        this(clazz, DEFAULT_BATCH_SIZE, results);
    }

    /**
     * Creates a spliterator from {@code ScrollableResults} obtained by invoking
     * {@code scroll(ScrollMode.FORWARD_ONLY)} on the supplied {@code Query}.
     * Spliterator's {@link #trySplit} method will split off its strict prefix with the
     * given batch size.
     *
     * @param clazz     the type of the elements which will be produced by the spliterator.
     * @param batchSize this spliterator's batch size.
     * @param query     the Hibernate query.
     */
    public ScrollableResultsSpliterator(Class<T> clazz, int batchSize, Query query) {
        this(clazz, batchSize, query.scroll(FORWARD_ONLY));
    }

    /**
     * Creates a spliterator from {@code ScrollableResults} obtained by invoking
     * {@code scroll(ScrollMode.FORWARD_ONLY)} on the supplied {@code Criteria}.
     * Spliterator's {@link #trySplit} method will split off its strict prefix with the
     * given batch size.
     *
     * @param clazz     the type of the elements which will be produced by the spliterator.
     * @param batchSize this spliterator's batch size.
     * @param criteria  the Hibernate criteria.
     */
    public ScrollableResultsSpliterator(Class<T> clazz, int batchSize, Criteria criteria) {
        this(clazz, batchSize, criteria.scroll(FORWARD_ONLY));
    }

    /**
     * Creates a spliterator from Hibernate's {@code ScrollableResults}. Spliterator's
     * {@link #trySplit} method will split off its strict prefix with the given batch size.
     * Callers should be careful in choosing the scroll mode for {@code ScrollableResults}
     * because the JDBC driver implementation may need to retrieve the entire result set
     * for modes other than {@code FORWARD_ONLY}.
     *
     * @param clazz     the type of the elements which will be produced by the spliterator.
     * @param batchSize this spliterator's batch size.
     * @param results   the Hibernate scrollable results.
     */
    public ScrollableResultsSpliterator(Class<T> clazz, int batchSize, ScrollableResults results) {
        super(ORDERED | NONNULL, batchSize);
        if (results == null) throw new NullPointerException("ScrollableResults must not be null");
        this.results = results;
    }

    /**
     * {@inheritDoc}
     *
     * @throws ClassCastException if the type of element derived from the invocation of
     *                            {@code ScrollableResults.get()} (as explained in the class-level
     *                            documentation above) cannot be cast into the type passed to the
     *                            constructor.
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        if (closed) return false;
        if (!results.next()) {
            close();
            return false;
        }
        if (canUnwrap == null) {
            final Object[] r = results.get();
            canUnwrap = r.length == 1;
            action.accept((T) (canUnwrap ? r[0] : r));
        } else action.accept((T) (canUnwrap ? results.get(0) : results.get()));
        return true;
    }

    /**
     * Closes the underlying {@code ScrollableResults}, releasing all JDBC resources it
     * has acquired. After this method is called, {@code tryAdvance} will always return
     * {@code false}.
     */
    public void close() {
        if (!closed) {
            results.close();
            closed = true;
        }
    }

    /**
     * Builds a {@code Stream} backed by {@code ScrollableResults} obtained from the given
     * {@code Query}. Convenience around
     * {@link #ScrollableResultsSpliterator(Class, Query)}. Closing the stream will
     * propagate to the underlying {@code ScrollableResults}.
     *
     * @param clazz the type of the stream elements.
     * @param query the Hibernate query.
     * @return a {@code Stream} of elements returned by the query.
     */
    public static <T> Stream<T> resultStream(Class<T> clazz, Query query) {
        return resultStream(new ScrollableResultsSpliterator<>(clazz, query));
    }

    /**
     * Builds a {@code Stream} backed by {@code ScrollableResults} obtained from the given
     * {@code Criteria}. Convenience around
     * {@link #ScrollableResultsSpliterator(Class, Criteria)}. Closing the stream will
     * propagate to the underlying {@code ScrollableResults}.
     *
     * @param clazz    the type of the stream elements.
     * @param criteria the Hibernate criteria.
     * @return a {@code Stream} of elements returned by the criteria.
     */
    public static <T> Stream<T> resultStream(Class<T> clazz, Criteria criteria) {
        return resultStream(new ScrollableResultsSpliterator<T>(clazz, criteria));
    }

    /**
     * Builds a {@code Stream} backed by the supplied {@code ScrollableResults}.
     * Convenience around {@link #ScrollableResultsSpliterator(Class, ScrollableResults)}.
     * Closing the stream will propagate to the underlying {@code ScrollableResults}.
     *
     * @param clazz   the type of the stream elements.
     * @param results the Hibernate scrollable results.
     * @return a {@code Stream} of elements returned by the scrollable results.
     */
    public static <T> Stream<T> resultStream(Class<T> clazz, ScrollableResults results) {
        return resultStream(new ScrollableResultsSpliterator<T>(clazz, results));
    }

    /**
     * Builds a {@code Stream} backed by {@code ScrollableResults} obtained from the given
     * {@code Query}. Convenience around
     * {@link #ScrollableResultsSpliterator(Class, int, Query)}. Closing the stream will
     * propagate to the underlying {@code ScrollableResults}.
     *
     * @param clazz     the type of the stream elements.
     * @param batchSize the underlying spliterator's batch size.
     * @param query     the Hibernate query.
     * @return a {@code Stream} of elements returned by the query.
     */
    public static <T> Stream<T> resultStream(Class<T> clazz, int batchSize, Query query) {
        return resultStream(new ScrollableResultsSpliterator<T>(clazz, batchSize, query));
    }

    /**
     * Builds a {@code Stream} backed by {@code ScrollableResults} obtained from the given
     * {@code Criteria}. Convenience around
     * {@link #ScrollableResultsSpliterator(Class, int, Criteria)}. Closing the stream
     * will propagate to the underlying {@code ScrollableResults}.
     *
     * @param clazz     the type of the stream elements.
     * @param batchSize the underlying spliterator's batch size.
     * @param criteria  the Hibernate criteria.
     * @return a {@code Stream} of elements returned by the criteria.
     */
    public static <T> Stream<T> resultStream(Class<T> clazz, int batchSize, Criteria criteria) {
        return resultStream(new ScrollableResultsSpliterator<T>(clazz, batchSize, criteria));
    }

    /**
     * Builds a {@code Stream} backed by the supplied {@code ScrollableResults}.
     * Convenience around
     * {@link #ScrollableResultsSpliterator(Class, int, ScrollableResults)}. Closing the
     * stream will propagate to the underlying {@code ScrollableResults}.
     *
     * @param clazz     the type of the stream elements.
     * @param batchSize the underlying spliterator's batch size.
     * @param results   the Hibernate scrollable results.
     * @return a {@code Stream} of elements returned by the scrollable results.
     */
    public static <T> Stream<T> resultStream(Class<T> clazz, int batchSize, ScrollableResults results) {
        return resultStream(new ScrollableResultsSpliterator<T>(clazz, batchSize, results));
    }

    /**
     * Returns an initially sequential {@code Stream} based on the supplied scrollable
     * results spliterator. Closing the stream will invoke {@link #close} on the
     * spliterator.
     *
     * @param spliterator the spliterator.
     * @return a {@code Stream} of elements produced by the spliterator.
     */
    public static <T> Stream<T> resultStream(ScrollableResultsSpliterator<T> spliterator) {
        return StreamSupport.stream(spliterator, false).onClose(spliterator::close);
    }
}
