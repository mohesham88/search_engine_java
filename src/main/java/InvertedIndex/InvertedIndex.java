package InvertedIndex;

import java.util.*;
import Stemmer.Stemmer;



public class InvertedIndex {
    // term -> list of postings
    private final Map<String, List<Posting>> index = new HashMap<>();
    // docId -> raw document text
    private final Map<Integer, String> documents = new HashMap<>();

    public void addDocument(int docId, String text) {
        documents.put(docId, text);

        String[] tokens = tokenizeAndNormalize(text);
        Map<String, Integer> termFreqs = new HashMap<>();
        
        for (String token : tokens) {
            Stemmer stemmer = new Stemmer();
            if (token.isBlank()) continue;
            stemmer.addString(token);
            stemmer.stem();
            String stemmed = stemmer.toString();
            termFreqs.put(stemmed, termFreqs.getOrDefault(stemmed, 0) + 1);
        }

        for (Map.Entry<String, Integer> entry : termFreqs.entrySet()) {
            String term = entry.getKey();
            int tf = entry.getValue();

            List<Posting> postings = index.computeIfAbsent(term, k -> new ArrayList<>());

            Posting posting = new Posting(docId);
            posting.termFrequency = tf;
            postings.add(posting);
        }
    }

    private String[] tokenizeAndNormalize(String text) {
        return text.toLowerCase().split("\\W+"); // non-word characters
    }

    public Map<String, List<Posting>> getIndex() {
        return index;
    }

    public Map<Integer, String> getDocuments() {
        return documents;
    }
}
