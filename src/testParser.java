import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class testParser {

    public static void main(String[] args) {
        getBuilders("ap890103");
    }

    public static void getBuilders(String string) {

        System.out.println("Reading file");

        // Initialize the BufferedReader
        BufferedReader docReader = null;
        try {
            docReader = new BufferedReader(new FileReader(string));
            System.out.println("Initialized BufferedReader");
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
                line = line.replaceAll("[^a-zA-Z0-9\\s\\>\\<\\-\\/\\']", " ")
                        .trim();
                if (line.equals(""))
                    continue;

                String[] splitLine = line.split("\\s+");
                if ((splitLine[0]).equals("<DOCNO>")) {
                    docId = splitLine[1];
                    // System.out.println("ID: " + docId);
                }
                if (docId.equals("AP890103-0215")) {
                    System.out.println(line);
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
                    // System.out.println(textBuilder.toString());
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
    }
}
