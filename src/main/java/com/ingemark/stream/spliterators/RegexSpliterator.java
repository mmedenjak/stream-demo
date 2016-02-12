package com.ingemark.stream.spliterators;

import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class RegexSpliterator extends FixedBatchSpliteratorBase<String> {
    private final Matcher matcher;

    public RegexSpliterator(Pattern p, String s) {
        super(ORDERED | NONNULL | IMMUTABLE, 64, Integer.MAX_VALUE);
        this.matcher = p.matcher(s);
    }

    public static Stream<String> resultStream(RegexSpliterator spliterator) {
        return StreamSupport.stream(spliterator, false);
    }

    @Override
    public boolean tryAdvance(Consumer<? super String> action) {
        if (!matcher.find()) return false;
        action.accept(matcher.group());
        return true;
    }


}
