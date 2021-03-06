import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Models {

    private static double avgDocLength = 165.97319319792157;
    private static Map<String, Integer> docLengths;
    private static long vocabSize = 138220;
    private static int NUM_OF_DOCS = 84678;
    private static double K1 = 1.2;
    private static double K2 = 0.25;
    private static double B = 0;
    private static int df;
    private static int ttf;
    private static int collectionLength = 14054610;

    public Models() {
        docLengths = deserializeDocLengths();
    }

    public Map<String, Double> okapiTF(
            Map<String, Map<String, Integer>> termStats) {
        HashMap<String, Double> results = new HashMap<>();

        // iterate through the statistical results for each query term in the
        // query
        for (String queryTerm : termStats.keySet()) {
            Map<String, Integer> queryTermStats = termStats.get(queryTerm);

            ttf = queryTermStats.get("ttf");
            df = queryTermStats.get("df");

            // iterate through map containing df, ttf, and tf's for all
            // documents that matched
            // the current queryTerm
            for (String docId : queryTermStats.keySet()) {
                if (docId == "ttf") {
                    continue;
                }
                if (docId == "df") {
                    continue;
                }
                Integer tf = queryTermStats.get(docId);

                if (!results.containsKey(docId)) {
                    results.put(docId, 0.0);
                }

                double okapiTermScore = tf
                        / (tf + 0.5 + (1.5 * (docLengths.get(docId) / avgDocLength)));

                results.put(docId, results.get(docId) + okapiTermScore);
                // System.out.println("Okapi score for " + docId + ": " +
                // okapiTermScore);
            }
        }
        return sortHashMapByValues(results);
    }

    public Map<String, Double> tfIdf(Map<String, Map<String, Integer>> termStats) {
        HashMap<String, Double> results = new HashMap<>();

        // iterate through the statistical results for each query term in the
        // query
        for (String queryTerm : termStats.keySet()) {
            Map<String, Integer> queryTermStats = termStats.get(queryTerm);

            ttf = queryTermStats.get("ttf");
            df = queryTermStats.get("df");

            // iterate through map containing df, ttf, and tf's for all
            // documents that matched
            // the current queryTerm
            for (String docId : queryTermStats.keySet()) {
                if (docId == "ttf") {
                    continue;
                }
                if (docId == "df") {
                    continue;
                }
                Integer tf = queryTermStats.get(docId);

                if (!results.containsKey(docId)) {
                    results.put(docId, 0.0);
                }

                double tfidfScore = tf
                        / (tf + 0.5 + (1.5 * (docLengths.get(docId) / avgDocLength)));
                tfidfScore = tfidfScore * Math.log(NUM_OF_DOCS / (double) df);

                results.put(docId, results.get(docId) + tfidfScore);
                // System.out.println("TF-IDF score for " + docId + ": " +
                // tfidfScore);
            }
        }
        return sortHashMapByValues(results);
    }

    public Map<String, Double> okapiBM25(
            Map<String, Map<String, Integer>> termStats) {
        HashMap<String, Double> results = new HashMap<>();

        // iterate through the statistical results for each query term in the
        // query
        for (String queryTerm : termStats.keySet()) {
            Map<String, Integer> queryTermStats = termStats.get(queryTerm);

            ttf = queryTermStats.get("ttf");
            df = queryTermStats.get("df");

            // iterate through map containing df, ttf, and tf's for all
            // documents that matched
            // the current queryTerm
            for (String docId : queryTermStats.keySet()) {
                if (docId == "ttf") {
                    continue;
                }
                if (docId == "df") {
                    continue;
                }
                Integer tf = queryTermStats.get(docId);

                if (!results.containsKey(docId)) {
                    results.put(docId, 0.0);
                }

                double term1 = Math.log((NUM_OF_DOCS + 0.5) / (df + 0.5));
                double term2 = (tf + (K1 * tf))
                        / (tf + (K1 * ((1 - B) + (B * (docLengths.get(docId) / avgDocLength)))));
                double term3 = (tf + (K2 * tf)) / (tf + K2);

                double okapiBM25Score = term1 * term2 * term3;

                results.put(docId, results.get(docId) + okapiBM25Score);
                // System.out.println("Okapi BM25 score for " + docId + ": " +
                // okapiBM25Score);
            }
        }
        return sortHashMapByValues(results);
    }

    public Map<String, Double> LMLaplace(
            Map<String, Map<String, Integer>> termStats) {
        HashMap<String, Double> results = new HashMap<>();

        // iterate through the statistical results for each query term in the
        // query
        for (String queryTerm : termStats.keySet()) {
            Map<String, Integer> queryTermStats = termStats.get(queryTerm);

            ttf = queryTermStats.get("ttf");
            df = queryTermStats.get("df");

            // iterate through map containing df, ttf, and tf's for all
            // documents that matched
            // the current queryTerm
            for (String docId : queryTermStats.keySet()) {
                if (docId == "ttf") {
                    continue;
                }
                if (docId == "df") {
                    continue;
                }
                Integer tf = queryTermStats.get(docId);

                if (!results.containsKey(docId)) {
                    results.put(docId, 0.0);
                }

                double p_laplace = (tf + 1)
                        / ((double) docLengths.get(docId) + vocabSize);
                double LMLaplace = Math.log(p_laplace);
                results.put(docId, results.get(docId) + LMLaplace);
                // System.out.println("LM with Laplace Smoothing score for " +
                // docId + ": " + LMLaplace);
            }
        }

        for (String queryTerm : termStats.keySet()) {
            Map<String, Integer> queryTermStats = termStats.get(queryTerm);
            for (String resultsDocId : results.keySet()) {
                if (!queryTermStats.keySet().contains(resultsDocId)) {
                    Integer tf = 0;
                    double p_laplace = (tf + 1)
                            / ((double) docLengths.get(resultsDocId) + vocabSize);
                    double LMLaplace = Math.log(p_laplace);
                    results.put(resultsDocId, results.get(resultsDocId)
                            + LMLaplace);
                }
            }
        }

        return sortHashMapByValues(results);
    }

    public Map<String, Double> LMJM(
            Map<String, Map<String, Integer>> termStats, double lambda) {
        HashMap<String, Double> results = new HashMap<>();

        // System.out.println("Size before getting cl: " + docLengths.size());
        // Integer collectionLength = 0;
        // for (String docId : docLengths.keySet()) {
        // collectionLength += docLengths.get(docId);
        // }
        // System.out.println("collection length: " + collectionLength);
        // System.out.println("new size: " + docLengths.size());

        // iterate through the statistical results for each query term in the
        // query
        for (String queryTerm : termStats.keySet()) {
            Map<String, Integer> queryTermStats = termStats.get(queryTerm);

            ttf = queryTermStats.get("ttf");
            df = queryTermStats.get("df");

            // iterate through map containing df, ttf, and tf's for all
            // documents that matched
            // the current queryTerm
            for (String docId : queryTermStats.keySet()) {
                if (docId == "ttf") {
                    continue;
                }
                if (docId == "df") {
                    continue;
                }
                Integer tf = queryTermStats.get(docId);

                if (!results.containsKey(docId)) {
                    results.put(docId, 0.0);
                }

                double currentDocLength = docLengths.get(docId);
                lambda = currentDocLength / (currentDocLength + avgDocLength);
                double term1 = lambda * (tf / currentDocLength);
                double term2 = (1 - lambda) * (ttf / collectionLength);
                double LMJMScore = Math.log(term1 + term2);

                results.put(docId, results.get(docId) + LMJMScore);
                // System.out.println("LM with JM smoothing score for " +
                // docId
                // + ": " + LMJMScore);

            }
        }
        return sortHashMapByValues(results);
    }

    /**
     * Deserializes the docLenghts.ser file, which contains a Map with the
     * document lengths of every document in the corpus
     */
    private static Map<String, Integer> deserializeDocLengths() {

        Map<String, Integer> tempDocLengths = new HashMap<>();
        try {
            FileInputStream fileIn = new FileInputStream("docLenghts.ser");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            tempDocLengths = (Map<String, Integer>) in.readObject();
            in.close();
            fileIn.close();
        } catch (IOException i) {
            i.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return tempDocLengths;
    }

    // Returns the given HashMap as a LinkedHashMap sorted in descending order
    // This method was copied from a post on stack overflow found at:
    // http://stackoverflow.com/questions/8119366/sorting-hashmap-by-values
    private LinkedHashMap sortHashMapByValues(HashMap passedMap) {
        List mapKeys = new ArrayList(passedMap.keySet());
        List mapValues = new ArrayList(passedMap.values());
        Collections.sort(mapValues, Collections.reverseOrder());
        Collections.sort(mapKeys, Collections.reverseOrder());

        LinkedHashMap sortedMap = new LinkedHashMap();

        Iterator valueIt = mapValues.iterator();
        while (valueIt.hasNext()) {
            Object val = valueIt.next();
            Iterator keyIt = mapKeys.iterator();

            while (keyIt.hasNext()) {
                Object key = keyIt.next();
                String comp1 = passedMap.get(key).toString();
                String comp2 = val.toString();

                if (comp1.equals(comp2)) {
                    passedMap.remove(key);
                    mapKeys.remove(key);
                    sortedMap.put(key, val);
                    break;
                }

            }

        }
        return sortedMap;
    }
}
