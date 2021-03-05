package org.apache.lucene.tr;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tr.Zemberek3StemFilter;
import org.apache.lucene.analysis.tr.Zemberek3StemFilterFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import zemberek.morphology.TurkishMorphology;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class TestZemberek3StemFilter extends BaseTokenStreamTestCase {

    private static TurkishMorphology morphology;

    @BeforeClass
    private static void initialize() throws IOException {
        morphology = TurkishMorphology.createWithDefaults();
    }

    @Test
    public void testSomeWords() throws Exception {
        TokenStream stream = whitespaceMockTokenizer("kuş gribi aşısı ortaklar çekişme masalı TİCARETİN DE ARTMASI BEKLENİYOR");
        stream = new Zemberek3StemFilter(stream, morphology, "maxLength");
        assertTokenStreamContents(stream, new String[]{"kuş", "grip", "aşı", "ortak", "çekişme", "masal", "ticaret", "de", "artma", "beklen"});
    }

    @Test
    public void testUnrecognizedWords() throws Exception {
        TokenStream stream = whitespaceMockTokenizer("kuku euro");
        stream = new Zemberek3StemFilter(stream, morphology, "maxLength");
        assertTokenStreamContents(stream, new String[]{"kuku", "euro"});
    }

    @Test
    public void test4SP() throws Exception {

        Analyzer analyzer = CustomAnalyzer.builder()
                .withTokenizer("standard")
                .addTokenFilter("turkishlowercase")
                .addTokenFilter(Zemberek3StemFilterFactory.class)
                .build();

        System.out.println(getAnalyzedTokens("4S.P", analyzer));

        TokenStream stream = whitespaceMockTokenizer("4S.P");
        stream = new Zemberek3StemFilter(stream, morphology, "maxLength");
        assertTokenStreamContents(stream, new String[]{"4S.P"});
    }

    @AfterClass
    private static void clean() {
        morphology.invalidateCache();
        morphology = null;
    }

    /**
     * Modified from : http://lucene.apache.org/core/4_10_2/core/org/apache/lucene/analysis/package-summary.html
     */
    public static List<String> getAnalyzedTokens(String text, Analyzer analyzer) {

        final List<String> list = new ArrayList<>();
        try (TokenStream ts = analyzer.tokenStream("FIELD", new StringReader(text))) {

            final CharTermAttribute termAtt = ts.addAttribute(CharTermAttribute.class);
            ts.reset(); // Resets this stream to the beginning. (Required)
            while (ts.incrementToken())
                list.add(termAtt.toString());

            ts.end();   // Perform end-of-stream operations, e.g. set the final offset.
        } catch (IOException ioe) {
            throw new RuntimeException("happened during string analysis", ioe);
        }
        return list;
    }
}
