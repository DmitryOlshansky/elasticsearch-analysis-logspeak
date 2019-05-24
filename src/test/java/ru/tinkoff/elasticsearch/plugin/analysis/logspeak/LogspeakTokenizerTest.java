package ru.tinkoff.elasticsearch.plugin.analysis.logspeak;

import junit.framework.TestCase;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LogspeakTokenizerTest extends TestCase {

    private List<String> tokenize(Tokenizer tk, String str) throws IOException  {
        Reader reader = new CharArrayReader(str.toCharArray());
        ArrayList<String> list = new ArrayList<>();
        tk.setReader(reader);
        tk.reset();
        while(tk.incrementToken()) {
            list.add(tk.getAttribute(CharTermAttribute.class).toString());
        }
        tk.close();
        assertFalse(tk.incrementToken());
        assertFalse(tk.incrementToken());
        return list;
    }

    public void testTokenizer() throws IOException {
        LogspeakTokenizer tk = new LogspeakTokenizer();
        assertEquals(Arrays.asList("ABC", "X", "AC"), tokenize(tk, "ABC X -AC-"));
    }

    public void testRefills() throws IOException {
        LogspeakTokenizer tk = new LogspeakTokenizer();
        tk.setMaxTokenLength(3);
        assertEquals(Arrays.asList("ABC", "D"), tokenize(tk, "ABCD"));
        assertEquals(Arrays.asList("A", "B", "CD", "E", "F"), tokenize(tk, " A B CD E F!!"));
    }

    public void testReset() throws IOException {
        LogspeakTokenizer tk = new LogspeakTokenizer();
    }

    public void testTokenizeWhiteSpace() throws IOException {
        LogspeakTokenizer tk = new LogspeakTokenizer();
        assertEquals(Arrays.asList("abc", "def"), tokenize(tk, "abc def"));
        assertEquals(Arrays.asList("me", "culpa"), tokenize(tk, " me culpa "));
        assertEquals(Arrays.asList("m", "cul", "p"), tokenize(tk, "m cul p "));
    }

    public void testPunct() throws IOException {
        LogspeakTokenizer tk = new LogspeakTokenizer();
        assertEquals(Arrays.asList("255.255.255.255"), tokenize(tk, "255.255.255.255"));
        assertEquals(Arrays.asList("255.255.255.255"), tokenize(tk, "-255.255.255.255"));
        assertEquals(Arrays.asList("my", "uuid-like", "var", "is", "ABCDE-EFAF-YOU_KNOW"),
                tokenize(tk, "my uuid-like :var: is ABCDE-EFAF-YOU_KNOW"));
        assertEquals(Arrays.asList("Some", "sentence", "with"), tokenize(tk, "Some sentence, with."));
        assertEquals(Arrays.asList("ABC", "X", "AC"), tokenize(tk, "ABC X -AC-"));
    }

}
