package org.apache.lucene;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.params.CommonParams;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


public class SolrSearcher {

    public SolrSearcher(String solrURL, String outputPath) {

        server = new HttpSolrServer(solrURL);

        this.outputPath = outputPath;

        Connection con = null;
        try {
            con = connectToMySQL();
            PreparedStatement psmt = con.prepareStatement("SELECT * FROM queries");
            ResultSet rs = psmt.executeQuery();
            while (rs.next()) {
                String id = rs.getString("QueryID");
                String title = rs.getString("Topic");
                String desc = rs.getString("Description");
                Topic topic = new Topic(id, title, desc);
                topics.add(topic);
            }
        } catch (SQLException ex) {
            throw new RuntimeException("", ex);
        } finally {
            try {
                if (con != null) con.close();
            } catch (SQLException e) {
                // ignore
            }
        }
    }

    public enum QueryLength {
        Short, Medium, QMS;

        public String toString() {
            switch (this) {
                case Short:
                    return "QS";
                case Medium:
                    return "QM";
                case QMS:
                    return "QMS";
            }
            return super.toString();
        }
    }

    /**
     * Constant <code>TURKISH_CHARACTERS={'\u00e7', '\u011f', '\u0131', '\u00f6', '\u015f', '\u00fc', '\u00c7', '\u011e', '\u0130', '\u00d6', '\u015e', '\u00dc'}</code>
     */
    public static final char[] TURKISH_CHARACTERS = {'\u00e7', '\u011f', '\u0131', '\u00f6', '\u015f', '\u00fc', '\u00c7', '\u011e', '\u0130', '\u00d6', '\u015e', '\u00dc'};
    /**
     * Constant <code>ENGLISH_CHARACTERS={'c', 'g', 'i', 'o', 's', 'u', 'C', 'G', 'I', 'O', 'S', 'U'}</code>
     */
    public static final char[] ENGLISH_CHARACTERS = {'c', 'g', 'i', 'o', 's', 'u', 'C', 'G', 'I', 'O', 'S', 'U'};

    private static final Map<Character, Character> characterMap = new HashMap<>();

    private HttpSolrServer server;
    private String defaultField = null;
    private final String outputPath;

    private ArrayList<Topic> topics = new ArrayList<>();

    private PrintWriter output;

    private QueryLength queryLength = QueryLength.Medium;

    public List<Topic> getTopics() {
        return this.topics;
    }

    public void setQueryLength(QueryLength queryLength) {
        this.queryLength = queryLength;
    }

    public void setDefaultField(String defaultField) {
        this.defaultField = defaultField;
    }

    public String getRunName() {
        return defaultField + "_" + queryLength.toString();
    }

    private void search(String q, String QueryID) throws SolrServerException {

        SolrQuery query = new SolrQuery(q.replace("?", ""));
        query.setParam(CommonParams.DF, defaultField);
        query.setFields("id", "score");
        query.setStart(0);
        query.setRows(1000);

        int i = 0;

        for (SolrDocument document : server.query(query).getResults()) {

            String docno = (String) document.getFieldValue("id");
            Float score = (Float) document.getFieldValue("score");

            output.println(QueryID + "\t0\tMilliyet_0105_v00_" + docno.trim() + "\t" + i + "\t" + score + "\tSTANDARD");
            i++;
        }

    }

    public String search() throws SolrServerException, FileNotFoundException {

        output = new PrintWriter(outputPath + getRunName() + "_submitted.txt");

        for (Topic topic : topics) {

            if (queryLength == QueryLength.Short)
                search(topic.title, topic.id);
            else if (queryLength == QueryLength.Medium)
                search(topic.desc, topic.id);
            else if (queryLength == QueryLength.QMS)
                search(topic.title + " " + topic.desc, topic.id);
        }
        output.close();
        return getRunName();

    }

    public String searchAsciify() throws SolrServerException, FileNotFoundException {

        output = new PrintWriter(outputPath + getRunName() + "_submitted.txt");

        for (Topic topic : topics) {

            if (queryLength == QueryLength.Short)
                search(asciify(topic.title), topic.id);
            else if (queryLength == QueryLength.Medium)
                search(asciify(topic.desc), topic.id);
            else if (queryLength == QueryLength.QMS)
                search(asciify(topic.title + " " + topic.desc), topic.id);
        }
        output.close();
        return getRunName();

    }

    public static String asciify(String string) {

        StringBuilder builder = new StringBuilder(string.length());

        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);

            if (characterMap.containsKey(c))
                builder.append(characterMap.get(c));
            else
                builder.append(c);


        }

        return builder.toString().toLowerCase(Locale.US);

    }

    static {
        for (int j = 0; j < TURKISH_CHARACTERS.length; j++)
            characterMap.put(TURKISH_CHARACTERS[j], ENGLISH_CHARACTERS[j]);


    }

    static void printAsciiTable() {
        System.out.println(characterMap.keySet());
        System.out.println(characterMap.values());

        for (char c : TURKISH_CHARACTERS) {
            System.out.print("\\symbol{");
            System.out.print((int) c);
            System.out.print("} & ");
        }
    }

    static final String[] deasciifiers = {"turkish_deascii", "zemberek2_deascii"};
    static final String[] stemmers = {"ns", "f5", "snowball", "zemberek2"};

    public static void main(String[] args) throws SolrServerException, FileNotFoundException {

        SolrSearcher searcher = new SolrSearcher("http://localhost:8983/solr/deascii/", "/Users/iorixxx/Dropbox/diacritic/");
        searcher.setQueryLength(SolrSearcher.QueryLength.Medium);

        for (String stemmer : stemmers) {

            searcher.setDefaultField("tr_" + stemmer);
            System.out.println(searcher.search());

            searcher.setDefaultField("ascii_" + stemmer);
            System.out.println(searcher.search());
        }


        for (String stemmer : stemmers) {
            for (String deasciifier : deasciifiers) {
                searcher.setDefaultField(deasciifier + "_" + stemmer);
                System.out.println(searcher.searchAsciify());
            }
        }
    }


    public Connection connectToMySQL() throws SQLException {
        MysqlDataSource ds = new MysqlDataSource();
        ds.setUser("mc");
        ds.setPassword("mc");
        ds.setDatabaseName("milliyetcollection");
        ds.setPortNumber(3306);
        ds.setServerName("localhost");
        ds.setEncoding("UTF-8");

        return ds.getConnection();
    }


    public static class Topic {

        final String id;
        final String title;
        final String desc;

        public Topic(String id, String title, String desc) {
            this.id = id;
            this.title = title;
            this.desc = desc;
        }
    }
}
