package ru.tinkoff.elasticsearch.analysis.logspeak;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.openjdk.jmh.annotations.*;

import java.io.CharArrayReader;
import java.io.IOException;
import java.io.Reader;
import java.util.concurrent.TimeUnit;

@BenchmarkMode({Mode.Throughput})
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 1, time = 10, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 15, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Benchmark)
public class TokenizersBenchmark {

    @Param({"logspeak", "standard"})
    String tokenizer;

    String string = "Big brown fox jumps over a lazy dog";

    Tokenizer tk;

    CharTermAttribute term;

    char[] source;

    @Setup(Level.Trial)
    public void setup() {
        switch(tokenizer) {
            case "standard":
                tk = new StandardTokenizer();
                break;
            case "logspeak":
                tk = new LogspeakTokenizer();
                break;
        }
        source = string.toCharArray();
        term = tk.getAttribute(CharTermAttribute.class);
    }

    @Benchmark
    public int tokenize() throws IOException {
        Reader reader = new CharArrayReader(source);
        tk.setReader(reader);
        tk.reset();
        int len = 0;
        while(tk.incrementToken()) {
            len += term.charAt(0) + term.length();
        }
        tk.close();
        return len;
    }
}
