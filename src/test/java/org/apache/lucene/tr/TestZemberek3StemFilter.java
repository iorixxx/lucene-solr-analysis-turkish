package org.apache.lucene.tr;

import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tr.Zemberek3StemFilter;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import zemberek.morphology.TurkishMorphology;

import java.io.IOException;

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

    @AfterClass
    private static void clean() {
        morphology.invalidateCache();
        morphology = null;
    }
}
