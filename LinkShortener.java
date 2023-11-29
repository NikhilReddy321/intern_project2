import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Scanner;

public class LinkShortener {
    private static final String BASE_URL = "https://short.link/";
    private final Map<String, String> urlMap;
    private final SecureRandom secureRandom;

    public LinkShortener() {
        this.urlMap = new ConcurrentHashMap<>();
        this.secureRandom = new SecureRandom();
    }

    // Method to generate a secure random key
    private String generateRandomKey() {
        byte[] randomBytes = new byte[6];
        secureRandom.nextBytes(randomBytes);
        return bytesToBase62(randomBytes);
    }

    // Method to convert a byte array to base62
    private String bytesToBase62(byte[] bytes) {
        String characters = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        StringBuilder base62 = new StringBuilder();

        for (byte b : bytes) {
            int index = b & 0xFF;
            base62.append(characters.charAt(index % 62));
        }

        return base62.toString();
    }

    // Method to validate URL
    private boolean isValidUrl(String url) {
        try {
            new URI(url).toURL(); // Throws an exception if the URL is not valid
            return true;
        } catch (URISyntaxException | java.net.MalformedURLException e) {
            return false;
        }
    }

    // Method to persist the URL mapping
    private void saveUrlMapping(String shortKey, String longUrl) {
        urlMap.put(shortKey, longUrl);
    }

    // Method to encode the short key
    private String encodeShortKey(String shortKey) {
        return URLEncoder.encode(shortKey, StandardCharsets.UTF_8);
    }

    // Method to shorten a URL
    public String shortenUrl(String longUrl) {
        if (!isValidUrl(longUrl)) {
            System.err.println("\u001B[31mInvalid URL. Please enter a valid URL.\u001B[0m");
            return null;
        }

        String shortKey = generateRandomKey();
        String shortUrl = BASE_URL + encodeShortKey(shortKey);
        saveUrlMapping(shortKey, longUrl);

        // Display a colorful success message
        System.out.println("\u001B[32mShortened URL successfully created!\u001B[0m");

        return shortUrl;
    }

    // Method to decode the short key
    private String decodeShortKey(String encodedShortKey) {
        return encodedShortKey;
    }

    // Method to expand a URL
    public String expandUrl(String shortUrl) {
        String encodedShortKey = shortUrl.replace(BASE_URL, "");
        String shortKey = decodeShortKey(encodedShortKey);
        String longUrl = urlMap.get(shortKey);
        return (longUrl != null) ? longUrl : "\u001B[33mURL not found\u001B[0m";
    }

    public static void main(String[] args) {
        LinkShortener linkShortener = new LinkShortener();
        linkShortener.processUserInput();
    }

    private void processUserInput() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("===== \u001B[36mLink Shortener\u001B[0m =====");
            System.out.print("Enter a \u001B[34mlong URL\u001B[0m (type 'exit' to quit): ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("exit")) {
                System.out.println("\u001B[35mExiting...\u001B[0m");
                break;
            }

            String shortUrl = shortenUrl(input);

            if (shortUrl != null) {
                System.out.println("\u001B[32mShortened URL:\u001B[0m " + shortUrl);

                // Test expanding
                String expandedUrl = expandUrl(shortUrl);
                System.out.println("\u001B[34mExpanded URL:\u001B[0m " + expandedUrl);
            }
        }

        scanner.close();
    }
}
