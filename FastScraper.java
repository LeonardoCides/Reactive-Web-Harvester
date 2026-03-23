import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FastScraper {

    private final HttpClient httpClient;
    private final Pattern titlePattern = Pattern.compile("<title>(.*?)</title>", Pattern.CASE_INSENSITIVE);

    public FastScraper() {
        this.httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public void run(List<String> urls) {
        List<CompletableFuture<Void>> tasks = urls.stream()
                .map(url -> httpClient.sendAsync(
                        HttpRequest.newBuilder().uri(URI.create(url)).GET().build(),
                        HttpResponse.BodyHandlers.ofString()
                ).thenAccept(res -> {
                    Matcher m = titlePattern.matcher(res.body());
                    if (m.find()) {
                        System.out.println("Site: " + url + " | Título: " + m.group(1));
                    }
                })).toList();

        CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0])).join();
    }

    public static void main(String[] args) {
        new FastScraper().run(List.of(
            "https://www.google.com",
            "https://www.github.com"
        ));
    }
}
