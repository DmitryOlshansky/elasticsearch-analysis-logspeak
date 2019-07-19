package ru.tinkoff.elasticsearch.plugin.analysis.logspeak;

import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenizerFactory;

public final class LogspeakTokenizerFactory extends AbstractTokenizerFactory {

    public LogspeakTokenizerFactory(IndexSettings indexSettings,  Settings settings) {
        super(indexSettings, settings);
    }

    @Override
    public final Tokenizer create() {
        return new LogspeakTokenizer();
    }

}
