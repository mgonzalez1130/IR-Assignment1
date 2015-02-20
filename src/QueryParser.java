import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class QueryParser {

    private ArrayList<String> stopList;
    private String stopListPath;
    private static final int QUERY_START_INDEX = 4;

    public QueryParser(String stopListPath) {
        this.stopListPath = stopListPath;
        readStopList();
    }

    public ArrayList<String> parseQuery(String query) {
        query = query.replaceAll("[^a-zA-Z0-9\\s\\>\\<\\-\\/\\']", " ").trim();
        String[] queryArray = query.split("\\s+");
        ArrayList<String> result = new ArrayList<String>();

        result.add(queryArray[0]);
        for (int i = 4; i <= (queryArray.length - 1); i++) {
            if (stopList.contains(queryArray[i])) {
                continue;
            } else {
                result.add(queryArray[i]);
            }
        }
        return result;
    }

    private void readStopList() {
        stopList = new ArrayList<String>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(stopListPath));
        } catch (FileNotFoundException e) {
            System.out.println("File not found!");
            e.printStackTrace();
        }

        try {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.equals("")) {
                    continue;
                }
                stopList.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
