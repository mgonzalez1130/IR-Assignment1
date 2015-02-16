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
import java.util.Map.Entry;


public class Models {

    private static double avgDocLength = 165.97319319792157;
    private static Map<String, Integer> docLengths;
    private static long vocabSize = 138220;
    private static int NUM_OF_DOCS = 84678;
    private static double K1 = 1.2;
    private static double K2 = 100;
    private static double B = 0.75;
    
    public Models () {
        docLengths = deserializeDocLengths();
    }
    
    public Map<String, Double> okapiTF (Map<String, Map<String, Integer>> termStats) {
        Map<String, Double> results = new HashMap<>();

        //iterate through the statistical results for each query term in the query
        Iterator termStatsIt = termStats.entrySet().iterator();
        while (termStatsIt.hasNext()) {
            Map.Entry pair = (Map.Entry) termStatsIt.next();
            String queryTerm = (String) pair.getKey();
            Map<String, Integer> queryTermStats = (Map<String, Integer>) pair.getValue();
            
            Integer ttf = queryTermStats.get("ttf");
            queryTermStats.remove("ttf");
            Integer df = queryTermStats.get("df");
            queryTermStats.remove("df");
            
            //iterate through map containing df, ttf, and tf's for all documents that matched
            //the current queryTerm
            Iterator queryTermStatsIt = queryTermStats.entrySet().iterator();
            while(queryTermStatsIt.hasNext()) {
                Map.Entry temStatsPair = (Map.Entry) queryTermStatsIt.next();
                String docId = (String) pair.getKey();
                Integer tf = (Integer) pair.getValue();
                
                if (!results.containsKey(docId)) {
                    results.put(docId, 0.0);
                }
                
                double okapiTermScore = tf / 
                        (tf + 0.5 + 
                                1.5*(docLengths.get(docId) / avgDocLength));
                
                results.put(docId, results.get(docId) + okapiTermScore);
                System.out.println("Okapi score for " + docId + ": " + okapiTermScore);
            }
        }
        return sortHashMapByValues((HashMap) results);
    }
    
    public Map<String, Double> tfIdf(Map<String, Map<String, Integer>> termStats) {
        HashMap<String, Double> results = new HashMap<>();

        //iterate through the statistical results for each query term in the query
        Iterator termStatsIt = termStats.entrySet().iterator();
        while (termStatsIt.hasNext()) {
            Map.Entry pair = (Map.Entry) termStatsIt.next();
            String queryTerm = (String) pair.getKey();
            Map<String, Integer> queryTermStats = (Map<String, Integer>) pair.getValue();
            
            Integer ttf = queryTermStats.get("ttf");
            queryTermStats.remove("ttf");
            Integer df = queryTermStats.get("df");
            queryTermStats.remove("df");
            
            //iterate through map containing df, ttf, and tf's for all documents that matched
            //the current queryTerm
            Iterator queryTermStatsIt = queryTermStats.entrySet().iterator();
            while(queryTermStatsIt.hasNext()) {
                Map.Entry temStatsPair = (Map.Entry) queryTermStatsIt.next();
                String docId = (String) pair.getKey();
                Integer tf = (Integer) pair.getValue();
                
                if (!results.containsKey(docId)) {
                    results.put(docId, 0.0);
                }
                
                double tfidfScore = tf / 
                        (tf + 0.5 + 
                                1.5*(docLengths.get(docId) / avgDocLength));
                tfidfScore = tfidfScore * Math.log(NUM_OF_DOCS / (double)df);
                
                results.put(docId, results.get(docId) + tfidfScore);
                System.out.println("TF-IDF score for " + docId + ": " + tfidfScore);
            }
        }
        return sortHashMapByValues(results);
    }
    
    public Map<String, Double> okapiBM25(Map<String, Map<String, Integer>> termStats) {
        HashMap<String, Double> results = new HashMap<>();

        //iterate through the statistical results for each query term in the query
        Iterator termStatsIt = termStats.entrySet().iterator();
        while (termStatsIt.hasNext()) {
            Map.Entry pair = (Map.Entry) termStatsIt.next();
            String queryTerm = (String) pair.getKey();
            Map<String, Integer> queryTermStats = (Map<String, Integer>) pair.getValue();
            
            Integer ttf = queryTermStats.get("ttf");
            queryTermStats.remove("ttf");
            Integer df = queryTermStats.get("df");
            queryTermStats.remove("df");
            
            //iterate through map containing df, ttf, and tf's for all documents that matched
            //the current queryTerm
            Iterator queryTermStatsIt = queryTermStats.entrySet().iterator();
            while(queryTermStatsIt.hasNext()) {
                Map.Entry temStatsPair = (Map.Entry) queryTermStatsIt.next();
                String docId = (String) pair.getKey();
                Integer tf = (Integer) pair.getValue();
                
                if (!results.containsKey(docId)) {
                    results.put(docId, 0.0);
                }
                
                double term1 = Math.log((NUM_OF_DOCS + 0.5) / (df + 0.5));
                double term2 = (tf + (K1*tf)) / 
                        (tf + K1*((1-B) + B * (docLengths.get(docId) / avgDocLength)));
                double term3 = (tf + (K2 * tf)) / (tf + K2);
                
                double okapiBM25Score = term1 * term2 * term3;
                
                results.put(docId, results.get(docId) + okapiBM25Score);
                System.out.println("Okapi BM25 score for " + docId + ": " + okapiBM25Score);
            }
        }
        return sortHashMapByValues(results);
    }
    
    public Map<String, Double> LMLaplace(Map<String, Map<String, Integer>> termStats) {
        HashMap<String, Double> results = new HashMap<>();

        //iterate through the statistical results for each query term in the query
        Iterator termStatsIt = termStats.entrySet().iterator();
        while (termStatsIt.hasNext()) {
            Map.Entry pair = (Map.Entry) termStatsIt.next();
            String queryTerm = (String) pair.getKey();
            Map<String, Integer> queryTermStats = (Map<String, Integer>) pair.getValue();
            
            Integer ttf = queryTermStats.get("ttf");
            queryTermStats.remove("ttf");
            Integer df = queryTermStats.get("df");
            queryTermStats.remove("df");
            
            //iterate through map containing df, ttf, and tf's for all documents that matched
            //the current queryTerm
            Iterator queryTermStatsIt = queryTermStats.entrySet().iterator();
            while(queryTermStatsIt.hasNext()) {
                Map.Entry temStatsPair = (Map.Entry) queryTermStatsIt.next();
                String docId = (String) pair.getKey();
                Integer tf = (Integer) pair.getValue();
                
                if (!results.containsKey(docId)) {
                    results.put(docId, 0.0);
                }
                
                double p_laplace = (tf + 1) / (docLengths.get(docId) + vocabSize);
                double LMLaplace = Math.log(p_laplace);
                
                results.put(docId, results.get(docId) + LMLaplace);
                System.out.println("LM with Laplace Smoothing score for " + docId + ": " + LMLaplace);
            }
        }
        return sortHashMapByValues(results);
    }
    
    public Map<String, Double> LMJM(Map<String, Map<String, Integer>> termStats) {
        HashMap<String, Double> results = new HashMap<>();

        //iterate through the statistical results for each query term in the query
        Iterator termStatsIt = termStats.entrySet().iterator();
        while (termStatsIt.hasNext()) {
            Map.Entry pair = (Map.Entry) termStatsIt.next();
            String queryTerm = (String) pair.getKey();
            Map<String, Integer> queryTermStats = (Map<String, Integer>) pair.getValue();
            
            Integer ttf = queryTermStats.get("ttf");
            queryTermStats.remove("ttf");
            Integer df = queryTermStats.get("df");
            queryTermStats.remove("df");
            
            //iterate through map containing df, ttf, and tf's for all documents that matched
            //the current queryTerm
            Iterator queryTermStatsIt = queryTermStats.entrySet().iterator();
            while(queryTermStatsIt.hasNext()) {
                Map.Entry temStatsPair = (Map.Entry) queryTermStatsIt.next();
                String docId = (String) pair.getKey();
                Integer tf = (Integer) pair.getValue();
                
                if (!results.containsKey(docId)) {
                    results.put(docId, 0.0);
                }
                
                double p_jm = ttf / vocabSize;
                double LMJMScore = Math.log(p_jm);
                
                results.put(docId, results.get(docId) + LMJMScore);
                System.out.println("LM with JM smoothing score for " + docId + ": " + LMJMScore);
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

                if (comp1.equals(comp2)){
                    passedMap.remove(key);
                    mapKeys.remove(key);
                    sortedMap.put((String)key, (Double)val);
                    break;
                }

            }

        }
        return sortedMap;
     }
}
