package ru.tinkoff.elasticsearch.analysis.logspeak;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.util.AttributeFactory;
import ru.tinkoff.elasticsearch.analysis.logspeak.bitnfa.BitNfaSplitter;

import java.io.IOException;
import java.util.Arrays;

public final class LogspeakTokenizer extends Tokenizer {
    static final int DEFAULT_MAX_TOKEN_LENGTH = 255;
    static final int MAX_TOKEN_LENGTH_LIMIT = 32*1024;

    private int maxTokenLength = DEFAULT_MAX_TOKEN_LENGTH;
    private BitNfaSplitter splitter = new BitNfaSplitter();

    private char[] buffer = new char[DEFAULT_MAX_TOKEN_LENGTH];
    private boolean firstBreak = true;
    private int offset = 0; // processed up to this index in the buffer
    private int mark = 0; // pinned point
    private int available = 0; // portion of buffer that is read from reader

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
        buffer = Arrays.copyOf(buffer, numChars);
    }

    public LogspeakTokenizer() {
        super();
    }

    public LogspeakTokenizer(AttributeFactory factory) {
        super(factory);
    }

    @Override
    public final boolean incrementToken() throws IOException {
        clearAttributes();
        int end;
        mark = scan();
        end = scan();
        if (mark == end) return false;
        termAtt.copyBuffer(buffer, mark, end - mark);
        offsetAtt.setOffset(correctOffset(mark), correctOffset(mark+termAtt.length()));
        return true;
    }

    // scan input to the next break point
    public final int scan() throws IOException {
        offset = firstBreak ? splitter.firstBreak(buffer, offset, available) : splitter.nextBreak(buffer, offset, available);
        firstBreak = false;
        if (offset == available) { // continue after refilling the buffer
            boolean eof = refill();
            offset = splitter.continueFind(buffer, offset, available); // this may be == available, truncated token
            if (eof) return offset - splitter.chopAtEof();
        }
        return offset;
    }

    private boolean refill() throws IOException {
        int pinned = available - mark;
        if (pinned > 0) System.arraycopy(buffer, mark, buffer, 0, pinned);
        int read = input.read(buffer, pinned, buffer.length - pinned);
        if (read > 0) {
            offset -= mark;
            mark = 0;
            available = pinned + read;
            return false;
        }
        else return true;
    }

    @Override
    public void end() throws IOException {
        super.end();
        // set final offset
        offsetAtt.setOffset(correctOffset(offset), correctOffset(offset));
    }

    @Override
    public void close() throws IOException {
        super.close();
        firstBreak = true;
        offset = 0;
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        firstBreak = true;
        offset = 0;
    }
}
