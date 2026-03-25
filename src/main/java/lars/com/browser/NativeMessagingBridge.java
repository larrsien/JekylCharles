package lars.com.browser;

import com.google.gson.Gson;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

import com.google.gson.JsonObject;

import java.net.ConnectException;

public class NativeMessagingBridge {
    private static final String HOST = "localhost";
    private static final int PORT = 37842;
    private static final int MAX_CONNECTION_ATTEMPTS = 3;
    private static final int RETRY_DELAY_MS = 1000;
    private final Gson gson;

    public NativeMessagingBridge() {
        this.gson = new Gson();
    }

    public static void main(String[] args) {
        System.err.println("Native Messaging Bridge запускается...");

        NativeMessagingBridge bridge = new NativeMessagingBridge();

        try {
            bridge.run();
        } catch (Exception e) {
            System.err.println("Ошибка в Native Messaging Bridge: " + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private void run() throws IOException {
        // Пытаемся подключиться к основному приложению
        Socket socket = null;

        for (int attempt = 1; attempt <= MAX_CONNECTION_ATTEMPTS; attempt++) {
            try {
                System.err.println("Попытка подключения " + attempt + "/" + MAX_CONNECTION_ATTEMPTS);
                socket = new Socket(HOST, PORT);
                System.err.println("Подключено к Socket Server");
                break;
            } catch (ConnectException e) {
                System.err.println("Основное приложение не запущено");

                if (attempt < MAX_CONNECTION_ATTEMPTS) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                } else {
                    // После всех попыток отправляем ошибку в браузер и корректно завершаемся
                    sendErrorToBrowser();
                    System.exit(0); // Корректное завершение, не ошибка
                    return;
                }
            }
        }

        try (Socket finalSocket = socket;
             BufferedReader socketIn = new BufferedReader(new InputStreamReader(finalSocket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter socketOut = new PrintWriter(new OutputStreamWriter(finalSocket.getOutputStream(), StandardCharsets.UTF_8), true)) {

            // Читаем сообщения от браузера и пересылаем в сокет
            Thread readerThread = new Thread(() -> {
                try {
                    readFromBrowser(socketOut);
                } catch (IOException e) {
                    System.err.println("Ошибка чтения от браузера: " + e.getMessage());
                }
            });
            readerThread.start();

            // Читаем ответы от сокета и отправляем в браузер
            String line;
            while ((line = socketIn.readLine()) != null) {
                writeToBrowser(line);
            }

            readerThread.join();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void sendErrorToBrowser() throws IOException {
        JsonObject error = new JsonObject();
        error.addProperty("status", "error");
        error.addProperty("message", "Основное приложение не запущено. Пожалуйста, запустите его.");
        error.addProperty("timestamp", System.currentTimeMillis());

        writeToBrowser(gson.toJson(error));
    }

    private void readFromBrowser(PrintWriter socketOut) throws IOException {
        InputStream in = System.in;

        while (true) {
            // Читаем длину сообщения (4 байта, little-endian)
            byte[] lengthBytes = new byte[4];
            int bytesRead = in.read(lengthBytes);

            if (bytesRead != 4) {
                System.err.println("Не удалось прочитать длину сообщения");
                break;
            }

            int messageLength = ByteBuffer.wrap(lengthBytes).order(ByteOrder.LITTLE_ENDIAN).getInt();

            if (messageLength <= 0 || messageLength > 1024 * 1024) {
                System.err.println("Некорректная длина сообщения: " + messageLength);
                break;
            }

            // Читаем само сообщение
            byte[] messageBytes = new byte[messageLength];
            int totalRead = 0;
            while (totalRead < messageLength) {
                int read = in.read(messageBytes, totalRead, messageLength - totalRead);
                if (read == -1) {
                    System.err.println("Поток завершился преждевременно");
                    return;
                }
                totalRead += read;
            }

            String messageJson = new String(messageBytes, StandardCharsets.UTF_8);
            System.err.println("От браузера: " + messageJson);

            // Пересылаем в сокет
            socketOut.println(messageJson);
        }
    }

    private void writeToBrowser(String responseJson) throws IOException {
        System.err.println("В браузер: " + responseJson);

        byte[] messageBytes = responseJson.getBytes(StandardCharsets.UTF_8);

        // Отправляем длину (4 байта, little-endian)
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(messageBytes.length);
        System.out.write(buffer.array());

        // Отправляем сообщение
        System.out.write(messageBytes);
        System.out.flush();
    }
}
