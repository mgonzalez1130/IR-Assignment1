import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.queryparser.flexible.core.builders.QueryBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryBuilders.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;

public class Main {

    static Client client = new TransportClient()
    .addTransportAddress(new InetSocketTransportAddress("10.15.70.79",
                    9300));

    public static void main(String[] args) {

//        File folder = new File("ap89_collection");
//        File[] files = folder.listFiles();
//
//        for (File file : files) {
//            System.out.println("Indexing " + file.getName());
//            List<String[]> builders = Parser.getBuilders(file);
//            for (String[] builder : builders) {
//                String id = builder[0];
//                String json = builder[1];
//                // System.out.println("ID: " + id);
//                // System.out.println("json:" + json);
//                IndexResponse indexResponse = client
//                        .prepareIndex("ap_dataset", "document", id)
//                        .setSource(json).execute().actionGet();
//            }
//        }
//
//        System.out.println("Finished indexing.");
        
    }
    
    private Map<String, Integer> getTermStats (String queryTerm) {
        
        Map<String, Integer> results = new HashMap<>();
        
        SearchResponse scrollResp = client.prepareSearch()
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setScroll(new TimeValue(60000))
                 .setQuery(QueryBuilders.matchQuery("text", queryTerm))
                .setExplain(true)
                .setSize(1000000).execute().actionGet(); 
        
        Integer df = (int) scrollResp.getHits().getTotalHits();
//        System.out.println("Executed search");
//        System.out.println("Total hits: " + df);
//        System.out.println(scrollResp.toString());
        
        results.put("df", df);

        Integer ttf = 0;
        for(SearchHit hit : scrollResp.getHits().getHits()) {
            String docno = (String) hit.getId();
            int tf = (int) hit.getExplanation()
                    .getDetails()[0]
                            .getDetails()[0]
                                    .getDetails()[0]
                                            .getValue();
//            System.out.println(docno);
//            System.out.println(tf);
            
            results.put(docno, tf);
            ttf += tf;
        }
        results.put("ttf", ttf);
        return results;
    }
}
