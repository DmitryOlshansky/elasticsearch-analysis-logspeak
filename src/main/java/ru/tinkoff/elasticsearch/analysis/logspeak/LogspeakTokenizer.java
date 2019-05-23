package ru.tinkoff.elasticsearch.analysis.logspeak;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.util.AttributeFactory;
import ru.tinkoff.elasticsearch.analysis.logspeak.bitnfa.BitNfaTokenizer;

import java.io.IOException;
import java.util.Arrays;

public final class LogspeakTokenizer extends Tokenizer {
    static final int DEFAULT_MAX_TOKEN_LENGTH = 255;
    static final int MAX_TOKEN_LENGTH_LIMIT = 32*1024;

    private int maxTokenLength = DEFAULT_MAX_TOKEN_LENGTH;
    private BitNfaTokenizer splitter;

    // this tokenizer generates two attributes:
    // term and offset
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);

    public int getMaxTokenLength() { return maxTokenLength; }

    public final void setMaxTokenLength(int numChars) {
        if (numChars < 1) {
            throw new IllegalArgumentException("maxTokenLength must be greater than zero");
        } else if (numChars > MAX_TOKEN_LENGTH_LIMIT) {
            throw new IllegalArgumentException("maxTokenLength may not exceed " + MAX_TOKEN_LENGTH_LIMIT);
        }
        splitter.setBufferSize(numChars);
    }

    public LogspeakTokenizer() {
        super();
        splitter = new BitNfaTokenizer(input);
    }

    public LogspeakTokenizer(AttributeFactory factory) {
        super(factory);
        splitter = new BitNfaTokenizer(input);
    }

    @Override
    public final boolean incrementToken() throws IOException {
        clearAttributes();
        if (!splitter.nextToken()) return false;
        splitter.getText(termAtt);
        int cnt = splitter.getCounter();
        offsetAtt.setOffset(correctOffset(cnt), correctOffset(cnt+termAtt.length()));
        return true;
    }

    @Override
    public final void end() throws IOException {
        super.end();
        // set final offset
        int cnt = splitter.getCounter();
        offsetAtt.setOffset(correctOffset(cnt), correctOffset(cnt));
    }

    @Override
    public final void close() throws IOException {
        super.close();
        splitter.close();
    }

    @Override
    public final void reset() throws IOException {
        super.reset();
        splitter.reset(input);
    }
}
