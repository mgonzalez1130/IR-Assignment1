import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.lucene.search.Explanation;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.MetricsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.cardinality.Cardinality;
import org.elasticsearch.search.facet.statistical.StatisticalFacet;

public class Main {

    private static double avgDocLength = 165.97319319792157;
    private static Map<String, Integer> docLengths = new HashMap<>();
    private static long vocabSize = 138220;
    private static int NUM_OF_DOCS = 84678;
    private static final int NUM_OF_QUERIES = 25;

    static Client client = new TransportClient()
            .addTransportAddress(new InetSocketTransportAddress("192.168.1.52",
                    9300));

    public static void main(String[] args) throws IOException {

        // buildIndex();
        getStatsForModels();
        processQueries();
    }

    private static void processQueries() {
        File file = new File("results.txt");
        try {
            file.createNewFile();
        } catch (IOException e1) {
            System.out.println("Problem creating results file");
            e1.printStackTrace();
        }
        QueryParser qp = new QueryParser("stoplist.txt");
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("query_desc.51-100.short.txt"));
        } catch (IOException e) {
            System.out.println("File not found!");
            e.printStackTrace();
        }
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(file);
        } catch (IOException e) {
            System.out.println("File not found for writer!");
            e.printStackTrace();
        }
        
        try {
            String line;
            Integer queryNumber = 1;
            while ((line = br.readLine()) != null) {
                System.out.println("Processing query number: " + queryNumber);
                Map<String, Map<String, Integer>> queryStats = new HashMap<>();
                ArrayList<String> queryTerms = qp.parseQuery(line);
                System.out.println("Terms in query: " + queryTerms.size());
                
                Integer queryCounter = 1;
                for (String query : queryTerms) {
                    System.out.println("Getting stats for term " + queryCounter 
                            + " in query " + queryNumber);
                    Map<String, Integer> termStats = getTermStats(query);
                    queryStats.put(query, termStats);
                    queryCounter++;
                }
                System.out.println("Finished getting stats for query terms.");
                printModelResults(queryStats, pw, queryNumber);
                queryNumber++;
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
        
        try {
            br.close();
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void printModelResults(
            Map<String, Map<String, Integer>> queryStats, PrintWriter bw, Integer queryNumber) {
        Models models = new Models();
 
        System.out.println("Starting Okapi TF");
        Map<String, Double> resultsMap = models.okapiTF(queryStats);
        writeToFile(bw, queryNumber, resultsMap);
        
        System.out.println("Starting TF-IDF");
        resultsMap = models.tfIdf(queryStats);
        writeToFile(bw, queryNumber, resultsMap);
        
        System.out.println("Starting Okapi BM25");
        resultsMap = models.okapiBM25(queryStats);
        writeToFile(bw, queryNumber, resultsMap);
        
        System.out.println("Starting LM-Laplace");
        resultsMap = models.LMLaplace(queryStats);
        writeToFile(bw, queryNumber, resultsMap);
        
        System.out.println("Starting LM-JM");
        resultsMap = models.LMJM(queryStats);
        writeToFile(bw, queryNumber, resultsMap);
    }

    private static void writeToFile(PrintWriter bw, Integer queryNumber,
            Map<String, Double> resultsMap) {
        Iterator resultsMapIt = resultsMap.entrySet().iterator();
        Integer rank = 1;
        Integer counter = 1;
        while (counter <= 100 && resultsMapIt.hasNext()) {
            Map.Entry pair = (Map.Entry)resultsMapIt.next();
            String docId = (String) pair.getKey();
            Double score = (Double) pair.getValue();
            String line = queryNumber + " Q0 " + docId + " " + rank + " " + score + " Exp";
            bw.println(line);
            rank++;
        }
    }

    /**
     * Builds the index. Includes a line to input the docId's into the
     * docLenghts map for easy access to a list of docId's
     */
    private static void buildIndex() {
        File folder = new File("ap89_collection");
        File[] files = folder.listFiles();

        for (File file : files) {
            System.out.println("Indexing " + file.getName());
            List<String[]> builders = DocumentParser.getBuilders(file);
            for (String[] builder : builders) {
                String id = builder[0];
                String json = builder[1];
                IndexResponse indexResponse = client
                        .prepareIndex("ap_dataset", "document", id)
                        .setSource(json).execute().actionGet();
                docLengths.put(id, 0);
            }
        }
        System.out.println("Finished indexing.");
    }

    /**
     * Gets the document lengths, average document length, and vocabulary size
     * from the index. Process was done once and values stored in fields above.
     *
     * @throws IOException
     */
    private static void getStatsForModels() throws IOException {
        // get doc lengths for every document - done once and serialized
        // serializeDocLengths();
        // System.out.println(docLengths.entrySet().size());
        deserializeDocLengths();

        // get vocabulary size - done once and saved
        // vocabSize = getVocabularySize(client, "ap_dataset", "document",
        // "text");
        // System.out.println("Vocab size: " + vocabSize);

        // get average doc length = done once and saved
        // StatisticalFacet f = getStatsOnTextTerms(client, "ap_dataset",
        // "document", null, null);
        // avgDocLength = f.getMean();
        // System.out.println("Average doc length: " + avgDocLength);
    }

    /**
     * Deserializes the docLenghts.ser file, which contains a Map with the
     * document lengths of every document in the corpus
     */
    private static void deserializeDocLengths() {

        try {
            FileInputStream fileIn = new FileInputStream("docLenghts.ser");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            docLengths = (Map<String, Integer>) in.readObject();
            in.close();
            fileIn.close();
        } catch (IOException i) {
            i.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    /**
     * Retrieves the docLength for all documents and inputs them into a
     * docLenghts Map. The Map is then serialized and saved to a file for easy
     * access without having to re-index the corpus.
     *
     * @throws IOException
     */
    private static void serializeDocLengths() throws IOException {
        Iterator it = docLengths.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            String docId = (String) pair.getKey();
            Integer docLength = (int) getStatsOnTextTerms(client, "ap_dataset",
                    "document", "_id", docId).getTotal();
            docLengths.put(docId, docLength);
        }

        try {
            FileOutputStream fileOut = new FileOutputStream("docLenghts.ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(docLengths);
            out.close();
            fileOut.close();
            System.out.println("Serialized docLenghts map");
        } catch (IOException i) {
            i.printStackTrace();
        }
    }

    /**
     * This function takes as an input a word that will be searched for in the
     * index using a match query. It will return a hashmap containing: a
     * document frequency value with the associated key "df"; a collection
     * frequency (total term frequency) value with the associated key "ttf"; and
     * a term frequency value for every document that matched the query term,
     * and with keys matching the docno for each document.
     *
     * @param queryTerm
     *            the word to be queried
     * @return a hashmap with df, ttf, and <docno, tf> pairs for every document
     *         that returned a match
     */
    private static Map<String, Integer> getTermStats(String queryTerm) {

        Map<String, Integer> results = new HashMap<>();

        SearchResponse scrollResp = client.prepareSearch()
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setScroll(new TimeValue(60000))
                .setQuery(QueryBuilders.matchQuery("text", queryTerm))
                .setExplain(true).setSize(1000).execute().actionGet();

        Integer df = (int) scrollResp.getHits().getTotalHits();
        // System.out.println("Executed search");
        // System.out.println("Total hits: " + df);
        // System.out.println(scrollResp.toString());

        results.put("df", df);

        Integer ttf = 0;
        for (SearchHit hit : scrollResp.getHits().getHits()) {
            String docno = hit.getId();
            int tf = getTermFrequency(hit.getExplanation());
            // System.out.println(docno);
            // System.out.println(tf);

            results.put(docno, tf);
            ttf += tf;
        }
        results.put("ttf", ttf);
        return results;
    }
    
    /**
     * Helper function for getting the term frequency from the explanation
     * @param explanation
     * @return
     */
    private static int getTermFrequency(Explanation explanation) {
        if ((explanation != null) && (explanation.getDetails() != null)) {
            for (Explanation newExplanation : explanation.getDetails()) {
                if (newExplanation.getDescription().startsWith("termFreq=")) {
                    return (int) newExplanation.getValue();
                }
                int tf = getTermFrequency(newExplanation);
                if (tf != -1) {
                    return tf;
                }
            }
        }
        return -1;
    }

    /**
     * V is the vocabulary size – the total number of unique terms in the
     * collection.
     *
     * @param client
     * @param index
     * @param type
     * @param field
     * @return
     */
    private static long getVocabularySize(Client client, String index,
            String type, String field) {
        MetricsAggregationBuilder aggregation = AggregationBuilders
                .cardinality("agg").field(field);
        SearchResponse sr = client.prepareSearch(index).setTypes(type)
                .addAggregation(aggregation).execute().actionGet();

        Cardinality agg = sr.getAggregations().get("agg");
        long value = agg.getValue();
        return value;

    }

    /**
     * get statistical facet by given docno or whole documents INFO including
     * following:
     *
     * @param client
     * @param index
     * @param type
     * @param matchedField
     * @param matchedValue
     * @return
     * @throws IOException
     */
    private static StatisticalFacet getStatsOnTextTerms(Client client,
            String index, String type, String matchedField, String matchedValue)
            throws IOException {
        XContentBuilder facetsBuilder;
        if ((matchedField == null) && (matchedValue == null)) {
            facetsBuilder = getStatsTermsBuilder();
        } else {
            facetsBuilder = getStatsTermsByMatchFieldBuilder(matchedField,
                    matchedValue);
        }
        SearchResponse response = client.prepareSearch(index).setTypes(type)
                .setSource(facetsBuilder).execute().actionGet();
        StatisticalFacet f = (StatisticalFacet) response.getFacets()
                .facetsAsMap().get("text");
        return f;
    }

    /**
     * builder for facets statistical terms length by given matched field, like
     * docno.
     *
     * @param matchField
     * @param matchValue
     * @return
     * @throws IOException
     */
    private static XContentBuilder getStatsTermsByMatchFieldBuilder(
            String matchField, String matchValue) throws IOException {
        XContentBuilder builder = XContentFactory.jsonBuilder();
        builder.startObject().startObject("query").startObject("match")
                .field(matchField, matchValue).endObject().endObject()
                .startObject("facets").startObject("text")
                .startObject("statistical")
                .field("script", "doc['text'].values.size()").endObject()
                .endObject().endObject().endObject();
        return builder;
    }

    /**
     * builder for the facets statistical terms length by whole documents.
     *
     * @return
     * @throws IOException
     */
    private static XContentBuilder getStatsTermsBuilder() throws IOException {
        XContentBuilder builder = XContentFactory.jsonBuilder();
        builder.startObject().startObject("query").startObject("match_all")
                .endObject().endObject().startObject("facets")
                .startObject("text").startObject("statistical")
                .field("script", "doc['text'].values.size()").endObject()
                .endObject().endObject().endObject();
        return builder;
    }

}
