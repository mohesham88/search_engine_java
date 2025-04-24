import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class Main {



    public static void main(String[] args) {

        List<String> seeds = Arrays.asList(
                "https://en.wikipedia.org/wiki/List_of_pharaohs",
                "https://en.wikipedia.org/wiki/Pharaoh"
        );

        Crawler crawler = new Crawler("https://en.wikipedia.org/wiki", 10, seeds);
        crawler.startCrawling();
    }
}
