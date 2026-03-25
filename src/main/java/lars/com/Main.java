package lars.com;

import lars.com.graphic.SpriteManager;

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

        SwingUtilities.invokeLater(() -> {
            System.out.println("Запуск этого нереального магнум опуса");

            // Спрайт-менеджеры для каждого персонажа
            SpriteManager jekyllSprites  = new SpriteManager("jekyll");
            SpriteManager charlesSprites = new SpriteManager("sprites/charles");

            // Центральный контроллер
            PetController controller = new PetController(jekyllSprites, charlesSprites);

            // Мониторинг: теперь использует controller вместо amonWindow
            // TODO: обновить InternalProcessMonitoring чтобы принимал PetController
            //       вместо AmonWindow. Вызов будет:
            //         controller.react("discord")
            //       вместо:
            //         amonWindow.reactToEvent("Дискорд? Пусть...")

            // InternalProcessMonitoring processMonitoring = new InternalProcessMonitoring(controller);
            // MealReminder mealReminder = new MealReminder(controller);

            // BugCloneEvent — спавнит баг-клоны для Шарля (можно позже добавить и для Джекилла)
            // BugCloneEvent bugCloneEvent = new BugCloneEvent(controller, charlesSprites);

            // Socket server для браузерного расширения
            // JCSocketServer socketServer = new JCSocketServer(controller);
            // socketServer.start();

            // Shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Завершение работы приложения");
                // processMonitoring.close();
                // mealReminder.stop();
                controller.cleanup();
                // bugCloneEvent.stop();
                // socketServer.stop();
                System.runFinalization();
                try { Thread.sleep(500); } catch (InterruptedException e) { /* ignore */ }
                Runtime.getRuntime().halt(0);
            }));

            System.out.println("Персонажи запущены успешно!");
        });
    }
}
