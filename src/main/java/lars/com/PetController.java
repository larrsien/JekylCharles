package lars.com;

import lars.com.UI.CharlesWindow;
import lars.com.UI.JekyllWindow;
import lars.com.dialogue.DialogueQueue;
import lars.com.graphic.SpriteManager;
import lars.com.model.CharacterId;
import lars.com.model.CharacterState;
import lars.com.model.DialogueLine;
import lars.com.reactions.DualResponseLibrary;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * PetController — центральная точка входа приложения.
 *
 * Создаёт оба окна, связывает их через DialogueQueue
 * и обеспечивает синхронизацию состояний.
 *
 * ── Синхронизация состояний ──────────────────────────────────────────────
 *  Когда один персонаж засыпает (SLEEPING) → второй тоже засыпает.
 *  Когда один просыпается (IDLE)           → второй тоже просыпается.
 *  CURIOUS — не синхронизируется (каждый любопытствует по-своему).
 *  DRAGGING — не синхронизируется.
 *  BUG — не синхронизируется.
 * ──────────────────────────────────────────────────────────────────────────
 */
public class PetController {

    private final DialogueQueue       queue;
    private JekyllWindow        jekyll;
    private CharlesWindow       charles;
    private final DualResponseLibrary library;

    // ════════════════════════════════════════════════════════════════════════
    //  Конструктор
    // ════════════════════════════════════════════════════════════════════════

    public PetController(SpriteManager jekyllSprites,
                         SpriteManager charlesSprites) {

        library = DualResponseLibrary.createDefault();

        // Маршрутизатор: очередь знает, чей showBubble() вызвать
        queue = new DialogueQueue(line -> {
            if (line.speaker == CharacterId.JEKYLL) {
                PetController.this.jekyll.showBubble(line.text);
            } else {
                PetController.this.charles.showBubble(line.text);
            }
        });

        // Фразы для ПКМ Джекилла
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

        // ── Подключаем синхронизацию состояний ──────────────────────────────
        jekyll.setOnStateChange(this::onCharacterStateChanged);
        charles.setOnStateChange(this::onCharacterStateChanged);

        positionWindows();

        jekyll.setVisible(true);
        charles.setVisible(true);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Синхронизация состояний
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Вызывается, когда один из персонажей меняет состояние.
     * Определяет, нужно ли синхронизировать второго.
     *
     * Используем setStateSilent() чтобы не вызвать бесконечный цикл:
     *   Jekyll.setState → onStateChange → Charles.setStateSilent (без коллбэка)
     */
    private void onCharacterStateChanged(CharacterId who, CharacterState newState) {
        // Синхронизируем только SLEEPING и IDLE (пробуждение)
        switch (newState) {
            case SLEEPING:
                // Оба засыпают
                if (who == CharacterId.JEKYLL) {
                    charles.setStateSilent(CharacterState.SLEEPING);
                } else {
                    jekyll.setStateSilent(CharacterState.SLEEPING);
                }
                break;

            case IDLE:
                // Если один проснулся — будим второго (если он спал)
                if (who == CharacterId.JEKYLL
                        && charles.getCurrentState() == CharacterState.SLEEPING) {
                    charles.setStateSilent(CharacterState.IDLE);
                    charles.resetIdleTimers();
                } else if (who == CharacterId.CHARLES
                        && jekyll.getCurrentState() == CharacterState.SLEEPING) {
                    jekyll.setStateSilent(CharacterState.IDLE);
                    jekyll.resetIdleTimers();
                }
                break;

            default:
                // CURIOUS, DRAGGING, BUG — индивидуальные, не синхронизируем
                break;
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Реакция на события (ProcessMonitor, BrowserMonitor и т.д.)
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Основной метод реакции на системное событие.
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

    public void reactAs(CharacterId who, String text) {
        queue.add(new DialogueLine(who, text));
    }

    public void charlesReact(String text) {
        reactAs(CharacterId.CHARLES, text);
    }

    public void jekyllReact(String text) {
        reactAs(CharacterId.JEKYLL, text);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Позиционирование
    // ════════════════════════════════════════════════════════════════════════

    private void positionWindows() {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(
                GraphicsEnvironment.getLocalGraphicsEnvironment()
                        .getDefaultScreenDevice().getDefaultConfiguration());

        int bottom   = screen.height - insets.bottom;
        int charlesX = screen.width  - charles.getWidth()  - 20;
        int jekyllX  = charlesX      - jekyll.getWidth()   - 10;

        charles.placeAt(charlesX, bottom - charles.getHeight());
        jekyll.placeAt(jekyllX,   bottom - jekyll.getHeight());
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Getters
    // ════════════════════════════════════════════════════════════════════════

    public CharlesWindow  getCharles() { return charles; }
    public JekyllWindow   getJekyll()  { return jekyll;  }
    public DialogueQueue  getQueue()   { return queue;   }

    // ════════════════════════════════════════════════════════════════════════
    //  Cleanup
    // ════════════════════════════════════════════════════════════════════════

    public void cleanup() {
        jekyll.cleanup();
        charles.cleanup();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Точка входа
    // ════════════════════════════════════════════════════════════════════════

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SpriteManager jekyllSprites  = new SpriteManager("jekyll");
            SpriteManager charlesSprites = new SpriteManager("charles");

            PetController controller = new PetController(jekyllSprites, charlesSprites);

            // ProcessMonitor, BrowserMonitor и т.д.:
            //   controller.react("browser")
            //   controller.charlesReact("Какое-то сообщение")
        });
    }
}
