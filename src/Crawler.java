import java.net.*;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//    http://info.cern.ch/ 2

public class Crawler {
    private static LinkedList<URLDepthPair> openPairs = new LinkedList<URLDepthPair>(); // открытые пары
    private static LinkedList<URLDepthPair> closedPairs = new LinkedList<URLDepthPair>(); // закрытые пары


    // Функция для проверки корректности URL адреса
    public static String parseURL(String url) {
        try {
            new URL(url);
            return url;
        } catch (MalformedURLException error) {
            System.out.println("Неверная структура URL");
            return null;
        }
    }
   // Функция для проверки корректности глубины
    public static int parseDepth(String digit) {
        Pattern pattern = Pattern.compile("^\\d+$");
        Matcher match = pattern.matcher(digit);
        if (match.find())
            return Integer.parseInt(match.group());
        System.out.println("Неприемлимая глубина " + digit);
        return -1;
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java Crawler <URL> <depth>");
            System.exit(1);
        }
        String url = parseURL(args[0]);
        if (url == null) {
            System.exit(1);
        }
        int maxDepth = parseDepth(args[1]);
        if (maxDepth == -1) {
            System.exit(1);
        }
        // Добавляет первую ссылку в список необработанных ссылок
        openPairs.add(new URLDepthPair(url, 1));
        // Цикл исполняется пока есть необработанные ссылки
        while (!openPairs.isEmpty()) {
            SiteReader reader = new SiteReader(openPairs, maxDepth);
            LinkedList<URLDepthPair> temp = reader.read();
            closedPairs.addAll(openPairs);
            openPairs.clear();
            for (URLDepthPair p : temp) {
                if (!closedPairs.contains(p)) {
                    openPairs.add(p);
                }
            }
            for (URLDepthPair p : closedPairs) {
                System.out.println(p);
            }
        }
    }
}