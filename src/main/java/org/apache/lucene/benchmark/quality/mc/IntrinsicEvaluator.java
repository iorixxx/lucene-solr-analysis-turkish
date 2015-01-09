package org.apache.lucene.benchmark.quality.mc;

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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.ClassicFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tr.ApostropheFilter;
import org.apache.lucene.analysis.tr.TurkishLowerCaseFilter;
import org.apache.lucene.util.Version;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * intrinsic evaluation of the different diacritics restoration systems
 */
public class IntrinsicEvaluator {

    /**
     * Filters {@link org.apache.lucene.analysis.standard.StandardTokenizer} with {@link org.apache.lucene.analysis.tr.ApostropheFilter} and {@link org.apache.lucene.analysis.tr.TurkishLowerCaseFilter}.
     */
    final static class TurkishAnalyzer extends Analyzer {

        @Override
        protected TokenStreamComponents createComponents(final String fieldName, final Reader reader) {
            final StandardTokenizer src = new StandardTokenizer(Version.LUCENE_48, reader);
            src.setMaxTokenLength(255);
            TokenStream tok = new ClassicFilter(src);
            tok = new ApostropheFilter(tok);
            tok = new TurkishLowerCaseFilter(tok);


            return new TokenStreamComponents(src, tok) {
                @Override
                protected void setReader(final Reader reader) throws IOException {
                    src.setMaxTokenLength(255);
                    super.setReader(reader);
                }
            };
        }
    }


    private final static PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:*.txt");

    static List<Path> discoverTextFiles(Path p) {

        final List<Path> txtFiles = new ArrayList<>();

        FileVisitor<Path> fv = new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

                Path name = file.getFileName();
                if (name != null && matcher.matches(name))
                    txtFiles.add(file);
                return FileVisitResult.CONTINUE;
            }
        };

        try {
            Files.walkFileTree(p, fv);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return txtFiles;
    }

    static final Set<Character> accentedCharSet = new HashSet<>(SolrSearcher.TURKISH_CHARACTERS.length);

    static {

        for (int j = 0; j < SolrSearcher.TURKISH_CHARACTERS.length; j++)
            accentedCharSet.add(SolrSearcher.TURKISH_CHARACTERS[j]);
    }

    static boolean containsTurkishAccentedChar(final CharTermAttribute termAtt) {
        final char[] buffer = termAtt.buffer();
        final int length = termAtt.length();

        for (int i = 0; i < length; i++)
            if (accentedCharSet.contains(buffer[i])) return true;

        return false;

    }

    public static void main(String[] args) throws IOException {

        Map<String, Set<String>> collisions = new HashMap<>();

        for (Path path : discoverTextFiles(Paths.get("/Users/iorixxx/collection-20072011"))) {
            System.out.println("processing file : " + path);

            Analyzer analyzer = new TurkishAnalyzer();
            try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {

                for (; ; ) {
                    String line = reader.readLine();

                    if (line == null)
                        break;

                    if ("<DOCUMENT>".equals(line)) continue;
                    if ("</DOCUMENT>".equals(line)) continue;


                    try (TokenStream ts = analyzer.tokenStream("field", new StringReader(line))) {

                        final CharTermAttribute termAtt = ts.addAttribute(CharTermAttribute.class);
                        ts.reset(); // Resets this stream to the beginning. (Required)
                        while (ts.incrementToken()) {

                            if (!containsTurkishAccentedChar(termAtt))
                                continue;

                            final String term = termAtt.toString();

                            String asciiTerm = SolrSearcher.asciify(term);

                            // System.out.println(term);

                            if (collisions.containsKey(asciiTerm))
                                collisions.get(asciiTerm).add(term);
                            else {
                                Set<String> list = new HashSet<>();
                                list.add(term);
                                collisions.put(asciiTerm, list);
                            }

                            ts.end();   // Perform end-of-stream operations, e.g. set the final offset.
                        }

                    }
                }
            }

        }

        List<Set<String>> allTheLists = new ArrayList<>(collisions.values());
        Collections.sort(allTheLists, new Comparator<Set<String>>() {
            public int compare(Set<String> a1, Set<String> a2) {
                return a2.size() - a1.size(); // assumes you want biggest to smallest
            }
        });

        for (int i = 0; i < 1000; i++)
            System.out.println(allTheLists.get(i));

    }
}
