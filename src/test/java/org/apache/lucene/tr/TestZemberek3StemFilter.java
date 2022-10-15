package org.apache.lucene.tr;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.tests.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tr.MyTurkishMorphology;
import org.apache.lucene.analysis.tr.TurkishLowerCaseFilter;
import org.apache.lucene.analysis.tr.Zemberek3StemFilter;
import org.apache.lucene.analysis.tr.Zemberek3StemFilterFactory;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class TestZemberek3StemFilter extends BaseTokenStreamTestCase {

    private static final MyTurkishMorphology morphology = MyTurkishMorphology.createWithDefaults();

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

        System.out.println(getAnalyzedTokens("4g.x", analyzer));
        System.out.println(getAnalyzedTokens("0.25", analyzer));
        System.out.println(getAnalyzedTokens(".", analyzer));
        System.out.println(getAnalyzedTokens("bulun.duğunu", analyzer));
        assertTrue(getAnalyzedTokens(".", analyzer).isEmpty());

        identity("4g.x");
        identity("0.25");
        identity(".");

        TokenStream stream = whitespaceMockTokenizer("4S.P");
        stream = new TurkishLowerCaseFilter(stream);
        stream = new Zemberek3StemFilter(stream, morphology, "maxLength");
        assertTokenStreamContents(stream, new String[]{"4s.p"});
    }

    private void identity(String word) throws Exception {
        TokenStream stream = whitespaceMockTokenizer(word);
        stream = new TurkishLowerCaseFilter(stream);
        stream = new Zemberek3StemFilter(stream, morphology, "maxLength");
        assertTokenStreamContents(stream, new String[]{word});
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
