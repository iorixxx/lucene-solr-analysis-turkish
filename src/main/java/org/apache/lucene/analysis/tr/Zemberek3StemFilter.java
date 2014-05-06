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
import org.apache.lucene.analysis.miscellaneous.StemmerOverrideFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.KeywordAttribute;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.CharsRef;
import org.apache.lucene.util.UnicodeUtil;
import org.apache.lucene.util.fst.FST;
import zemberek.morphology.parser.MorphParse;
import zemberek.morphology.parser.MorphParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Stemmer based on <a href="https://github.com/ahmetaa/zemberek-nlp">Zemberek3</a>
 */
public final class Zemberek3StemFilter extends TokenFilter {

    private static final StringLengthComparator STRING_LENGTH_COMPARATOR = new StringLengthComparator();
    private static final MorphemeComparator MORPHEME_COMPARATOR = new MorphemeComparator();

    private StemmerOverrideFilter.StemmerOverrideMap cache;
    private FST.BytesReader fstReader;

    private final FST.Arc<BytesRef> scratchArc = new FST.Arc<>();
    private final CharsRef spare = new CharsRef();

    private final MorphParser parser;
    private final String aggregation;

    private final CharTermAttribute termAttribute = addAttribute(CharTermAttribute.class);
    private final KeywordAttribute keywordAttribute = addAttribute(KeywordAttribute.class);

    public Zemberek3StemFilter(TokenStream input, MorphParser parser, String aggregation) {
        super(input);
        this.parser = parser;
        this.aggregation = aggregation;
    }

    public void setCache(StemmerOverrideFilter.StemmerOverrideMap cache) {
        this.cache = cache;
        fstReader = cache.getBytesReader();
    }

    private static class StringLengthComparator implements Comparator<String> {
        @Override
        public int compare(String a, String b) {
            return a.length() - b.length();
        }

    }

    private static class MorphemeComparator implements Comparator<MorphParse> {
        @Override
        public int compare(MorphParse o1, MorphParse o2) {
            if (o1 == null || o2 == null) return -1;
            return o1.inflectionalGroups.size() - o2.inflectionalGroups.size();
        }
    }


    static List<MorphParse> selectMorphemes(List<MorphParse> parses, String strategy) {

        // if 0 or 1
        if (parses.size() < 2) return parses;

        List<MorphParse> list;

        switch (strategy) {
            case "all":
                return parses;
            case "maxMorpheme":
                list = new ArrayList<>();
                MorphParse maxMorphParse = Collections.max(parses, MORPHEME_COMPARATOR);
                int maxMorphParseLength = maxMorphParse.inflectionalGroups.size();
                for (MorphParse parse : parses)
                    if (parse.inflectionalGroups.size() == maxMorphParseLength)
                        list.add(parse);

                return list;
            case "minMorpheme":
                list = new ArrayList<>();
                MorphParse minMorphParse = Collections.min(parses, MORPHEME_COMPARATOR);
                int minMorphParseLength = minMorphParse.inflectionalGroups.size();
                for (MorphParse parse : parses)
                    if (parse.inflectionalGroups.size() == minMorphParseLength)
                        list.add(parse);

                return list;
            default:
                throw new RuntimeException("unknown strategy " + strategy);

        }
    }

    static List<String> morphToString(List<MorphParse> parses, String methodName) {

        List<String> list = new ArrayList<>();

        switch (methodName) {
            case "stems":
                for (MorphParse parse : parses)
                    list.addAll(parse.getStems());
                return list;
            case "lemmas":
                for (MorphParse parse : parses)
                    list.addAll(parse.getLemmas());
                return list;
            case "lemma":
                for (MorphParse parse : parses)
                    list.add(parse.getLemma());
                return list;
            case "root":
                for (MorphParse parse : parses)
                    list.add(parse.root);
                return list;
            default:
                throw new RuntimeException("unknown method name " + methodName);
        }


    }

    static String stem(List<MorphParse> parses, String aggregation) {

        List<MorphParse> alternatives = selectMorphemes(parses, "minMorpheme");

        List<String> candidates = morphToString(alternatives, "lemmas");

        switch (aggregation) {
            case "maxLength":
                return Collections.max(candidates, STRING_LENGTH_COMPARATOR);
            case "minLength":
                return Collections.min(candidates, STRING_LENGTH_COMPARATOR);
            default:
                throw new RuntimeException("unknown strategy " + aggregation);
        }
    }

    @Override
    public boolean incrementToken() throws IOException {

        if (!input.incrementToken()) return false;
        if (keywordAttribute.isKeyword()) return true;

        /**
         * copied from {@link StemmerOverrideFilter#incrementToken}
         */
        if (cache != null) {
            final BytesRef stem = cache.get(termAttribute.buffer(), termAttribute.length(), scratchArc, fstReader);
            if (stem != null) {
                final char[] buffer = spare.chars = termAttribute.buffer();
                UnicodeUtil.UTF8toUTF16(stem.bytes, stem.offset, stem.length, spare);
                if (spare.chars != buffer) {
                    termAttribute.copyBuffer(spare.chars, spare.offset, spare.length);
                }
                termAttribute.setLength(spare.length);
                return true;
            }
        }

        /**
         *  copied from {@link org.apache.lucene.analysis.br.BrazilianStemFilter#incrementToken}
         */
        final String term = termAttribute.toString();

        final List<MorphParse> parses = parser.parse(term);
        if (parses.size() == 0) return true;

        final String s = stem(parses, aggregation);
        // If not stemmed, don't waste the time adjusting the token.
        if ((s != null) && !s.equals(term))
            termAttribute.setEmpty().append(s);

        return true;
    }
}
