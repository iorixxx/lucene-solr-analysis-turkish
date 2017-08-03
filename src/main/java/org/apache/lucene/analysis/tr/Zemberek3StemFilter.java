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
import zemberek.morphology.analysis.WordAnalysis;
import zemberek.morphology.analysis.tr.TurkishMorphology;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Stemmer based on <a href="https://github.com/ahmetaa/zemberek-nlp">Zemberek3</a>
 */
public final class Zemberek3StemFilter extends TokenFilter {

    private final TurkishMorphology morphology;
    private final String aggregation;

    private final CharTermAttribute termAttribute = addAttribute(CharTermAttribute.class);
    private final KeywordAttribute keywordAttribute = addAttribute(KeywordAttribute.class);

    public Zemberek3StemFilter(TokenStream input, TurkishMorphology morphology, String aggregation) {
        super(input);
        this.morphology = morphology;
        this.aggregation = aggregation;
    }

    private static List<WordAnalysis> selectMorphemes(List<WordAnalysis> results, String strategy) {

        // if 0 or 1
        if (results.size() < 2) return results;

        switch (strategy) {
            case "all":
                return results;
            case "maxMorpheme":
                final int max = results.stream().map(morphParse -> morphParse.inflectionalGroups.size()).max(Comparator.naturalOrder()).get();
                return results.stream().filter(parse -> parse.inflectionalGroups.size() == max).collect(Collectors.toList());
            case "minMorpheme":
                final int min = results.stream().map(morphParse -> morphParse.inflectionalGroups.size()).min(Comparator.naturalOrder()).get();
                return results.stream().filter(parse -> parse.inflectionalGroups.size() == min).collect(Collectors.toList());
            default:
                throw new RuntimeException("unknown strategy " + strategy);

        }
    }

    private static List<String> morphToString(List<WordAnalysis> results, String methodName) {

        List<String> list = new ArrayList<>();

        switch (methodName) {
            case "stems":
                for (WordAnalysis result : results)
                    list.addAll(result.getStems());
                return list;
            case "lemmas":
                for (WordAnalysis result : results)
                    list.addAll(result.getLemmas());
                return list;
            case "lemma":
                return results.stream().map(WordAnalysis::getLemma).collect(Collectors.toList());
            case "root":
                return results.stream().map(morphParse -> morphParse.root).collect(Collectors.toList());
            default:
                throw new RuntimeException("unknown method name " + methodName);
        }


    }

    static String stem(List<WordAnalysis> results, String aggregation) {

        List<WordAnalysis> alternatives = selectMorphemes(results, "minMorpheme");

        List<String> candidates = morphToString(alternatives, "lemmas");

        switch (aggregation) {
            case "maxLength":
                return Collections.max(candidates, Comparator.comparing(String::length));
            case "minLength":
                return Collections.min(candidates, Comparator.comparing(String::length));
            default:
                throw new RuntimeException("unknown strategy " + aggregation);
        }
    }

    @Override
    public boolean incrementToken() throws IOException {

        if (!input.incrementToken()) return false;
        if (keywordAttribute.isKeyword()) return true;

        /**
         *  copied from {@link org.apache.lucene.analysis.br.BrazilianStemFilter#incrementToken}
         */
        final String word = termAttribute.toString();

        final List<WordAnalysis> parses = morphology.analyze(word);
        if (parses.size() == 0) return true;

        final String s = stem(parses, aggregation);
        // If not stemmed, don't waste the time adjusting the token.
        if ((s != null) && !s.equals(word))
            termAttribute.setEmpty().append(s);

        return true;
    }
}
