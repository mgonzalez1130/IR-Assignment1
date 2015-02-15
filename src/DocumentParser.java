import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DocumentParser {

    public static List<String[]> getBuilders(File file) {
        List<String[]> documents = new ArrayList<String[]>();

        // Initialize the BufferedReader
        BufferedReader docReader = null;
        try {
            docReader = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            System.out.println("File not found!");
            e.printStackTrace();
        }

        // iterate through all lines in the file
        try {
            String line;
            String docId = "";
            boolean readingText = false;
            StringBuilder textBuilder = new StringBuilder();
            while ((line = docReader.readLine()) != null) {
                // Note: my also need to include escape for apostrophe
                line = line.replaceAll("[^a-zA-Z0-9\\s\\>\\<\\-\\/\\']", " ")
                        .trim();
                if (line.equals(""))
                    continue;

                String[] splitLine = line.split("\\s+");
                if ((splitLine[0]).equals("<DOCNO>")) {
                    docId = splitLine[1];
                    // System.out.println("ID: " + docId);
                }
                if ((splitLine[0]).equals("<TEXT>")) {
                    readingText = true;
                    continue;
                }
                if ((splitLine[0]).equals("</TEXT>")) {
                    readingText = false;
                    continue;
                }
                if (readingText) {
                    textBuilder.append(line);
                    textBuilder.append(" ");
                    continue;
                }
                if ((splitLine[0]).equals("</DOC>")) {
                    documents.add(generateJson(docId, textBuilder));
                    textBuilder = new StringBuilder();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // close the BufferedReader
        try {
            docReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // System.out.println("Number of docs: " + documents.size());
        return documents;
    }

    private static String[] generateJson(String docId, StringBuilder textBuilder) {
        String[] result = new String[2];
        String json = "{" + "\"text\": " + "\"" + textBuilder.toString() + "\""
                + "}";
        result[0] = docId;
        result[1] = json;
        return result;
    }
}
