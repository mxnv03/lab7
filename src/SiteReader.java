import java.net.*;
import java.io.*;
import java.util.LinkedList;

/**Ридер сайтов считывает http ссылки в html документе*/
public class SiteReader {
    private final LinkedList<URLDepthPair> pairs;
    // константы
    private static final String HREF_HEAD = "href=\"";
    private static final String HREF_TAIL =  "\">";
    private static final String HTTP_STATE  =  "http";
    private static final String HTTPS_STATE =  "https";
    private static final int TIMEOUT = 1000;
    private final int maxDepth;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    // Конструктор для одной ссылки
    public SiteReader(URLDepthPair pair, int maxDepth){
        pairs = new LinkedList<URLDepthPair>();
        pairs.add(pair);
        this.maxDepth = maxDepth;
    }
    // Конструктор для списка ссылок, для многопоточности в дальнейшем
    public SiteReader(LinkedList<URLDepthPair> pairs, int maxDepth){
        this.pairs = pairs;
        this.maxDepth = maxDepth;
    }
    // Функция для создания сокета/подключения к сайту
    private boolean connect(URLDepthPair pair){
        try{
            socket = new Socket(pair.getHost(), 80);
        } catch (UnknownHostException e) {
            System.out.println("Неизвестный хост: " + pair.getHost());
            return false;
        } catch (IOException error) {
            return false;
        }
        return true;
    }

    // Функция для установки таймаута/времени после которого сокет перестанет пытаться считывать/получать информацию
    private boolean setTimeout(){
        try{
            socket.setSoTimeout(TIMEOUT);
        } catch (SocketException error) {
            System.out.println("Ошибка ввода-вывода при установке таймаута: " + error.getMessage());
            return false;
        }
        return true;
    }

    // Функция для получения потоков ввода вывода
    private boolean openStreams(){
        try{
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException error) {
            System.out.println("Ошибка ввода-вывода при открытии потоков: " + error.getMessage());
            return false;
        }
        return true;
    }

    // Функция для отправки запроса на сервер и чтения html документа
    private LinkedList<String> readHTML(URLDepthPair pair){
        LinkedList<String> lines = new LinkedList<String>();
        out.println("GET " + pair.getPath() + " HTTP/1.1");
        out.println("Host: " + pair.getHost());
        out.println("Connection: close");
        out.println();
        String line;
        try {
            while((line = in.readLine()) != null){
                lines.add(line);
            }
        } catch (IOException error) {
            System.out.println("Ошибка ввода-вывода: " + error.getMessage());
            return null;
        }
        return lines;
    }

    // Функция для закрытия сокета
    private boolean closeConnection(){
        try{
            socket.close();
        } catch (IOException error) {
            System.out.println("Ошибка ввода-вывода при закрытии сокета: " + error.getMessage());
            return false;
        }
        return true;
    }

    // Функция для получения ссылок из html документа
    public LinkedList<URLDepthPair> read(){
        LinkedList<URLDepthPair> list = new LinkedList<URLDepthPair>();
        for(URLDepthPair pair: pairs) {
            if (!connect(pair)) {
                continue;
            }
            if (!setTimeout()) {
                continue;
            }
            if (!openStreams()) {
                continue;
            }
            LinkedList<String> lines = readHTML(pair);
            if (lines == null) {
                continue;
            }
            if (!closeConnection()) {
                continue;
            }
            String link;
            URLDepthPair newPair;
            for (String hLine : lines) {
                int hrefIdx = hLine.indexOf(HREF_HEAD);
                int hrefEndIdx = hLine.indexOf(HREF_TAIL);
                if (hrefIdx == -1 || hrefEndIdx == -1 || (!hLine.contains(HTTP_STATE) && !hLine.contains(HTTPS_STATE)))
                    continue;
                if (hrefIdx + 6 > hrefEndIdx) continue;
                link = hLine.substring(hrefIdx + 6, hrefEndIdx);
                newPair = new URLDepthPair(link, pair.getDepth() + 1);
                if (pair.getDepth() + 1 <= maxDepth)
                    list.add(newPair);
            }
            closeConnection();
        }
        return list;
    }
}