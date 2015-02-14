import java.io.File;
import java.util.List;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

public class Main {

    // private static Node node = nodeBuilder().local(true).node();
    static Settings settings = ImmutableSettings.settingsBuilder()
            .put("client.transport.sniff", true).build();
    static Client client = new TransportClient(settings)
    .addTransportAddress(new InetSocketTransportAddress("10.0.0.12",
                    9300));

    public static void main(String[] args) {

        File folder = new File("ap89_collection");
        File[] files = folder.listFiles();

        for (File file : files) {
            System.out.println("Indexing " + file.getName());
            List<String[]> builders = Parser.getBuilders(file);
            for (String[] builder : builders) {
                String id = builder[0];
                String json = builder[1];
                // System.out.println("ID: " + id);
                // System.out.println("json:" + json);
                IndexResponse indexResponse = client
                        .prepareIndex("ap_dataset", "document", id)
                        .setSource(json).execute().actionGet();
            }
        }

        System.out.println("Finished indexing.");
    }
}
