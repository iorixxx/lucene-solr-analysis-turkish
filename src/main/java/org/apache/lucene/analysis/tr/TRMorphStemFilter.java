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
import org.apache.lucene.analysis.tr.util.Piper;
import org.apache.lucene.analysis.util.CharArrayMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * Stemmer based on <a href="https://github.com/coltekin/TRmorph">TRmorph</a>
 */
public final class TRMorphStemFilter extends TokenFilter {

    public static final Logger log = LoggerFactory.getLogger(TRMorphStemFilter.class);

    private final CharTermAttribute termAttribute = addAttribute(CharTermAttribute.class);
    private final KeywordAttribute keywordAttribute = addAttribute(KeywordAttribute.class);
    private CharArrayMap<String> cache = null;
    private final String aggregation;
    private final String lookup;
    private final String fst;

    public void setCache(CharArrayMap<String> cache) {
        this.cache = cache;
    }

    public TRMorphStemFilter(TokenStream input, String lookup, String fst, String aggregation) {
        super(input);
        this.lookup = lookup;
        this.fst = fst;
        this.aggregation = aggregation;
    }

    @Override
    public boolean incrementToken() throws IOException {
        if (input.incrementToken()) {
            final String term = termAttribute.toString();
            // Check the exclusion table.
            if (!keywordAttribute.isKeyword()) {
                final String s = stem(term);
                // If not stemmed, don't waste the time adjusting the token.
                if ((s != null) && !s.equals(term))
                    termAttribute.setEmpty().append(s);
            }
            return true;
        } else {
            return false;
        }
    }

    private String stem(String word) throws IOException {

        List<String> parses = parse(word);

        TreeSet<String> set = new TreeSet<>();

        for (String parse : parses) {
            String[] parts = parse.split("\\s+");
            if (parts.length < 1) {
                log.warn("unexpected line " + parse);
                continue;
            }

            String stem = parts[1].trim();

            int i = stem.indexOf("<");

            if (i == -1) {
                if (stem.contains("+?"))
                    return word;
                else {
                    log.warn("unexpected stem " + stem);
                    continue;
                }
            }

            set.add(stem.substring(0, i));
        }

        if (set.size() == 1) return set.first();

        switch (aggregation) {
            case "max":
                return set.pollLast();
            case "min":
                return set.pollFirst();
            default:
                throw new RuntimeException("unknown strategy " + aggregation);
        }
    }

    public List<String> parse(String word) throws IOException {
        List<String> list = new ArrayList<>();
        java.lang.Runtime rt = java.lang.Runtime.getRuntime();
        java.lang.Process p2 = rt.exec(lookup + " " + fst);
        Piper pipe = new Piper(new ByteArrayInputStream(word.getBytes(StandardCharsets.UTF_8)), p2.getOutputStream());
        new Thread(pipe).start();
        try {
            p2.waitFor();
        } catch (InterruptedException ie) {
            return list;
        }
        java.io.BufferedReader r = new java.io.BufferedReader(new java.io.InputStreamReader(p2.getInputStream()));
        String s;
        while ((s = r.readLine()) != null) {

            s = s.trim();
            if (s.length() == 0) continue;

            if (s.startsWith(word))
                list.add(s);
            else
                log.warn("unexpected line from word " + word + " " + s);
        }
        return list;
    }
}



