package org.apache.lucene.analysis.tr.util;

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

import org.apache.lucene.analysis.util.CharArrayMap;
import org.apache.lucene.util.Version;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Compiles a decision list into a hash where keys are patterns and
 * values give the rank and the classification of the decision list.  The
 * sign of a value gives the classification (positive implies t, negative
 * implies nil), and the absolute value gives the rank (smaller rank
 * means higher priority).
 */
public final class PatternTableFactory {

    /**
     * Converts turkish characters into ascii equivalent
     */
    public static final Map<Character, Character> turkish_asciify_table =

            Collections.unmodifiableMap(
                    new HashMap<Character, Character>() {{

                        put('ç', 'c');
                        put('Ç', 'C');
                        put('ğ', 'g');
                        put('Ğ', 'G');
                        put('ö', 'o');
                        put('Ö', 'O');
                        put('ü', 'u');
                        put('Ü', 'U');
                        put('ı', 'i');
                        put('İ', 'I');
                        put('ş', 's');
                        put('Ş', 'S');

                    }}
            );
    public static final Map<Character, Character> turkish_downcase_asciify_table =

            Collections.unmodifiableMap(
                    new HashMap<Character, Character>() {{

                        put('ç', 'c');
                        put('Ç', 'c');
                        put('ğ', 'g');
                        put('Ğ', 'g');
                        put('ö', 'o');
                        put('Ö', 'o');
                        put('ı', 'i');
                        put('İ', 'i');
                        put('ş', 's');
                        put('Ş', 's');
                        put('ü', 'u');
                        put('Ü', 'u');

                        //    for ch in string.uppercase:
                        //    turkish_downcase_asciify_table[ch] = ch.lower()
                        //    turkish_downcase_asciify_table[ch.lower()] = ch.lower()


                        put('A', 'a');
                        put('B', 'b');
                        put('C', 'c');
                        put('D', 'd');
                        put('E', 'e');
                        put('F', 'f');
                        put('G', 'g');
                        put('H', 'h');
                        put('I', 'i');
                        put('J', 'j');
                        put('K', 'k');
                        put('L', 'l');
                        put('M', 'm');
                        put('N', 'n');
                        put('O', 'o');
                        put('P', 'p');
                        put('Q', 'q');
                        put('R', 'r');
                        put('S', 's');
                        put('T', 't');
                        put('U', 'u');
                        put('V', 'v');
                        put('W', 'w');
                        put('X', 'x');
                        put('Y', 'y');
                        put('Z', 'z');

                        put('a', 'a');
                        put('b', 'b');
                        put('c', 'c');
                        put('d', 'd');
                        put('e', 'e');
                        put('f', 'f');
                        put('g', 'g');
                        put('h', 'h');
                        put('i', 'i');
                        put('j', 'j');
                        put('k', 'k');
                        put('l', 'l');
                        put('m', 'm');
                        put('n', 'n');
                        put('o', 'o');
                        put('p', 'p');
                        put('q', 'q');
                        put('r', 'r');
                        put('s', 's');
                        put('t', 't');
                        put('u', 'u');
                        put('v', 'v');
                        put('w', 'w');
                        put('x', 'x');
                        put('y', 'y');
                        put('z', 'z');


                    }}
            );
    /**
     * Lowercase the string except for Turkish accented characters which are converted to uppercase ascii equivalent.
     * Useful for pattern matching.  Handles all 3 encodings.
     * The confusing case of i is as follows: i => i, dotted I => i, dotless i => I, I => I"
     */
    public static final Map<Character, Character> turkish_upcase_accents_table =

            Collections.unmodifiableMap(
                    new HashMap<Character, Character>() {{

                        put('ç', 'C');
                        put('Ç', 'C');
                        put('ğ', 'G');
                        put('Ğ', 'G');
                        put('ö', 'O');
                        put('Ö', 'O');
                        put('ı', 'I');
                        put('İ', 'i');
                        put('ş', 'S');
                        put('Ş', 'S');
                        put('ü', 'U');
                        put('Ü', 'U');

                        put('A', 'a');
                        put('B', 'b');
                        put('C', 'c');
                        put('D', 'd');
                        put('E', 'e');
                        put('F', 'f');
                        put('G', 'g');
                        put('H', 'h');
                        put('I', 'i');
                        put('J', 'j');
                        put('K', 'k');
                        put('L', 'l');
                        put('M', 'm');
                        put('N', 'n');
                        put('O', 'o');
                        put('P', 'p');
                        put('Q', 'q');
                        put('R', 'r');
                        put('S', 's');
                        put('T', 't');
                        put('U', 'u');
                        put('V', 'v');
                        put('W', 'w');
                        put('X', 'x');
                        put('Y', 'y');
                        put('Z', 'z');

                        put('a', 'a');
                        put('b', 'b');
                        put('c', 'c');
                        put('d', 'd');
                        put('e', 'e');
                        put('f', 'f');
                        put('g', 'g');
                        put('h', 'h');
                        put('i', 'i');
                        put('j', 'j');
                        put('k', 'k');
                        put('l', 'l');
                        put('m', 'm');
                        put('n', 'n');
                        put('o', 'o');
                        put('p', 'p');
                        put('q', 'q');
                        put('r', 'r');
                        put('s', 's');
                        put('t', 't');
                        put('u', 'u');
                        put('v', 'v');
                        put('w', 'w');
                        put('x', 'x');
                        put('y', 'y');
                        put('z', 'z');

                    }}
            );
    /**
     * Converts turkish characters into ascii equivalent and appropriate
     * ascii characters to utf-8 turkish accented versions.
     */
    public static final Map<Character, Character> turkish_toggle_accent_table =

            Collections.unmodifiableMap(
                    new HashMap<Character, Character>() {{

                        put('c', 'ç');
                        put('C', 'Ç');
                        put('g', 'ğ');
                        put('G', 'Ğ');
                        put('o', 'ö');
                        put('O', 'Ö');
                        put('u', 'ü');
                        put('U', 'Ü');
                        put('i', 'ı');
                        put('I', 'İ');
                        put('s', 'ş');
                        put('S', 'Ş');
                        put('ç', 'c');
                        put('Ç', 'C');
                        put('ğ', 'g');
                        put('Ğ', 'G');
                        put('ö', 'o');
                        put('Ö', 'O');
                        put('ü', 'u');
                        put('Ü', 'U');
                        put('ı', 'i');
                        put('İ', 'I');
                        put('ş', 's');
                        put('Ş', 'S');

                    }}
            );
    static final Version version = Version.LUCENE_48;
    static final boolean ignoreCase = false;

    private PatternTableFactory() {
    }

    public static CharArrayMap<Integer> getMap(char c) {
        switch (c) {
            case 'c':
                return MapC.map;
            case 'g':
                return MapG.map;
            case 'i':
                return MapI.map;
            case 'o':
                return MapO.map;
            case 's':
                return MapS.map;
            case 'u':
                return MapU.map;
            default:
                return null;
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
}
