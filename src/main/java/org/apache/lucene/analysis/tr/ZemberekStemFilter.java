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

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.KeywordAttribute;
import zemberek.morphology.apps.SimpleMorphCache;
import zemberek.morphology.parser.MorphParse;
import zemberek.morphology.parser.MorphParser;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

/**
 * Stemmer based on <a href="https://github.com/ahmetaa/zemberek-nlp">Zemberek3</a>
 */
public final class ZemberekStemFilter extends TokenFilter {


    private final MorphParser parser;
    private final SimpleMorphCache cache;

    private final String aggregation;

    private final CharTermAttribute termAttribute = addAttribute(CharTermAttribute.class);
    private final KeywordAttribute keywordAttribute = addAttribute(KeywordAttribute.class);

    public ZemberekStemFilter(TokenStream input, MorphParser parser, SimpleMorphCache cache, String aggregation) {
        super(input);
        this.parser = parser;
        this.cache = cache;
        this.aggregation = aggregation;
    }

    private List<MorphParse> parse(String word) {
        if (cache != null) {
            List<MorphParse> result = cache.parse(word);
            return result != null ? result : parser.parse(word);
        } else
            return parser.parse(word);
    }


    private String stem(String word, String aggregation) {

        List<MorphParse> parses = parse(word);
        if (parses.size() == 0) return word;


        TreeSet<String> lemmaSet = new TreeSet<>(new Comparator<String>() {
            @Override
            public int compare(String a, String b) {
                return a.length() - b.length();
            }
        }
        );


        for (MorphParse parse : parses) {
            lemmaSet.addAll(parse.getLemmas());
        }

        if (lemmaSet.size() == 1) return lemmaSet.first();

        switch (aggregation) {
            case "max":
                return lemmaSet.pollLast();
            case "min":
                return lemmaSet.pollFirst();
            default:
                throw new RuntimeException("unknown strategy " + aggregation);
        }


    }

    @Override
    public boolean incrementToken() throws IOException {
        if (input.incrementToken()) {
            final String term = termAttribute.toString();
            // Check the exclusion table.
            if (!keywordAttribute.isKeyword()) {
                final String s = stem(term, aggregation);
                // If not stemmed, don't waste the time adjusting the token.
                if ((s != null) && !s.equals(term))
                    termAttribute.setEmpty().append(s);
            }
            return true;
        } else {
            return false;
        }
    }
}
