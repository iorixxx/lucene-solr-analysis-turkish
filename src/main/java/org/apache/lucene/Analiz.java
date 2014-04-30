package org.apache.lucene;

import net.zemberek.erisim.Zemberek;
import net.zemberek.tr.yapi.TurkiyeTurkcesi;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tr.ApostropheFilter;
import org.apache.lucene.analysis.tr.TurkishLowerCaseFilter;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by iorixxx on 4/29/14.
 */
public class Analiz {

  static private final Zemberek zemberek = new Zemberek(new TurkiyeTurkcesi());

  static List<String> getTerms(String text) throws IOException {
    List<String> list = new ArrayList<>();
    Reader reader = new StringReader(text);
    TokenStream stream = new StandardTokenizer(Version.LUCENE_48, reader);
    stream = new ApostropheFilter(stream);
    stream = new TurkishLowerCaseFilter(stream);

    CharTermAttribute termAttribute = stream.getAttribute(CharTermAttribute.class);

    stream.reset();
    while (stream.incrementToken()) {

      String term = termAttribute.toString();
      // System.out.println(term);
      list.add(term);
    }
    reader.close();
    return list;
  }

  public static void main(String[] args) throws IOException {

    List<SolrSearcher.Topic> topics = SolrSearcher.getTopics("/Users/iorixxx/Dropbox/queries.csv/");

    int totalCount = 0;

    int changedTokens = 0;
    for (SolrSearcher.Topic topic : topics) {

      String input = topic.desc;

      List<String> terms = getTerms(input);

      totalCount += terms.size();

      boolean flag = true;
      for (String term : terms) {
        String asciiTerm = SolrSearcher.asciify(term);

        if (!term.equals(asciiTerm)) {
          changedTokens++;
          flag = false;

          List<String> trList = Arrays.asList(zemberek.asciidenTurkceye(asciiTerm));

          if (trList.size() > 2)
            System.out.println(term + " " + asciiTerm);

         // if(!trList.contains(term))
         //   System.out.println(term + " " + asciiTerm + " trList " +  trList) ;
        }
      }

     // if (flag)
      //  System.out.println(topic.id + " " + input);
    }

    System.out.println("total count = " + totalCount);
    System.out.println("changed token count = " + changedTokens);
  }
}
