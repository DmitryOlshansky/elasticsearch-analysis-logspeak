package ru.tinkoff.elasticsearch.plugin.analysis.logspeak;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
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
@Measurement(iterations = 5, time = 30, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Benchmark)
public class TokenizersBenchmark {

    @Param({"logspeak", "standard", "whitespace"})
    String tokenizer;

    @Param({
        "short",
        "uri",
        "request"
    })
    String argument;

    String small = "Big brown fox jumps over a lazy dog";
    String uri = "http://some.api.ru/user/params?param1=1-XYZXYZXYZ&param2=ABCD";
    String req = "Request HttpMethod(GET)" +
            "http://some.api.ru/user/params?param1=1-XYZXYZXYZ&param2=ABCD " +
            "Host: api.tinkoff.ru, " +
            "X-Forwarded-For: 10.218.7.21 " +
            "X-Real-Ip: 10.218.7.21 " +
            "X-Scheme: https " +
            "Connection: close, " +
            "Authorization: 1234567890ABCDEF123456789ABCDEF " +
            "User-Agent: akka-http/10.1.1 " +
            "X-Forwarded-Proto: https";

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
            case "whitespace":
                tk = new WhitespaceTokenizer();
                break;
        }
        switch(argument) {
            case "short":
                source = small.toCharArray();
                break;
            case "uri":
                source = uri.toCharArray();
                break;
            case "request":
                source = req.toCharArray();
                break;
        }
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
