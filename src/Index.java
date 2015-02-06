public final class Index {

    public static String getIndexSettings() {
        String settings = "{" + "\"settings\": {" + "\"index\": {"
                + "\"store\": {" + "\"type\": \"default\"" + "},"
                + "\"number_of_shards\": 1," + "\"number_of_replicas\": 1"
                + "}," + "\"analysis\": {" + "\"analyzer\": {"
                + "\"my_english\": {" + "\"type\": \"english\","
                + "\"stopwords_path\": \"stoplist.txt\"" + "}" + "}" + "}"
                + "}" + "}";
        return settings;
    }

    public static String getIndexMappings() {
        String mappings = "{" + "\"mappings\": {" + "\"document\": {"
                + "\"properties\": {" + "\"docno\": {"
                + "\"type\": \"string\"," + "\"store\": true,"
                + "\"index\": \"not_analyzed\"" + "}," + "\"text\": {"
                + "\"type\": \"string\"," + "\"store\": true,"
                + "\"index\": \"analyzed\","
                + "\"term_vector\": \"with_positions_offsets_payloads\","
                + "\"analyzer\": \"my_english\"" + "}" + "}" + "}" + "}" + "}";
        return mappings;
    }
}
