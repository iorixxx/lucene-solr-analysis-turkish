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

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.util.TokenFilterFactory;

import java.io.File;
import java.util.Map;

/**
 * Factory for {@link TRMorphStemFilterFactory}.
 * <pre class="prettyprint">
 * &lt;fieldType name="text_tr_morph" class="solr.TextField" positionIncrementGap="100"&gt;
 *   &lt;analyzer&gt;
 *     &lt;tokenizer class="solr.StandardTokenizerFactory"/&gt;
 *     &lt;filter class="solr.ApostropheFilterFactory"/&gt;
 *     &lt;filter class="solr.TurkishLowerCaseFilterFactory"/&gt;
 *     &lt;filter class="org.apache.lucene.analysis.tr.TRMorphStemFilterFactory" lookup="/Applications/foma/flookup" fst="/Volumes/datadisk/Desktop/TRmorph-master/stem.fst"/&gt;
 *   &lt;/analyzer&gt;
 * &lt;/fieldType&gt;</pre>
 */
public class TRMorphStemFilterFactory extends TokenFilterFactory {

    private final String strategy;
    private final String lookup;
    private final String fst;

    public TRMorphStemFilterFactory(Map<String, String> args) {
        super(args);

        strategy = get(args, "strategy", "max");
        lookup = require(args, "lookup");
        fst = require(args, "fst");

        if (!args.isEmpty())
            throw new IllegalArgumentException("Unknown parameters: " + args);

        if (!"min".equals(strategy) && !"max".equals(strategy))
            throw new IllegalArgumentException("unknown strategy " + strategy);

        if (lookup != null) {
            File f = new File(lookup);
            if (!f.isAbsolute()) {
                throw new IllegalArgumentException("AbsolutePath must be provided for lookup executable: " + lookup);
            }
            if (!(f.isFile() && f.canRead())) {
                throw new IllegalArgumentException("Cannot read lookup executable: " + lookup);
            }
        }

        if (fst != null) {
            File f = new File(fst);
            if (!f.isAbsolute()) {
                throw new IllegalArgumentException("AbsolutePath must be provided for fst: " + fst);
            }
            if (!(f.isFile() && f.canRead())) {
                throw new IllegalArgumentException("Cannot read fst: " + fst);
            }
        }
    }

    @Override
    public TokenStream create(TokenStream input) {
        return new TRMorphStemFilter(input, lookup, fst, strategy);
    }
}
