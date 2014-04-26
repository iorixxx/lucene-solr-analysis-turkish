package org.apache.lucene.analysis.tr;

import net.zemberek.erisim.Zemberek;
import net.zemberek.islemler.KelimeKokFrekansKiyaslayici;
import net.zemberek.islemler.cozumleme.CozumlemeSeviyesi;
import net.zemberek.tr.yapi.TurkiyeTurkcesi;
import net.zemberek.yapi.Kelime;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.KeywordAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.analysis.util.TokenFilterFactory;
import org.apache.lucene.util.AttributeSource;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Factory for {@link Zemberek2DeasciifyFilter}.
 */
public class Zemberek2DeasciifyFilterFactory extends TokenFilterFactory {

    private final Zemberek zemberek = new Zemberek(new TurkiyeTurkcesi());
    static final String DEASCII_TOKEN_TYPE = "<DEASCII>";

    public Zemberek2DeasciifyFilterFactory(Map<String, String> args) {
        super(args);
        if (!args.isEmpty()) {
            throw new IllegalArgumentException("Unknown parameters: " + args);
        }
    }


    @Override
    public TokenStream create(TokenStream input) {
        return new Zemberek2DeasciifyFilter(input);
    }

    /**
     * Deasciifier based on <a href="https://code.google.com/p/zemberek">Zemberek2</a>
     * Modified from <a href="http://www.docjar.com/html/api/org/apache/lucene/wordnet/SynonymTokenFilter.java.html">
     * org.apache.lucene.wordnet.SynonymTokenFilter</a>
     */
    private final class Zemberek2DeasciifyFilter extends TokenFilter {

        private final CharTermAttribute termAttribute = addAttribute(CharTermAttribute.class);
        private final KeywordAttribute keywordAttribute = addAttribute(KeywordAttribute.class);
        private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);
        private final PositionIncrementAttribute posIncrAtt = addAttribute(PositionIncrementAttribute.class);

        private String[] stack = null;
        private int index = 0;
        private AttributeSource.State current = null;
        private int todo = 0;

        public Zemberek2DeasciifyFilter(TokenStream input) {
            super(input);
        }

        @Override
        public boolean incrementToken() throws IOException {

            while (todo > 0 && index < stack.length) { // pop from stack
                if (createToken(stack[index++], current)) {
                    todo--;
                    return true;
                }
            }

            if (!input.incrementToken()) return false;
            if (keywordAttribute.isKeyword()) return true;

            // stack = zemberek.asciidenTurkceye(termAttribute.toString());

            Kelime[] kelimeler = zemberek.asciiToleransliCozumleyici().cozumle(termAttribute.toString(), CozumlemeSeviyesi.TUM_KOKLER);
            Arrays.sort(kelimeler, new KelimeKokFrekansKiyaslayici());

            ArrayList<String> olusumlar = new ArrayList<>(kelimeler.length);

            for (Kelime kelime : kelimeler) {
                String olusum = kelime.icerikStr();
                if (!olusumlar.contains(olusum))
                    olusumlar.add(olusum);
            }

            olusumlar.remove(termAttribute.toString());
            stack = olusumlar.toArray(new String[olusumlar.size()]);

            index = 0;
            current = captureState();
            todo = stack.length;
            return true;
        }

        protected boolean createToken(String synonym, AttributeSource.State current) {
            restoreState(current);
            termAttribute.setEmpty().append(synonym);
            typeAtt.setType(DEASCII_TOKEN_TYPE);
            posIncrAtt.setPositionIncrement(0);
            return true;
        }

        @Override
        public void reset() throws IOException {
            super.reset();
            stack = null;
            index = 0;
            current = null;
            todo = 0;
        }
    }


    public static void main(String[] args) throws IOException {

        StringReader reader = new StringReader("kus asisi ortaklar çekişme masali");

        Map<String, String> map = new HashMap<>();


        Zemberek2DeasciifyFilterFactory factory = new Zemberek2DeasciifyFilterFactory(map);

        TokenStream stream = factory.create(new WhitespaceTokenizer(Version.LUCENE_47, reader));

        CharTermAttribute termAttribute = stream.getAttribute(CharTermAttribute.class);

        stream.reset();
        while (stream.incrementToken()) {

            String term = termAttribute.toString();
            System.out.println(term);
        }
    }
}


