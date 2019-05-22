package ru.tinkoff.elasticsearch.analysis.logspeak.bitnfa;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class TestBitNFA extends TestCase {

    String capture(String example, int offset) {
        char[] s = example.toCharArray();
        int i = new BitNfaSplitter().nextBreak(s, offset, s.length);
        return example.substring(0, i);
    }

    List<String> tokenize(String example) {
        List<String> result = new ArrayList<>();
        BitNfaSplitter split = new BitNfaSplitter();
        int i = 0;
        char[] chars = example.toCharArray();
        i = split.firstBreak(chars, i, chars.length);
        if (i < 0) i = chars.length;
        if (!split.left) result.add(example.substring(0, i));
        boolean lastLeft;
        int j = 0;
        while (true) {
            lastLeft = split.left;
            j = i;
            i = split.nextBreak(chars, i, chars.length);
            if (lastLeft) {
                if (i == chars.length) {
                    i -= split.chopAtEof();
                    result.add(example.substring(j, i));
                    return result;
                }
                else
                    result.add(example.substring(j, i));

            }
        }
    }

    public void testSimpleBreaks() {
        assertEquals("abc", capture("abc def", 0));
        assertEquals("abc ", capture("abc def", 3));
        assertEquals("abc def", capture("abc def", 4));
    }

    public void testTokenizeWhiteSpace() {
        assertEquals(Arrays.asList("abc", "def"), tokenize("abc def"));
        assertEquals(Arrays.asList("me", "culpa"), tokenize(" me culpa "));
        assertEquals(Arrays.asList("m", "cul", "p"), tokenize("m cul p "));
    }

    public void testPunct() {
        assertEquals(Arrays.asList("255.255.255.255"), tokenize("255.255.255.255"));
        assertEquals(Arrays.asList("255.255.255.255"), tokenize("-255.255.255.255"));
        assertEquals(Arrays.asList("my", "uuid-like", "var", "is", "ABCDE-EFAF-YOU_KNOW"),
                tokenize("my uuid-like :var: is ABCDE-EFAF-YOU_KNOW"));
        assertEquals(Arrays.asList("Some", "sentence", "with"), tokenize("Some sentence, with."));
        assertEquals(Arrays.asList("ABC", "X", "AC"), tokenize("ABC X -AC-"));
    }
}
