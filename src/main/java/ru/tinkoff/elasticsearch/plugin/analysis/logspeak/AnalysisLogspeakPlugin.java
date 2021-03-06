package ru.tinkoff.elasticsearch.plugin.analysis.logspeak;

import org.apache.lucene.analysis.Analyzer;
import org.elasticsearch.index.analysis.AnalyzerProvider;
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
                new LogspeakTokenizerFactory(indexSettings, settings));
        return tokenizers;
    }

    @Override
    public Map<String, AnalysisModule.AnalysisProvider<AnalyzerProvider<? extends Analyzer>>> getAnalyzers() {
        final Map<String, AnalysisModule.AnalysisProvider<AnalyzerProvider<? extends Analyzer>>> analyzers = new HashMap<>();
        analyzers.put("logspeak", (indexSettings, environment, name, settings) ->
                new LogspeakAnalyzerProvider(indexSettings, name, settings));
        return analyzers;
    }
}
