package ru.tinkoff.elasticsearch.analysis.logspeak.bitnfa;
import static ru.tinkoff.elasticsearch.analysis.logspeak.bitnfa.BitNfaConstants.*;

public final class BitNfaSplitter {

    boolean left = false; // after successful find indicates if the break was left or right

    private int state;

    // used to prime the first symbol with whitespace
    private void advance(int ch) {
        state |= startMask;
        state &= bitnfa[ch];
        state <<= 1;
    }

    public final int firstBreak(char[] buf, int start, int length) {
        state = 0;
        advance(' ');
        return continueFind(buf, start, length);
    }

    public final int nextBreak(char[] buf, int start, int length) {
        state = 0;
        return continueFind(buf, start, length);
    }

    public final int continueFind(char[] buf, int start, int length) {
        int s = state;
        for(int i=start; i<length; i++) {
            char ch = buf[i];
            s |= startMask;
            s &= bitnfa[ch];
            s <<= 1;
            //advance(c < maxInput ? c : ' '); // translate uncovered characters to ' '
            int f = s & finishMask;
            if (f != 0) {
                state = s;
                //left = (f & leftMask) != 0;
                /*int origin = (f - 1) & startMask;
                int offset = Integer.numberOfLeadingZeros(origin) - Integer.numberOfLeadingZeros(f);
                if ((f & breakMask) != 0) return i - (offset - 2); // index of 2nd char from start
                else return i; // index of first from back*/
                return i;
            }
        }
        state = s;
        return length;
    }

    // a kludge - test if our right 3-char break would succeed at EOF
    public final int chopAtEof() { return (state & 0x1000) != 0  ? 1 : 0; }

}
