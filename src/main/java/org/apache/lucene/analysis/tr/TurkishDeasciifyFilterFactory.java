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

import java.util.Map;

/**
 * Factory for {@link TurkishDeasciifyFilter}.
 * <pre class="prettyprint">
 * &lt;fieldType name="text_tr_deascii" class="solr.TextField" positionIncrementGap="100"&gt;
 *   &lt;analyzer type="index"&gt;
 *     &lt;tokenizer class="solr.StandardTokenizerFactory"/&gt;
 *     &lt;filter class="solr.ApostropheFilterFactory"/&gt;
 *     &lt;filter class="solr.TurkishLowerCaseFilterFactory"/&gt;
 *     &lt;filter class="solr.ZemberekStemFilterFactory"/&gt;
 *   &lt;/analyzer&gt;
 *   &lt;analyzer type="query"&gt;
 *     &lt;tokenizer class="solr.StandardTokenizerFactory"/&gt;
 *     &lt;filter class="solr.ApostropheFilterFactory"/&gt;
 *     &lt;filter class="solr.TurkishLowerCaseFilterFactory"/&gt;
 *     &lt;filter class="solr.TurkishDeasciifyFilterFactory" preserveOriginal="true"/&gt;
 *     &lt;filter class="solr.ZemberekStemFilterFactory"/&gt;
 *   &lt;/analyzer&gt;
 * &lt;/fieldType&gt;</pre>
 */
public class TurkishDeasciifyFilterFactory extends TokenFilterFactory {

    private final boolean preserveOriginal;

    /**
     * Creates a new TurkishDeasciifyFilterFactory
     */
    public TurkishDeasciifyFilterFactory(Map<String, String> args) {
        super(args);
        preserveOriginal = getBoolean(args, "preserveOriginal", false);
        if (!args.isEmpty()) {
            throw new IllegalArgumentException("Unknown parameters: " + args);
        }
    }

    @Override
    public TurkishDeasciifyFilter create(TokenStream input) {
        return new TurkishDeasciifyFilter(input, preserveOriginal);
    }
}
