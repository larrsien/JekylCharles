package lars.com.browser;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lars.com.PetController;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class JCSocketServer {
    private static final int PORT = 37842;
    private final PetController petController;
    private final BrowserReactionLibrary reactionLibrary;
    private final Gson gson;
    private ServerSocket serverSocket;
    private Thread serverThread;
    private volatile boolean running;
    private volatile long lastReactionTime = 0;
    private static final long COOLDOWN_MS = 1000;

    public JCSocketServer(PetController petController) {
        this.petController   = petController;
        this.reactionLibrary = new BrowserReactionLibrary();
        this.gson            = new Gson();
        this.running         = false;
    }

    public void start() {
        if (running) return;
        running = true;

        serverThread = new Thread(() -> {
            try {
                serverSocket = new ServerSocket();
                serverSocket.setReuseAddress(true);
                serverSocket.bind(new java.net.InetSocketAddress(PORT));
                System.out.println("Socket Server запущен на порту " + PORT);

                while (running) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        Thread t = new Thread(() -> handleClient(clientSocket));
                        t.setDaemon(true);
                        t.start();
                    } catch (IOException e) {
                        if (running) System.err.println("Ошибка подключения: " + e.getMessage());
                    }
                }
            } catch (IOException e) {
                System.err.println("Не удалось запустить Socket Server: " + e.getMessage());
                running = false;
            }
        }, "JCSocketServer");

        serverThread.setDaemon(true);
        serverThread.start();
    }

    private void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(
                     new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8), true)) {

            String line;
            while ((line = in.readLine()) != null) handleMessage(line, out);

        } catch (IOException e) {
            System.err.println("Ошибка клиента: " + e.getMessage());
        } finally {
            try { clientSocket.close(); } catch (IOException ignored) {}
        }
    }

    private void handleMessage(String messageJson, PrintWriter out) {
        try {
            JsonObject message = gson.fromJson(messageJson, JsonObject.class);
            String type = message.get("type").getAsString();

            if (type.equals("EXTENSION_CONNECTED")) {
                sendResponse(out, "Приветствуем!");
                return;
            }

            long now = System.currentTimeMillis();
            if (now - lastReactionTime < COOLDOWN_MS) return;

            String category = null;

            if (type.equals("TAB_ACTIVATED")) {
                // Сначала поисковый запрос
                if (message.has("searchQuery") && !message.get("searchQuery").isJsonNull()) {
                    category = reactionLibrary.getCategoryBySearchQuery(
                            message.get("searchQuery").getAsString());
                }
                // Потом категория сайта
                if (category == null && message.has("category")) {
                    category = reactionLibrary.getCategoryBySite(
                            message.get("category").getAsString());
                }
            }

            if (category != null && !category.equals("unknown")) {
                lastReactionTime = now;
                petController.react(category);
                sendResponse(out, "Реакция на: " + category);
            }

        } catch (Exception e) {
            System.err.println("Ошибка обработки: " + e.getMessage());
        }
    }

    private void sendResponse(PrintWriter out, String msg) {
        JsonObject r = new JsonObject();
        r.addProperty("status", "ok");
        r.addProperty("message", msg);
        r.addProperty("timestamp", System.currentTimeMillis());
        out.println(gson.toJson(r));
    }

    public void stop() {
        running = false;
        try { if (serverSocket != null) serverSocket.close(); } catch (IOException ignored) {}
        if (serverThread != null) serverThread.interrupt();
    }
}
