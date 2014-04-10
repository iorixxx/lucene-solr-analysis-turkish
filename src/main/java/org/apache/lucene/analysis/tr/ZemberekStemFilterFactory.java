package org.apache.lucene.analysis.tr;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import com.google.common.collect.Lists;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.util.*;
import zemberek.morphology.lexicon.RootLexicon;
import zemberek.morphology.lexicon.SuffixProvider;
import zemberek.morphology.lexicon.graph.DynamicLexiconGraph;
import zemberek.morphology.lexicon.tr.TurkishDictionaryLoader;
import zemberek.morphology.lexicon.tr.TurkishSuffixes;
import zemberek.morphology.parser.MorphParser;
import zemberek.morphology.parser.SimpleParser;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Factory for {@link ZemberekStemFilter}.
 * <pre class="prettyprint">
 * &lt;fieldType name="text_tr_zemberek" class="solr.TextField" positionIncrementGap="100"&gt;
 * &lt;analyzer&gt;
 * &lt;tokenizer class="solr.StandardTokenizerFactory"/&gt;
 * &lt;filter class="solr.ApostropheFilterFactory"/&gt;
 * &lt;filter class="solr.TurkishLowerCaseFilterFactory"/&gt;
 * &lt;filter class="solr.ZemberekStemFilterFactory" strategy="max"/&gt;
 * &lt;/analyzer&gt;
 * &lt;/fieldType&gt;</pre>
 */
public class ZemberekStemFilterFactory extends TokenFilterFactory implements ResourceLoaderAware {

    private final String strategy;
    private final String dictionaryFiles;
    private final String cacheFiles;
    private final int cacheSize;
    List<String> _lines = Lists.newArrayList();
    private CharArraySet _cacheLines;


    public ZemberekStemFilterFactory(Map<String, String> args) {
        super(args);
        strategy = get(args, "strategy", "max");

        dictionaryFiles = get(args, "dictionary");
        cacheFiles = get(args, "cache");
        cacheSize = getInt(args, "cacheSize", 5000);

        if (!args.isEmpty()) {
            throw new IllegalArgumentException("Unknown parameters: " + args);
        }
    }

    @Override
    public void inform(ResourceLoader loader) throws IOException {

        if (dictionaryFiles != null) {
            List<String> files = splitFileNames(dictionaryFiles);
            if (files.size() > 0) {
                for (String file : files) {
                    List<String> wlist = getLines(loader, file.trim());
                    _lines.addAll(wlist);
                }
            }
        }

        _cacheLines = getWordSet(loader, cacheFiles, false);
        /*
        if (cacheFiles != null) {
            List<String> files = splitFileNames(cacheFiles);

            if (files.size() > 0) {
                for (String file : files) {
                    List<String> wlist = getLines(loader, file.trim());
                    if ((_cacheLines.size() + wlist.size()) < cacheSize)
                        _cacheLines.addAll(wlist);
                    else {
                        _cacheLines.addAll(wlist.subList(0, cacheSize - _cacheLines.size()));
                        break;
                    }

                }
            }
        }
        */
    }

    @Override
    public TokenStream create(TokenStream input) {

        MorphParser parser;
        SuffixProvider suffixProvider = new TurkishSuffixes();
        RootLexicon lexicon = new TurkishDictionaryLoader(suffixProvider).load(_lines);
        DynamicLexiconGraph graph = new DynamicLexiconGraph(suffixProvider);
        graph.addDictionaryItems(lexicon);
        parser = new SimpleParser(graph);

        ZemberekStemFilter filter = new ZemberekStemFilter(input, parser, strategy);

        if (_cacheLines != null) {
            CharArrayMap<String> cache = new CharArrayMap<>(luceneMatchVersion, _cacheLines.size(), false);

            for (Object a : _cacheLines)
                cache.put(a, filter.stem(a.toString()));

            filter.setCache(cache);
        }
        return filter;
    }
}