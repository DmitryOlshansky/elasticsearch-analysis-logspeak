package ru.tinkoff.elasticsearch.analysis.logspeak;

import junit.framework.TestCase;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LogspeakTokenizerTest extends TestCase {

    private List<String> tokenize(Tokenizer tk, Reader reader) throws IOException  {
        ArrayList<String> list = new ArrayList<>();
        tk.setReader(reader);
        tk.reset();
        while(tk.incrementToken()) {
            list.add(tk.getAttribute(CharTermAttribute.class).toString());
        }
        assertFalse(tk.incrementToken());
        assertFalse(tk.incrementToken());
        return list;
    }

    public void testTokenizer() throws IOException {
        LogspeakTokenizer tk = new LogspeakTokenizer();
        Reader reader = new InputStreamReader(new ByteArrayInputStream("ABC X -AC-".getBytes()));
        assertEquals(Arrays.asList("ABC", "X", "AC"), tokenize(tk, reader));
    }

    public void testRefills() throws IOException {
        LogspeakTokenizer tk = new LogspeakTokenizer();
        tk.setMaxTokenLength(3);
        Reader reader = new InputStreamReader(new ByteArrayInputStream(" A B CD E F!!".getBytes()));
        assertEquals(Arrays.asList("A", "B", "CD", "E", "F"), tokenize(tk, reader));
    }

    public void testReset() throws IOException {
        LogspeakTokenizer tk = new LogspeakTokenizer();

    }
}
