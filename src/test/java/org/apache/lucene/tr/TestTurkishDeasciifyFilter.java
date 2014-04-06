package org.apache.lucene.tr;

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

import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.MockTokenizer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tr.TurkishDeasciifyFilter;

import java.io.IOException;
import java.io.StringReader;

/**
 * Simple tests to ensure Turkish deasciifiy filter factory is working.
 */
public class TestTurkishDeasciifyFilter extends BaseTokenStreamTestCase {

    protected static MockTokenizer whitespaceMockTokenizer(String input) throws IOException {
        return new MockTokenizer(new StringReader(input));
    }

    public void testDeAscii() throws Exception {
        TokenStream stream = whitespaceMockTokenizer("kus fadil akgunduz dogalgaz ahmet");
        stream = new TurkishDeasciifyFilter(stream, false);
        assertTokenStreamContents(stream, new String[]{"kuş", "fadıl", "akgündüz", "doğalgaz", "ahmet"});
    }

    public void testPreserveOriginal() throws Exception {
        TokenStream stream = whitespaceMockTokenizer("kus fadil akgunduz dogalgaz ahmet izmir");
        stream = new TurkishDeasciifyFilter(stream, true);
        assertTokenStreamContents(stream, new String[]{
                "kuş", "kus",
                "fadıl", "fadil",
                "akgündüz", "akgunduz",
                "doğalgaz", "dogalgaz",
                "ahmet",
                "izmir"
        });
    }
}
