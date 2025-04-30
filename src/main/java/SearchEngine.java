import java.util.*;
import InvertedIndex.*;
import Stemmer.Stemmer;
public class SearchEngine {
    private static final double SMALL_VALUE = 0.5; // Small value for terms not in collection

    private final InvertedIndex index;
    private final int totalDocs;

    public SearchEngine(InvertedIndex index) {
        this.index = index;
        this.totalDocs = index.getDocuments().size();
    }

    public List<Result> search(String query) {
      String[] queryTermsRaw = query.toLowerCase().split("\\W+");
      List<String> queryTerms = new ArrayList<>();
      for (String term : queryTermsRaw) {
            Stemmer stemmer = new Stemmer();
            if (!term.isBlank()) {
                stemmer.addString(term);
                stemmer.stem();
                queryTerms.add(stemmer.toString());
            }
        }

        Map<String, Double> queryVector = new HashMap<>();
        Map<Integer, Map<String, Double>> docVectors = new HashMap<>();

        for (String term : queryTerms) {
            List<Posting> postings = index.getIndex().getOrDefault(term, Collections.emptyList());
            // if the term is not in any documents make df(t) equal to a small value instead of completely skipping it
            double df = Math.max(postings.size(), SMALL_VALUE); // the number of docs containing the term
            double idf = Math.log10((double) totalDocs / df);

            // query vector: count term in query
            queryVector.put(term, queryVector.getOrDefault(term, 0.0) + 1);

            // build tf-idf vectors for documents
            for (Posting posting : postings) {
                double tf = 1 + Math.log10(posting.termFrequency);
                double tfidf = tf * idf;

                docVectors.putIfAbsent(posting.docId, new HashMap<>());
                docVectors.get(posting.docId).put(term, tfidf);
            }
        }

        // Apply TF-IDF weighting to the query
        for (String term : queryVector.keySet()) {
            List<Posting> postings = index.getIndex().getOrDefault(term, Collections.emptyList());
            double df = Math.max(postings.size(), SMALL_VALUE);
            double idf = Math.log10((double) totalDocs / df);
            double tf = 1 + Math.log10(queryVector.get(term));
            queryVector.put(term, tf * idf);
        }

        // Compute cosine similarity
        PriorityQueue<Result> results = new PriorityQueue<>(Comparator.comparingDouble(r -> -r.score));

        for (Map.Entry<Integer, Map<String, Double>> entry : docVectors.entrySet()) {
            int docId = entry.getKey();
            Map<String, Double> docVector = entry.getValue();

            double score = cosineSimilarity(queryVector, docVector);
            if (score > 0) {
                results.offer(new Result(docId, score));
            }
        }

        // Top 10 results
        List<Result> topResults = new ArrayList<>();
        for (int i = 0; i < 10 && !results.isEmpty(); i++) {
            topResults.add(results.poll());
        }

        return topResults;
    }

    private double cosineSimilarity(Map<String, Double> queryVec, Map<String, Double> docVec) {

        double dotProduct = 0, queryNorm = 0, docNorm = 0;

        for(String term : queryVec.keySet()) {
            double queryWeight = queryVec.get(term);
            double docWeight = docVec.getOrDefault(term , 0.0);
            dotProduct += queryWeight * docWeight;
        }

        for (double weight : queryVec.values()) {
            queryNorm += weight * weight;
        }

        // Calculate the norm of the document vector
        for (double weight : docVec.values()) {
            docNorm += weight * weight;
        }

        // avoid division by zero
        if (docNorm == 0 || queryNorm == 0) {
            return 0.0; // the cosine similarity is zero
        }

        // Return the normalized cosine similarity
        return dotProduct / Math.sqrt(queryNorm * docNorm);

    }

    public static class Result {
        public final int docId;
        public final double score;

        public Result(int docId, double score) {
            this.docId = docId;
            this.score = score;
        }

        @Override
        public String toString() {
            return "Doc #" + docId + " Score: " + score;
        }
    }
}
