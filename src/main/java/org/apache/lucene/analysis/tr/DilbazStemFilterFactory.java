package org.apache.lucene.analysis.tr;

import MorphologicalAnalysis.FsmMorphologicalAnalyzer;
import MorphologicalAnalysis.FsmParse;
import MorphologicalAnalysis.FsmParseList;
import MorphologicalDisambiguation.LongestRootFirstDisambiguation;
import MorphologicalDisambiguation.MorphologicalDisambiguator;

import java.util.List;
import java.util.Locale;

public class DilbazStemFilterFactory {
    static FsmMorphologicalAnalyzer fsm = new FsmMorphologicalAnalyzer();

    public static void main(String[] args) {

        String a = "0.25 4p.05 4p.x kuş asisi ortaklar çekişme masalı İCARETİN DE ARTMASI BEKLENİYOR\n" +
                "Savinykh, Ege Bölgesi Sanayi Odası'nda (EBSO) düzenlenen \"Belarus Türkiye Yatırım ve İşbirliği Olanakları Semineri\"nde yaptığı konuşmada, \" 2 Haziran'dan itibaren Türk halkı vizesiz olarak Belarus'a gidip gelebilecek. İki ülke arasındaki ticaret bu anlaşma ile daha da artacak\" dedi. Türkiye ile Belarus arasında ticari, kültürel ve sosyal ilişkilerin gelişmesini arzu ettiklerini kaydeden Andrei Savinykh, ülkesinin Kırgızistan ve Kazakistan ile Gümrük Birliği anlaşması bulunduğunu, önümüzdeki kuku birliğ";

        a = a.toLowerCase(Locale.forLanguageTag("tr"));

        for (String s : a.split("\\s+")) {
            parse(s);
        }
    }

    static void parse(String word) {

        FsmParseList fsmParseList = fsm.morphologicalAnalysis(word);

        System.out.println("found " + fsmParseList.size() + " many solutions for " + word);

        if (fsmParseList.size() == 0) return;


        System.out.println("longest " + fsmParseList.getParseWithLongestRootWord().getWord().getName() + " lemma " + fsmParseList.getParseWithLongestRootWord().getLastLemma());

        for (int i = 0; i < fsmParseList.size(); i++) {
            System.out.println(fsmParseList.getFsmParse(i).transitionList());
        }
        MorphologicalDisambiguator morphologicalDisambiguator = new LongestRootFirstDisambiguation();

        List<FsmParse> dis = morphologicalDisambiguator.disambiguate(new FsmParseList[]{fsmParseList});
        System.out.println("====disambiguator found " + dis.size() + " many candidates");

        for (FsmParse parse : dis) {
            System.out.println(parse.transitionList());
            System.out.println("stem: " + parse.getWord().getName());
            System.out.println("lemma: " + parse.getLastLemma());
        }
    }
}
