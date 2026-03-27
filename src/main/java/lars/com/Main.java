package lars.com;

import lars.com.browser.JCSocketServer;
import lars.com.events.BugCloneEvent;
import lars.com.graphic.SpriteManager;
import lars.com.monitoring.InternalProcessMonitoring;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {

        java.util.prefs.Preferences.userNodeForPackage(lars.com.UI.CharlesWindow.class)
                .putBoolean("surpriseRevealed", false);

        System.setProperty("jna.tmpdir", System.getProperty("java.io.tmpdir"));
        System.setProperty("jna.nounpack", "false");
        System.setProperty("java.io.tmpdir", System.getProperty("java.io.tmpdir"));

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.out.println("Не удалось установить Look and Feel: " + e.getMessage());
        }

        SwingUtilities.invokeLater(() -> {
            System.out.println("Запуск этого нереального магнум опуса");

            SpriteManager jekyllSprites  = new SpriteManager("jekyll");
            SpriteManager charlesSprites = new SpriteManager("charles");

            // Центральный контроллер (создаёт оба окна, очередь, синхронизацию)
            PetController controller = new PetController(jekyllSprites, charlesSprites);

            // Мониторинг процессов
            InternalProcessMonitoring processMonitoring = new InternalProcessMonitoring(controller);

            BugCloneEvent bugCloneEvent = new BugCloneEvent(controller.getCharles(), charlesSprites);

            // Socket server для браузерного расширения
            JCSocketServer socketServer = new JCSocketServer(controller);

            // Запускаем всё
            processMonitoring.start();
            bugCloneEvent.start();
            socketServer.start();

            // Shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Завершение работы приложения");
                processMonitoring.close();
                controller.cleanup();
                bugCloneEvent.stop();
                socketServer.stop();
                try { Thread.sleep(500); } catch (InterruptedException e) { /* ignore */ }
                Runtime.getRuntime().halt(0);
            }));

            System.out.println("Персонажи запущены успешно!");
        });
    }
}
