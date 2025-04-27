import java.net.URL;
import java.util.*;
import InvertedIndex.InvertedIndex;

public class Main {
    private static final int SNIPPET_LENGTH = 150;

    public static void main(String[] args) {
        // Initialize components
        List<String> seeds = Arrays.asList(
                "https://en.wikipedia.org/wiki/List_of_pharaohs",
                "https://en.wikipedia.org/wiki/Pharaoh"
        );

        // Create and run crawler
        Crawler crawler = new Crawler("https://en.wikipedia.org/wiki", 10, seeds);
        crawler.startCrawling();

        // Create inverted index
        InvertedIndex index = new InvertedIndex();
        
        // Create URL to docId mapping
        Map<String, Integer> urlToDocId = new HashMap<>();
        int docId = 0;
        
        // Index the crawled pages
        for (Map.Entry<String, String> entry : crawler.getPageContents().entrySet()) {
            urlToDocId.put(entry.getKey(), docId);
            index.addDocument(docId++, entry.getValue());
        }

        // Create search engine
        SearchEngine searchEngine = new SearchEngine(index);

        // Simple search interface
        Scanner scanner = new Scanner(System.in);
        System.out.println("\nSearch Engine Ready! Enter your search query (or 'quit' to exit):");
        
        while (true) {
            System.out.print("\nSearch: ");
            String query = scanner.nextLine().trim();
            
            if (query.equalsIgnoreCase("quit")) {
                break;
            }
            
            if (query.isEmpty()) {
                continue;
            }

            // Perform search
            List<SearchEngine.Result> results = searchEngine.search(query);
            
            // Display results
            if (results.isEmpty()) {
                System.out.println("No results found.");
            } else {
                System.out.println("\nSearch Results:");
                for (int i = 0; i < results.size(); i++) {
                    SearchEngine.Result result = results.get(i);
                    
                    // Find the URL for this docId
                    String url = urlToDocId.entrySet().stream()
                            .filter(e -> e.getValue() == result.docId)
                            .map(Map.Entry::getKey)
                            .findFirst()
                            .orElse("Unknown URL");
                    
                    String title = crawler.getPageTitles().get(url);
                    String content = crawler.getPageContents().get(url);
                    
                    // Generate a snippet around the first occurrence of any query term
                    String snippet = generateSnippet(content, query);
                    
                    // Print formatted result
                    System.out.println("\n" + (i + 1) + ". " + title);
                    System.out.println("URL: " + url);
                    System.out.println("Score: " + String.format("%.4f", result.score));
                    System.out.println("Snippet: " + snippet);
                    System.out.println("----------------------------------------");
                }
            }
        }
        
        scanner.close();
        System.out.println("\nGoodbye!");
    }

    private static String generateSnippet(String content, String query) {
        String[] terms = query.toLowerCase().split("\\W+");
        String lowerContent = content.toLowerCase();
        
        // Find the first occurrence of any query term
        int firstPos = -1;
        for (String term : terms) {
            int pos = lowerContent.indexOf(term);
            if (pos != -1 && (firstPos == -1 || pos < firstPos)) {
                firstPos = pos;
            }
        }
        
        if (firstPos == -1) {
            return content.substring(0, Math.min(SNIPPET_LENGTH, content.length())) + "...";
        }
        
        // Get a window around the first occurrence
        int start = Math.max(0, firstPos - SNIPPET_LENGTH/2);
        int end = Math.min(content.length(), firstPos + SNIPPET_LENGTH/2);
        
        String snippet = content.substring(start, end);
        if (start > 0) snippet = "..." + snippet;
        if (end < content.length()) snippet = snippet + "...";
        
        return snippet;
    }
}
