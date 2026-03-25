package lars.com;

import lars.com.UI.AmonWindow;
import lars.com.browser.AmonSocketServer;
import lars.com.events.BugCloneEvent;
import lars.com.events.MealReminder;
import lars.com.graphic.SpriteManager;
import lars.com.monitoring.InternalProcessMonitoring;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {

        System.setProperty("jna.tmpdir", System.getProperty("java.io.tmpdir"));
        System.setProperty("jna.nounpack", "false");
        System.setProperty("java.io.tmpdir", System.getProperty("java.io.tmpdir"));

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.out.println("Не удалось установить Look and Feel: " + e.getMessage());
        }

        // Запускаем в EDT (Event Dispatch Thread)
        SwingUtilities.invokeLater(() -> {
            System.out.println("Запуск этого нереального магнум опуса");

            SpriteManager spriteManager = new SpriteManager();
            AmonWindow amonWindow = new AmonWindow(spriteManager);
            InternalProcessMonitoring processMonitoring = new InternalProcessMonitoring(amonWindow);
            MealReminder mealReminder = new MealReminder(amonWindow);
            BugCloneEvent bugCloneEvent = new BugCloneEvent(amonWindow, spriteManager);

            // Socket server для браузерного расширения
            AmonSocketServer socketServer = new AmonSocketServer(amonWindow);

            // Амон на экране
            amonWindow.show();

            // мониторинг
            processMonitoring.start();

            mealReminder.start();
            bugCloneEvent.start();

            // Запускаем socket server
            socketServer.start();

            // shutdown hook для корректного завершения
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Завершение работы приложения");
                processMonitoring.close();
                mealReminder.stop();
                amonWindow.cleanup();
                bugCloneEvent.stop();
                socketServer.stop();
                System.runFinalization();
                // небольшая пауза чтобы всё успело закрыться
                try { Thread.sleep(500); } catch (InterruptedException e) { /* ignore */ }
                Runtime.getRuntime().halt(0);
            }));

            System.out.println("Амон запущен успешно!");
        });
    }
}
