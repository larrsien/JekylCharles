package lars.com.browser;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lars.com.PetController;
import lars.com.UI.AmonWindow;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class JCSocketServer {
    private static final int PORT = 37842; // Фиксированный порт
    private final PetController petController;
    private final BrowserReactionLibrary reactionLibrary;
    private final Gson gson;
    private ServerSocket serverSocket;
    private Thread serverThread;
    private volatile boolean running;
    private long lastReactionTime = 0;
    private static final long COOLDOWN_MS = 1000;

    public JCSocketServer(PetController petController) {
        this.petController = petController;
        this.reactionLibrary = new BrowserReactionLibrary();
        this.gson = new Gson();
        this.running = false;
    }

    // мы запускаем сокет-сервер в отдельном потоке, чтобы приложение не блокировалось
    public void start() {
        if (running) {
            System.out.println("Socket server уже запущен");
            return;
        }

        serverThread = new Thread(() -> {
            try {
                serverSocket = new ServerSocket();
                serverSocket.setReuseAddress(true); // ← вот это добавь
                serverSocket.bind(new java.net.InetSocketAddress(PORT)); // ← и замени эту строку
                running = true;
                System.out.println("Socket Server запущен на порту " + PORT);

                while (running) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        System.out.println("Браузерное расширение подключилось");

                        // Обрабатываем каждое подключение в отдельном потоке
//                        new Thread(() -> handleClient(clientSocket)).start();
                        Thread clientThread = new Thread(() -> handleClient(clientSocket));
                        clientThread.setDaemon(true);
                        clientThread.start();

                    } catch (IOException e) {
                        if (running) {
                            System.err.println("Ошибка при приеме подключения: " + e.getMessage());
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Не удалось запустить Socket Server: " + e.getMessage());
            }}, "JCSocketServer");

        serverThread.setDaemon(true);
        serverThread.start();
    }

    private void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(
                     new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8), true)) {

            String line;
            while ((line = in.readLine()) != null) {
                handleMessage(line, out);
            }

        } catch (IOException e) {
            System.err.println("Ошибка при работе с клиентом: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
                System.out.println("Браузерное расширение отключилось");
            } catch (IOException e) {
                // игнорим
            }
        }
    }

    private void handleMessage(String messageJson, PrintWriter out) {
        try {
            JsonObject message = gson.fromJson(messageJson, JsonObject.class);
            String type = message.get("type").getAsString();

            System.out.println("Получено от расширения: " + type);

            // Пропускаем событие EXTENSION_CONNECTED без фильтрации
            if (type.equals("EXTENSION_CONNECTED")) {
                System.out.println("Расширение подключено!");
                sendResponse(out, "Амон приветствует Вас!");
                return;
            }

            // ФИЛЬТРАЦИЯ: Проверяем cooldown
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastReactionTime < COOLDOWN_MS) {
                System.out.println("Cooldown активен, событие пропущено");
                return;
            }

            String reaction = null;

            switch (type) {
                case "TAB_ACTIVATED":
                    // Сначала проверяем поисковый запрос (более специфично)
                    if (message.has("searchQuery") && !message.get("searchQuery").isJsonNull()) {
                        String searchQuery = message.get("searchQuery").getAsString();
                        reaction = reactionLibrary.getReactionBySearchQuery(searchQuery);
                    }

                    // Если по запросу ничего не нашли — смотрим на категорию сайта
                    if (reaction == null && message.has("category")) {
                        String category = message.get("category").getAsString();
                        reaction = reactionLibrary.getReaction(category);
                    }
                    break;

                case "TAB_CREATED":
                case "TAB_CLOSED":
                    reaction = reactionLibrary.getReaction(type);
                    break;

                default:
                    System.out.println("Неизвестный тип события: " + type);
                    break;
            }

            if (reaction != null) {
                lastReactionTime = currentTime; // Обновляем время последней реакции
                System.out.println("Амон говорит: " + reaction);
                petController.react(category);
                sendResponse(out, "Реакция показана: " + reaction);
            }

        } catch (Exception e) {
            System.err.println("Ошибка обработки сообщения: " + e.getMessage());
        }
    }

    private void sendResponse(PrintWriter out, String message) {
        JsonObject response = new JsonObject();
        response.addProperty("status", "ok");
        response.addProperty("message", message);
        response.addProperty("timestamp", System.currentTimeMillis());

        out.println(gson.toJson(response));
    }

    public void stop() {
        running = false;

        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                System.err.println("Ошибка при закрытии сервера: " + e.getMessage());
            }
        }

        if (serverThread != null) {
            serverThread.interrupt();
        }

        System.out.println("Socket Server остановлен");
    }
}
