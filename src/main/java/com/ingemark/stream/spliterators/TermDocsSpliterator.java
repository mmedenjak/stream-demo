package com.ingemark.stream.spliterators;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.DocIdSetIterator;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class TermDocsSpliterator extends FixedBatchSpliteratorBase<Document> {
    private final PostingsEnum docs;
    private final IndexReader reader;
    private final Set<String> fieldsToLoad;

    public TermDocsSpliterator(IndexReader reader, TermsEnum termsEnum, Set<String> fieldsToLoad) {
        super(ORDERED | NONNULL | DISTINCT, 64, Integer.MAX_VALUE);
        try {
            this.docs = termsEnum.postings(null);
            this.reader = reader;
            this.fieldsToLoad = fieldsToLoad;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static Stream<Document> termDocs(IndexReader reader, TermsEnum termsEnum, Set<String> fieldsToLoad) {
        return StreamSupport.stream(new TermDocsSpliterator(reader, termsEnum, fieldsToLoad), false);
    }

    @Override
    public long estimateSize() {
        return super.estimateSize();
    }

    @Override
    public boolean tryAdvance(Consumer<? super Document> action) {
        try {
            int docId;
            if ((docId = docs.nextDoc()) == DocIdSetIterator.NO_MORE_DOCS) return false;
            action.accept(reader.document(docId, fieldsToLoad));
            return true;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
