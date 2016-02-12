package com.ingemark.stream.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.stream.Stream;

@SuppressWarnings({"unchecked"})
public abstract class Util {
    public static final Logger logger = LoggerFactory.getLogger(Util.class);

    public static Properties props(String... data) {
        final Properties props = new Properties();
        for (int i = 0; i < data.length; ) props.put(data[i++], data[i++]);
        return props;
    }

    @SafeVarargs
    public static <K> List<K> l(K... data) {
        final List<K> retval = new ArrayList<>();
        retval.addAll(Arrays.asList(data));
        return retval;
    }

    @SafeVarargs
    public static <K> Set<K> s(K... data) {
        final Set<K> retval = new HashSet<>();
        retval.addAll(Arrays.asList(data));
        return retval;
    }

    public static <K, V> Map<K, V> m(K key, V val, Object... kvals) {
        final HashMap<K, V> m = new HashMap<>();
        m.put(key, val);
        for (int i = 0; i < kvals.length; ) m.put((K) kvals[i++], (V) kvals[i++]);
        return m;
    }

    public static <T> T spy(String msg, T x) {
        return spy(logger, msg, x);
    }

    public static <T> T spy(Logger log, String msg, T x) {
        if (log.isDebugEnabled()) log.debug(msg + ": '{}'", x);
        return x;
    }

    @SafeVarargs
    public static <K> K with(K o, Consumer<K>... cs) {
        if (o != null) Stream.of(cs).forEach(c -> c.accept(o));
        return o;
    }

    public static void loggingExc(String msg, RunnableExc r) {
        loggingExc(logger, msg, r);
    }

    public static void loggingExc(Logger log, String msg, RunnableExc r) {
        try {
            r.run();
        } catch (Exception e) {
            log.error(msg, e);
        }
    }

    public static <T> T uncheckCall(Callable<T> callable) {
        try {
            return callable.call();
        } catch (Exception e) {
            return sneakyThrow(e);
        }
    }

    public static void uncheckRun(RunnableExc r) {
        try {
            r.run();
        } catch (Exception e) {
            sneakyThrow(e);
        }
    }


    public static <T> T sneakyThrow(Throwable e) {
        return Util.<RuntimeException, T>sneakyThrow0(e);
    }

    private static <E extends Throwable, T> T sneakyThrow0(Throwable t) throws E {
        throw (E) t;
    }


    public interface RunnableExc {
        void run() throws Exception;
    }


}
