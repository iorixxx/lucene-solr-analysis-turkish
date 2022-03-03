package org.apache.lucene.analysis.tr;

import com.google.common.base.Stopwatch;
import zemberek.core.logging.Log;
import zemberek.core.text.TextUtil;
import zemberek.core.turkish.PrimaryPos;
import zemberek.core.turkish.StemAndEnding;
import zemberek.core.turkish.Turkish;
import zemberek.core.turkish.TurkishAlphabet;
import zemberek.morphology.analysis.RuleBasedAnalyzer;
import zemberek.morphology.analysis.SingleAnalysis;
import zemberek.morphology.analysis.WordAnalysis;
import zemberek.morphology.lexicon.RootLexicon;
import zemberek.morphology.morphotactics.InformalTurkishMorphotactics;
import zemberek.morphology.morphotactics.TurkishMorphotactics;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * A variant of {@link zemberek.morphology.TurkishMorphology} simplified for a pre-tokenized input.
 */
public final class MyTurkishMorphology {

    private final RootLexicon lexicon;
    private final RuleBasedAnalyzer analyzer;
    private final TurkishMorphotactics morphotactics;


    private MyTurkishMorphology(MyTurkishMorphology.Builder builder) {

        this.lexicon = builder.lexicon;
        if (lexicon.isEmpty()) {
            Log.warn("TurkishMorphology class is being instantiated with empty root lexicon.");
        }

        this.morphotactics = builder.informalAnalysis ?
                new InformalTurkishMorphotactics(this.lexicon) : new TurkishMorphotactics(this.lexicon);

        this.analyzer = builder.ignoreDiacriticsInAnalysis ?
                RuleBasedAnalyzer.ignoreDiacriticsInstance(morphotactics) :
                RuleBasedAnalyzer.instance(morphotactics);

    }


    public static MyTurkishMorphology createWithDefaults() {
        Stopwatch sw = Stopwatch.createStarted();
        MyTurkishMorphology instance = new MyTurkishMorphology.Builder().setLexicon(RootLexicon.getDefault()).build();
        Log.info("Initialized in %d ms.", sw.elapsed(TimeUnit.MILLISECONDS));
        return instance;
    }

    public static zemberek.morphology.TurkishMorphology create(RootLexicon lexicon) {
        return new zemberek.morphology.TurkishMorphology.Builder().setLexicon(lexicon).build();
    }

    public TurkishMorphotactics getMorphotactics() {
        return morphotactics;
    }

    public WordAnalysis analyze(String word) {
        return analyzeWithoutCache(word);
    }

    public RootLexicon getLexicon() {
        return lexicon;
    }

    /**
     * Normalizes the input word and analyses it. If word cannot be parsed following occurs: - if
     * input is a number, system tries to parse it by creating a number DictionaryEntry. - if input
     * starts with a capital letter, or contains ['] adds a Dictionary entry as a proper noun. - if
     * above options does not generate a result, it generates an UNKNOWN dictionary entry and returns
     * a parse with it.
     *
     * @param word input word.
     * @return WordAnalysis list.
     */

    public static String normalizeForAnalysis(String word) {
        // TODO: This may cause problems for some foreign words with letter I.
        String s = word.toLowerCase(Turkish.LOCALE);
        s = TurkishAlphabet.INSTANCE.normalizeCircumflex(s);
        String noDot = s.replace(".", "");
        if (noDot.length() == 0) {
            noDot = s;

        }
        return TextUtil.normalizeApostrophes(noDot);
    }

    /**
     * This should be the entry point to stemming
     *
     * @param word a word to be stemmed
     * @return the stem of the word
     */
    List<SingleAnalysis> analyzeList(String word) {

        String s = normalizeForAnalysis(word);

        if (s.length() == 0) {
            System.out.println("empty " + word);
            return Collections.emptyList();
        }

        List<SingleAnalysis> result;

        if (TurkishAlphabet.INSTANCE.containsApostrophe(s)) {
            s = TurkishAlphabet.INSTANCE.normalizeApostrophe(s);
            result = analyzeWordsWithApostrophe(s);
        } else {
            result = analyzer.analyze(s);
        }

        if (result.size() == 0) {
            System.out.println("unknown word: " + word);
            return Collections.emptyList();
        }

        if (result.size() == 1 && result.get(0).getDictionaryItem().isUnknown()) {
            return Collections.emptyList();
        }

        return result;
    }

    private WordAnalysis analyzeWithoutCache(String word) {

        String s = normalizeForAnalysis(word);

        if (s.length() == 0) {
            System.out.println("empty " + word);
            return WordAnalysis.EMPTY_INPUT_RESULT;
        }

        List<SingleAnalysis> result;

        if (TurkishAlphabet.INSTANCE.containsApostrophe(s)) {
            s = TurkishAlphabet.INSTANCE.normalizeApostrophe(s);
            result = analyzeWordsWithApostrophe(s);
        } else {
            result = analyzer.analyze(s);
        }

        if (result.size() == 0) {
            System.out.println("unknown word: " + word);
            result = Collections.emptyList();
        }

        if (result.size() == 1 && result.get(0).getDictionaryItem().isUnknown()) {
            result = Collections.emptyList();
        }

        return new WordAnalysis(word, s, result);
    }

    public List<SingleAnalysis> analyzeWordsWithApostrophe(String word) {

        int index = word.indexOf('\'');

        if (index <= 0 || index == word.length() - 1) {
            return Collections.emptyList();
        }

        StemAndEnding se = new StemAndEnding(
                word.substring(0, index),
                word.substring(index + 1));

        String stem = TurkishAlphabet.INSTANCE.normalize(se.stem);

        String withoutQuote = word.replace("'", "");

        List<SingleAnalysis> noQuotesParses = analyzer.analyze(withoutQuote);
        if (noQuotesParses.size() == 0) {
            return Collections.emptyList();
        }

        // TODO: this is somewhat a hack.Correct here once we decide what to do about
        // words like "Hastanesi'ne". Should we accept Hastanesi or Hastane?
        return noQuotesParses.stream()
                .filter(
                        a -> a.getDictionaryItem().primaryPos == PrimaryPos.Noun &&
                                (a.containsMorpheme(TurkishMorphotactics.p3sg) || a.getStem().equals(stem)))
                .collect(Collectors.toList());
    }


    public static MyTurkishMorphology.Builder builder() {
        return new MyTurkishMorphology.Builder();
    }

    public static MyTurkishMorphology.Builder builder(RootLexicon lexicon) {
        return new MyTurkishMorphology.Builder().setLexicon(lexicon);
    }

    public static class Builder {

        RootLexicon lexicon = new RootLexicon();

        boolean informalAnalysis = false;
        boolean ignoreDiacriticsInAnalysis = false;

        public MyTurkishMorphology.Builder setLexicon(RootLexicon lexicon) {
            this.lexicon = lexicon;
            return this;
        }

        public MyTurkishMorphology.Builder setLexicon(String... dictionaryLines) {
            this.lexicon = RootLexicon.fromLines(dictionaryLines);
            return this;
        }

        public MyTurkishMorphology.Builder useInformalAnalysis() {
            this.informalAnalysis = true;
            return this;
        }

        public MyTurkishMorphology.Builder ignoreDiacriticsInAnalysis() {
            this.ignoreDiacriticsInAnalysis = true;
            return this;
        }

        public MyTurkishMorphology build() {
            return new MyTurkishMorphology(this);
        }
    }
}