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
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

/**
 * Stemmer based on <a href="https://github.com/ahmetaa/zemberek-nlp">Zemberek3</a>
 */
public final class ZemberekStemFilter extends TokenFilter {

    private StemmerOverrideFilter.StemmerOverrideMap cache;
    private FST.BytesReader fstReader;

    private final FST.Arc<BytesRef> scratchArc = new FST.Arc<>();
    private final CharsRef spare = new CharsRef();

    private final MorphParser parser;
    private final String aggregation;

    private final CharTermAttribute termAttribute = addAttribute(CharTermAttribute.class);
    private final KeywordAttribute keywordAttribute = addAttribute(KeywordAttribute.class);

    public ZemberekStemFilter(TokenStream input, MorphParser parser, String aggregation) {
        super(input);
        this.parser = parser;
        this.aggregation = aggregation;
    }

    public void setCache(StemmerOverrideFilter.StemmerOverrideMap cache) {
        this.cache = cache;
        fstReader = cache.getBytesReader();
    }

    static String stem(String word, MorphParser parser, String aggregation) {

        List<MorphParse> parses = parser.parse(word);
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
        final String s = stem(term, parser, aggregation);
        // If not stemmed, don't waste the time adjusting the token.
        if ((s != null) && !s.equals(term))
            termAttribute.setEmpty().append(s);

        return true;
    }
}
