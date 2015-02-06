import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.io.File;
import java.util.List;

import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;

public class Main {

    private static Node node = nodeBuilder().local(true).node();
    private static Client client = node.client();

    public static void main(String[] args) {

        client.admin().indices().prepareDelete("ap_dataset").execute();

        CreateIndexResponse createIndexResponse = client.admin().indices()
                .prepareCreate("ap_dataset")
                .setSettings(Index.getIndexSettings()).execute().actionGet();
        // PutMappingResponse putMappingResponse = client.admin().indices()
        // .preparePutMapping(Index.getIndexMappings()).execute()
        // .actionGet();

        File folder = new File("ap89_collection");
        File[] files = folder.listFiles();

        for (File file : files) {
            List<String[]> builders = Parser.getBuilders(file);
            for (String[] builder : builders) {
                String id = builder[0];
                String json = builder[1];
                System.out.println("ID: " + id);
                System.out.println("json:" + json);
                IndexResponse indexResponse = client
                        .prepareIndex("ap_dataset", "document", id)
                        .setSource(json).execute().actionGet();
            }
        }
    }
}
