package lars.com.events;

import lars.com.UI.AmonWindow;
import lars.com.UI.BugCloneWindow;
import lars.com.graphic.SpriteManager;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class BugCloneEvent {
    private final AmonWindow amonWindow;
    private final SpriteManager spriteManager;
    private final ScheduledExecutorService scheduler;
    private final Random random;

    private int lastDayOfYear = -1;
    private int bugsShownToday = 0;

    private static final int BUGS_PER_DAY = 2;
    private static final int MIN_SAFE_DISTANCE = 250; // Минимальное расстояние от основного Амона

    // минимум и максимум задержки между появлениями в минутах
    private static final int MIN_DELAY_MINUTES = 30;
    private static final int MAX_DELAY_MINUTES = 180;

    private ScheduledFuture<?> nextBugTask;

    public BugCloneEvent(AmonWindow amonWindow, SpriteManager spriteManager) {
        this.amonWindow = amonWindow;
        this.spriteManager = spriteManager;
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.random = new Random();
    }

    public void start() {
        scheduleNextBug();
    }

    private void scheduleNextBug() {
        // случайная задержка от мин до макс минут
        int delayMinutes = MIN_DELAY_MINUTES + random.nextInt(MAX_DELAY_MINUTES - MIN_DELAY_MINUTES);
        System.out.println("Следующий баг появится через " + delayMinutes + " минут");

        nextBugTask = scheduler.schedule(this::spawnAndReschedule, delayMinutes, TimeUnit.MINUTES);
    }

    private void spawnAndReschedule() {
        // сбрасываем счётчик если наступил новый день
        int currentDayOfYear = LocalDate.now().getDayOfYear();
        if (currentDayOfYear != lastDayOfYear) {
            bugsShownToday = 0;
            lastDayOfYear = currentDayOfYear;
        }
        // показываем только если лимит не исчерпан
        if (bugsShownToday < BUGS_PER_DAY) {
            spawnBugClone();
        }
        // планируем следующее появление в любом случае
        scheduleNextBug();
    }

    private void spawnBugClone() {
        SwingUtilities.invokeLater(() -> {
            Point bugPosition = getRandomSafePosition();
            if (bugPosition != null) {
                BugCloneWindow bugClone = new BugCloneWindow(spriteManager, bugPosition);
                bugClone.setVisible(true);
                bugsShownToday++;
            }
        });
    }

    private Point getRandomSafePosition() {
        Rectangle screenBounds = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getMaximumWindowBounds();
        int screenWidth = screenBounds.width;
        int screenHeight = screenBounds.height;

        Point amonLocation = amonWindow.getLocation();

        // Размеры окна Амона
        int amonWidth = 217;
        int amonHeight = 371;

        // Пытаемся найти безопасную позицию (максимум 10 попыток)
        for (int attempt = 0; attempt < 10; attempt++) {
            int x = screenBounds.x + random.nextInt(Math.max(1, screenWidth - amonWidth));
            int y = screenBounds.y + random.nextInt(Math.max(1, screenHeight - amonHeight));

            // Проверяем расстояние до основного Амона
            double distance = Math.sqrt(Math.pow(x - amonLocation.x, 2) + Math.pow(y - amonLocation.y, 2));

            if (distance >= MIN_SAFE_DISTANCE) {
                return new Point(x, y);
            }
        }
        // Если нет безопасной позиции, то null
        return null;
    }

    public void stop() {
        if (nextBugTask != null && !nextBugTask.isCancelled()) {
            nextBugTask.cancel(false);
        }
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
            }
        }
    }
}
