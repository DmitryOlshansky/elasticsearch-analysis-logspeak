package ru.tinkoff.elasticsearch.analysis.logspeak.bitnfa;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Array;
import java.util.Arrays;

import static ru.tinkoff.elasticsearch.analysis.logspeak.bitnfa.BitNfaConstants.*;

public final class BitNfaTokenizer {
    private Reader reader;
    private char[] buffer = new char[255];
    private short state;
    private int offset;
    private int counter;
    private int left;
    private int right;
    private boolean overlong;
    private int available;
    private boolean eof;

    public BitNfaTokenizer(Reader input){
        reader = input;
    }

    public final void reset(Reader input) {
        reader = input;
        state = advance(' ', state);
        offset = 0;
        counter = 0;
        available = 0;
        overlong = false;
        eof = false;
    }

    public final void setBufferSize(int size) {
        buffer = Arrays.copyOf(buffer, size);
    }

    public final void close() throws IOException {
        eof = true;
        reader.close();
    }

    boolean refill() throws IOException {
        if (left == -1 || overlong) {
            offset = 0;
            left = -1;
            available = 0;
            overlong = false;
        }
        else if (left > 0) {
            System.arraycopy(buffer, left, buffer, 0, available - left);
            offset -= left;
            available -= left;
            if (left > 0) left = 0;
        }
        int toRead = buffer.length - available;
        if (toRead == 0) {
            overlong = true;
            return true;
        }
        int numRead = reader.read(buffer, available, toRead);
        if (numRead > 0) {
            available += numRead;
            return false;
        }
        eof = true;
        return true;
    }

    // used to prime the first symbol with whitespace
    private short advance(int ch, short s) {
        s |= startMask;
        s &= bitnfa[ch];
        s <<= 1;
        return s;
    }

    public final void getText(CharTermAttribute t) {
        t.copyBuffer(buffer, left, right - left);
    }

    public final int getCounter(){ return counter; }

    public final boolean nextToken() throws IOException {
        if (eof) return false;
        short s = state;
        int i = offset;
        left = -1;
        while (true) {
            if (i < available) {
                s = advance(buffer[i], s);
                int first = s & leftMask;
                int second = s & rightMask;
                if (first != 0) {
                    left = i;
                }
                else if (second != 0) {
                    counter += i - offset;
                    state = s;
                    offset = i;
                    right = second > 0x100 ? i - 1 : i;
                    return true;
                }
                i += 1;
            } else {
                counter += i - offset;
                offset = i;
                boolean e = refill();
                i = offset;
                if (e) {
                    if (left >= 0) {
                        right = offset;
                        // kludge, detect 3-char sequence state at the end
                        right -= (s & 0x1000) != 0 ? 1 : 0;
                        return true;
                    }
                    else return false;
                }
            }
        }
    }
}
