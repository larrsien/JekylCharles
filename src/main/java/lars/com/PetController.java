package lars.com;

import lars.com.UI.CharlesWindow;
import lars.com.UI.JekyllWindow;
import lars.com.dialogue.DialogueQueue;
import lars.com.graphic.SpriteManager;
import lars.com.model.CharacterId;
import lars.com.model.DialogueLine;
import lars.com.reactions.DualResponseLibrary;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * PetController — центральная точка входа приложения.
 *
 * Заменяет старый Main / AmonWindow как точку старта.
 * Создаёт:
 *  - DialogueQueue с функцией-маршрутизатором бабла
 *  - JekyllWindow (спрайты Джекилла)
 *  - CharlesWindow (спрайты Шарля + всё меню)
 *  - DualResponseLibrary
 *  - ProcessMonitor, BrowserMonitor, MealReminder — все они теперь
 *    вызывают controller.react(category) вместо amonWindow.reactToEvent()
 *
 * ── Позиционирование ────────────────────────────────────────────────────────
 *  По умолчанию Джекилл — правый нижний угол, Шарль — чуть левее.
 *  Оба перетаскиваются независимо.
 * ──────────────────────────────────────────────────────────────────────────
 */
public class PetController {

    private final DialogueQueue        queue;
    private final JekyllWindow         jekyll;
    private final CharlesWindow        charles;
    private final DualResponseLibrary  library;

    // ──────────────────────────────────────────────────────────────────────────
    //  Конструктор
    // ──────────────────────────────────────────────────────────────────────────

    public PetController(SpriteManager jekyllSprites,
                         SpriteManager charlesSprites) {

        library = DualResponseLibrary.createDefault();

        // Маршрутизатор: очередь знает, чей showBubble() вызвать
        queue = new DialogueQueue(line -> {
            if (line.speaker == CharacterId.JEKYLL) {
                jekyll.showBubble(line.text);
            } else {
                charles.showBubble(line.text);
            }
        });

        // Фразы для ПКМ Джекилла — определяем здесь, передаём в конструктор
        List<String> jekyllRightClickPhrases = List.of(
                "...",
                "Зачем Вы это делаете?",
                "Хм.",
                "Я всё вижу.",
                "Правая кнопка мыши. Интересный выбор.",
                "Вы ожидали меню? Его здесь нет.",
                "Я не Шарль. Запомните это.",
                "Снова?",
                "Любопытство — это хорошо. Иногда."
        );

        jekyll  = new JekyllWindow(jekyllSprites, queue, jekyllRightClickPhrases);
        charles = new CharlesWindow(charlesSprites, queue);

        positionWindows();

        jekyll.setVisible(true);
        charles.setVisible(true);
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Реакция на события (вызывается ProcessMonitor, BrowserMonitor и т.д.)
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Основной метод реакции на системное событие.
     * Получает категорию (напр. "browser", "discord"), берёт случайный вариант
     * из библиотеки и добавляет его в очередь.
     */
    public void react(String category) {
        if (!library.hasCategory(category)) {
            category = "default";
        }
        List<DialogueLine> lines = library.getResponse(category);
        if (!lines.isEmpty()) {
            queue.addAll(lines);
        }
    }

    /**
     * Прямое добавление одной реплики — для MealReminder и TimerWidget.
     */
    public void reactAs(CharacterId who, String text) {
        queue.add(new DialogueLine(who, text));
    }

    /**
     * Прямая реплика Шарля (совместимость со старым reactToEvent).
     */
    public void charlesReact(String text) {
        reactAs(CharacterId.CHARLES, text);
    }

    /**
     * Прямая реплика Джекилла.
     */
    public void jekyllReact(String text) {
        reactAs(CharacterId.JEKYLL, text);
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Позиционирование
    // ──────────────────────────────────────────────────────────────────────────

    private void positionWindows() {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(
                GraphicsEnvironment.getLocalGraphicsEnvironment()
                        .getDefaultScreenDevice().getDefaultConfiguration());

        int bottom = screen.height - insets.bottom;
        int charlesX = screen.width  - charles.getWidth()  - 20;
        int jekyllX  = charlesX      - jekyll.getWidth()   - 10;

        // placeAt запоминает baseY для плавающей анимации
        charles.placeAt(charlesX, bottom - charles.getHeight());
        jekyll.placeAt(jekyllX,   bottom - jekyll.getHeight());
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Getters (для MealReminder, ProcessMonitor и прочих)
    // ──────────────────────────────────────────────────────────────────────────

    public CharlesWindow getCharles() { return charles; }
    public JekyllWindow getJekyll()  { return jekyll;  }
    public DialogueQueue getQueue()   { return queue;   }

    // ──────────────────────────────────────────────────────────────────────────
    //  Точка входа (если запускать без старого Main)
    // ──────────────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Замените на реальные SpriteManager'ы для каждого персонажа
            SpriteManager jekyllSprites  = new SpriteManager("jekyll");
            SpriteManager charlesSprites = new SpriteManager("charles");

            PetController controller = new PetController(jekyllSprites, charlesSprites);

            // ProcessMonitor, BrowserMonitor и т.д.:
            // Было:   amonWindow.reactToEvent(message)
            // Стало:  controller.react(category)  — или controller.charlesReact(message)
        });
    }
}
