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

import org.apache.lucene.analysis.CharArrayMap;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.analysis.tr.util.PatternTableFactory;

import java.io.IOException;
import java.util.Arrays;

import static org.apache.lucene.analysis.tr.util.PatternTableFactory.*;

/**
 * Translation of <a href="https://github.com/emres/turkish-deasciifier">Turkish Deasciifier</a> from Lisp into Java
 */
public final class TurkishDeASCIIfyFilter extends TokenFilter {

    private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final PositionIncrementAttribute posIncAttr = addAttribute(PositionIncrementAttribute.class);
    private final boolean preserveOriginal;
    private State state;

    public TurkishDeASCIIfyFilter(TokenStream input, boolean preserveOriginal) {
        super(input);
        this.preserveOriginal = preserveOriginal;
    }

    /**
     * Determine if char at cursor needs correction.
     */
    private static boolean turkish_need_correction(char c, int point, char[] turkish_string, int length) {

        final Character tr;

        if (turkish_asciify_table.containsKey(c))
            tr = turkish_asciify_table.get(c);
        else
            tr = c;

        CharArrayMap<Integer> pl = PatternTableFactory.getMap(Character.toLowerCase(tr));

        boolean m = false;
        if (pl != null) {
            m = turkish_match_pattern(pl, point, turkish_string, length);
        }

        if (tr.equals('I')) {
            if (c == tr) {
                return !m;
            } else {
                return m;
            }
        } else {
            if (c == tr) {
                return m;
            } else {
                return !m;
            }
        }
    }

    private static char[] turkish_get_context(int size, int point, char[] turkish_string, int length) {

        char[] s = new char[1 + (2 * size)];
        Arrays.fill(s, ' ');

        s[size] = 'X';

        int i = size + 1;
        boolean space = false;
        int index = point;
        index++;

        char current_char;

        while (i < s.length && !space && index < length) {
            current_char = turkish_string[index];

            Character x = turkish_downcase_asciify_table.get(current_char);

            if (x == null) {
                i++;
                space = true;

            } else {
                s[i] = x;
                i++;
                space = false;
            }
            index++;
        }

/*
        System.out.println("before ");
        System.out.println(s.length);
        System.out.println(s);
        System.out.println(i);
*/

        System.arraycopy(s, 0, s, 0, i);

/*
        System.out.println("after ");

        System.out.println(s);
        System.out.println(s.length);
*/
        index = point;
        i = size - 1;
        space = false;

        index--;

        while (i >= 0 && index >= 0) {
            current_char = turkish_string[index];
            Character x = turkish_upcase_accents_table.get(current_char);

            if (x == null) {
                if (!space) {
                    i--;
                    space = true;
                }
            } else {
                s[i] = x;
                i--;
                space = false;
            }
            index--;
        }

        //System.out.println("return");
        //System.out.println(s);
        return s;
    }

    private static boolean turkish_match_pattern(CharArrayMap<Integer> dlist, int point, char[] turkish_string, int length) {
        final int turkish_context_size = 10;
        int rank = dlist.size() * 2;
        char[] str = turkish_get_context(turkish_context_size, point, turkish_string, length);

        //System.out.println("length = " + str.length);
        int start = 0;
        int end;
        int _len = str.length;

        while (start <= turkish_context_size) {
            end = turkish_context_size + 1;
            while (end <= _len) {

                Integer r = dlist.get(str, start, end - start);

                if (r != null && Math.abs(r) < Math.abs(rank)) {
                    rank = r;
                }
                end++;
            }
            start++;
        }
        return rank > 0;
    }

    /**
     * Adds necessary accents to the words in the region.
     */
    public static String convert_to_turkish(char[] turkish_string) {

        for (int i = 0; i < turkish_string.length; i++) {
            char c = turkish_string[i];
            if (turkish_toggle_accent_table.containsKey(c)) {
                if (turkish_need_correction(c, i, turkish_string, turkish_string.length)) {
                    turkish_string[i] = turkish_toggle_accent_table.get(c);
                }
            }
        }
        return new String(turkish_string);
    }

    /**
     * Adds necessary accents to the words in the region.
     */
    public boolean convert_to_turkish(char[] turkish_string, int length) {

        boolean returnValue = false;
        boolean flag = true;

        for (int i = 0; i < length; i++) {
            char c = turkish_string[i];
            if (turkish_toggle_accent_table.containsKey(c)) {
                if (turkish_need_correction(c, i, turkish_string, length)) {
                    /** works only once **/
                    if (flag && preserveOriginal) {
                        // we are about to make a change
                        // capture original state
                        state = captureState();
                        flag = false;
                    }
                    turkish_string[i] = turkish_toggle_accent_table.get(c);
                    returnValue = true;
                }
            }
        }

        return returnValue;
    }


    @Override
    public boolean incrementToken() throws IOException {
        if (state != null) {
            assert preserveOriginal : "state should only be captured if preserveOriginal is true";
            restoreState(state);
            posIncAttr.setPositionIncrement(0);
            state = null;
            return true;
        }
        if (input.incrementToken()) {
            final char[] buffer = termAtt.buffer();
            final int length = termAtt.length();
            if (convert_to_turkish(buffer, length))
                typeAtt.setType(Zemberek2DeASCIIfyFilterFactory.DEASCII_TOKEN_TYPE);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        state = null;
    }
}
