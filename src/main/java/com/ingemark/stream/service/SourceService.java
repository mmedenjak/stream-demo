package com.ingemark.stream.service;

import com.ingemark.stream.spliterators.PartitionBySpliterator;
import com.ingemark.stream.spliterators.PartitioningSpliterator;
import com.ingemark.stream.spliterators.RegexSpliterator;
import com.ingemark.stream.spliterators.ZippingSpliterator;
import com.ingemark.stream.util.RandomIntSource;
import com.ingemark.stream.util.SourceIterable;
import com.ingemark.stream.util.SourceSpliterator;
import com.ingemark.stream.util.SourceSpliteratorEnhanced;

import java.util.Arrays;
import java.util.Spliterator;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class SourceService {

    public static void main(String[] args) {
        System.out.println(whileIteration(new RandomIntSource()));
        System.out.println(forIteration(new RandomIntSource()));
        System.out.println(enhancedForLoop(new RandomIntSource()));
        System.out.println(iterableStream(new RandomIntSource()));
        System.out.println(stream(new RandomIntSource()));
        System.out.println(streamEnhanced(new RandomIntSource()));
        // Producing on thread demo

        regexSpliterator();
        partitioningSpliterator();
        partitioningBySpliterator();
        zippingSpliterator();
    }

    private static void zippingSpliterator() {
        final Stream<Integer> ints = SourceSpliteratorEnhanced.sourceStream(new RandomIntSource());
        final Stream<Character> chars = Stream.iterate('A', ch -> ch >= 'Z' ? 'A' : ++ch);
        ZippingSpliterator.zip(ints, chars).forEach(arr -> System.out.println(Arrays.toString(arr)));
    }

    private static void partitioningSpliterator() {
        final Stream<Integer> stream = SourceSpliteratorEnhanced.sourceStream(new RandomIntSource());
        PartitioningSpliterator.partition(stream, 20).forEach(System.out::println);
    }

    private static void partitioningBySpliterator() {
        final Stream<Integer> stream = SourceSpliteratorEnhanced.sourceStream(new RandomIntSource());
        PartitionBySpliterator.partitionBy(i -> i % 2 == 0, stream).forEach(System.out::println);
    }

    private static void regexSpliterator() {
        final Pattern p = Pattern.compile("\\w");
        RegexSpliterator.resultStream(new RegexSpliterator(p, "Inge-mark")).forEach(System.out::println);
    }

    public static long whileIteration(RandomIntSource s) {
        long sum = 0;

        while (s.hasNext()) {
            sum += s.next();
        }
        return sum;
    }

    public static long forIteration(RandomIntSource s) {
        long sum = 0;
        for (; s.hasNext(); ) {
            sum += s.next();
        }
        return sum;
    }

    public static long enhancedForLoop(RandomIntSource s) {
        long sum = 0;
        for (Integer i : new SourceIterable(s)) {
            sum += i;
        }
        return sum;
    }

    public static long iterableStream(RandomIntSource s) {
        final Spliterator<Integer> spliterator = new SourceIterable(s).spliterator();
        return StreamSupport.stream(spliterator, false).collect(Collectors.summingLong(Integer::longValue));
    }

    public static long stream(RandomIntSource s) {
        return SourceSpliterator.sourceStream(s).collect(Collectors.summingLong(Integer::longValue));
    }

    public static long streamEnhanced(RandomIntSource s) {
        return SourceSpliteratorEnhanced.sourceStream(s)
                .map(i -> {
//                    System.out.println("Mapping on " + Thread.currentThread());
//                    Util.uncheckRun(() -> Thread.sleep(10));
                    return i;
                })
                .collect(Collectors.summingLong(Integer::longValue));
    }

}
