package lars.com.dialogue;

import lars.com.model.DialogueLine;

import javax.swing.*;
import java.util.*;
import javax.swing.Timer;
import java.util.function.Consumer;

/**
 * Глобальная очередь диалогов.
 *
 * Правила:
 *  - В любой момент активна ровно одна реплика (или очередь пуста).
 *  - Новая реплика показывается только после того, как предыдущий бабл
 *    закрылся (по таймауту или клику).
 *  - Можно добавить одиночную реплику (soliloquy) или цепочку (dialogue).
 *  - Если очередь занята, новый одиночный монолог откладывается в конец;
 *    если нужно прервать — вызвать interrupt() перед добавлением.
 *
 * Использование:
 *   DialogueQueue queue = new DialogueQueue(line -> {
 *       // вызывается на EDT; line.speaker говорит line.text
 *       if (line.speaker == CharacterId.JEKYLL) jekyllWindow.showBubble(line.text);
 *       else                                    charlesWindow.showBubble(line.text);
 *   });
 *
 *   // Добавить одну реплику Джекилла:
 *   queue.add(new DialogueLine(CharacterId.JEKYLL, "Хм."));
 *
 *   // Добавить цепочку:
 *   queue.addAll(List.of(
 *       new DialogueLine(CharacterId.JEKYLL,  "Это интересно."),
 *       new DialogueLine(CharacterId.CHARLES, "Не особенно.")
 *   ));
 *
 *   // Когда бабл закрылся — вызвать onBubbleDone():
 *   queue.onBubbleDone();
 */
public class DialogueQueue {

    /** Сколько миллисекунд бабл висит, если пользователь не закрыл сам. */
    private static final int BUBBLE_DURATION_MS = 6000;

    private final Deque<DialogueLine> pending = new ArrayDeque<>();
    private final Consumer<DialogueLine> displayer;   // функция показа бабла

    private boolean busy = false;   // true пока висит активный бабл
    private Timer autoCloseTimer;

    /**
     * @param displayer  лямбда, вызываемая на EDT, когда нужно показать очередную реплику.
     *                   Получает DialogueLine и должна вызвать showBubble() нужного окна.
     */
    public DialogueQueue(Consumer<DialogueLine> displayer) {
        this.displayer = displayer;
    }

    // ──────────────────────────────────────────────────────────
    //  Публичный API
    // ──────────────────────────────────────────────────────────

    /** Добавить одну реплику в конец очереди. */
    public synchronized void add(DialogueLine line) {
        pending.addLast(line);
        tryNext();
    }

    /** Добавить несколько реплик (диалог) в конец очереди. */
    public synchronized void addAll(List<DialogueLine> lines) {
        pending.addAll(lines);
        tryNext();
    }

    /** Удобный вариант через varargs. */
    public synchronized void addAll(DialogueLine... lines) {
        addAll(Arrays.asList(lines));
    }

    /**
     * Вызвать, когда бабл закрылся (по таймауту или клику).
     * Продвигает очередь вперёд.
     */
    public synchronized void onBubbleDone() {
        busy = false;
        if (autoCloseTimer != null) {
            autoCloseTimer.stop();
            autoCloseTimer = null;
        }
        tryNext();
    }

    /**
     * Очистить очередь и остановить текущий бабл (если он есть).
     * Полезно при смене контекста (например, открылось контекстное меню).
     */
    public synchronized void interrupt() {
        pending.clear();
        busy = false;
        if (autoCloseTimer != null) {
            autoCloseTimer.stop();
            autoCloseTimer = null;
        }
    }

    /** Возвращает true, если очередь сейчас что-то показывает или ждёт. */
    public synchronized boolean isActive() {
        return busy || !pending.isEmpty();
    }

    // ──────────────────────────────────────────────────────────
    //  Внутренняя логика
    // ──────────────────────────────────────────────────────────

    private void tryNext() {
        // Метод вызывается уже под synchronized-блоком
        if (busy || pending.isEmpty()) return;

        busy = true;
        DialogueLine next = pending.pollFirst();

        SwingUtilities.invokeLater(() -> {
            displayer.accept(next);
            startAutoClose();
        });
    }

    private void startAutoClose() {
        autoCloseTimer = new Timer(BUBBLE_DURATION_MS, e -> {
            synchronized (DialogueQueue.this) {
                if (busy) onBubbleDone();
            }
        });
        autoCloseTimer.setRepeats(false);
        autoCloseTimer.start();
    }
}
