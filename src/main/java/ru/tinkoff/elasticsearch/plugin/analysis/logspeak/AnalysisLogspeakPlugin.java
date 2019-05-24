package ru.tinkoff.elasticsearch.plugin.analysis.logspeak;

import org.elasticsearch.index.analysis.TokenizerFactory;
import org.elasticsearch.indices.analysis.AnalysisModule;
import org.elasticsearch.plugins.AnalysisPlugin;
import org.elasticsearch.plugins.Plugin;

import java.util.HashMap;
import java.util.Map;

public class AnalysisLogspeakPlugin extends Plugin implements AnalysisPlugin {

    @Override
    public Map<String, AnalysisModule.AnalysisProvider<TokenizerFactory>> getTokenizers() {
        final Map<String, AnalysisModule.AnalysisProvider<TokenizerFactory>> tokenizers = new HashMap<>();
        tokenizers.put("logspeak", (indexSettings, environment, name, settings) ->
                new LogspeakTokenizerFactory(indexSettings, name, settings));
        return tokenizers;
    }

}
