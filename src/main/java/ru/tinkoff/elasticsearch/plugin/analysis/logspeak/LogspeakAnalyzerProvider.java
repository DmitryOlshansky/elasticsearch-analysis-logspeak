package ru.tinkoff.elasticsearch.plugin.analysis.logspeak;

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractIndexAnalyzerProvider;

public class LogspeakAnalyzerProvider extends AbstractIndexAnalyzerProvider<LogspeakAnalyzer> {

    private final LogspeakAnalyzer analyzer;

    public LogspeakAnalyzerProvider(IndexSettings indexSettings, String name, Settings settings) {
        super(indexSettings, name, settings);
        int maxTokenLength = settings.getAsInt("max_token_length", LogspeakAnalyzer.DEFAULT_MAX_TOKEN_LENGTH);
        analyzer = new LogspeakAnalyzer();
        analyzer.setMaxTokenLength(maxTokenLength);
    }

    @Override
    public LogspeakAnalyzer get() {
        return analyzer;
    }
}

