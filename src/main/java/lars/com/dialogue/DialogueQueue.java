package lars.com.dialogue;

import lars.com.model.DialogueLine;

import javax.swing.*;
import java.util.*;
import java.util.function.Consumer;

public class DialogueQueue {

    private final Deque<DialogueLine> pending = new ArrayDeque<>();
    private final Consumer<DialogueLine> displayer;

    private boolean busy = false;
    private Runnable onAllDone;

    public DialogueQueue(Consumer<DialogueLine> displayer) {
        this.displayer = displayer;
    }

    public synchronized void addWithCallback(DialogueLine line, Runnable callback) {
        this.onAllDone = callback;
        pending.addLast(line);
        tryNext();
    }


    public synchronized void add(DialogueLine line) {
        pending.addLast(line);
        tryNext();
    }

    public synchronized void addAll(List<DialogueLine> lines) {
        pending.addAll(lines);
        tryNext();
    }

    // varargs
    public synchronized void addAll(DialogueLine... lines) {
        addAll(Arrays.asList(lines));
    }

    public synchronized void onBubbleDone() {
        busy = false;

        if (pending.isEmpty() && onAllDone != null) {
            Runnable cb = onAllDone;
            onAllDone = null;
            SwingUtilities.invokeLater(cb);
            return;
        }

        tryNext();
    }

    public synchronized void interrupt() {
        pending.clear();
        onAllDone = null;
        busy = false;
    }

    public synchronized boolean isActive() {
        return busy || !pending.isEmpty();
    }

    private void tryNext() {
        if (busy || pending.isEmpty()) return;

        busy = true;
        DialogueLine next = pending.pollFirst();

        SwingUtilities.invokeLater(() -> {
            displayer.accept(next);
        });
    }
}
