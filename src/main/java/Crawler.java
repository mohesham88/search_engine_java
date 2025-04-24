import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;

public class Crawler {

    private final String BASE_URL;
    private final int MAX_PAGES;
    private final Queue<String> queue;
    private final Set<String> visited;

    public Crawler(String baseUrl, int maxPages, List<String> seedUrls) {
        this.BASE_URL = baseUrl;
        this.MAX_PAGES = maxPages;
        this.queue = new LinkedList<>(seedUrls);
        this.visited = new HashSet<>();
    }


    public void startCrawling() {

        while (!queue.isEmpty() && visited.size() < MAX_PAGES) {
            String url = queue.poll();

            if (visited.contains(url)) {
                continue;
            }

            try {
                System.out.println("Crawling: " + url);
                Document doc = Jsoup.connect(url).get();
                visited.add(url);

                // Extract and filter links
                Elements links = doc.select("a[href]");
                for (Element link : links) {
                    String absHref = link.absUrl("href");


                    if (isValidLink(absHref) && !visited.contains(absHref)) {
                        queue.add(absHref);
                    }


                    if (visited.size()  >= MAX_PAGES ) {
                        break; // Prevent the queue from growing too large
                    }
                }

                // System.out.println(links.size() + " links found");
            } catch (IOException e) {
                System.out.println("Failed to crawl: " + url);
            }
        }

        System.out.println("\nCrawled Pages:");
        for (String page : visited) {
            System.out.println(page);
        }
    }

    private boolean isValidLink(String url) {
        // : to avoid :contact_us , :about , :contents ,
        // # to avoid urls like #body
        if (!url.startsWith(BASE_URL) ) return false;
        if (url.contains("#")) return false;

        String path = url.substring((BASE_URL).length());

        return !path.contains(":");
    }


}
