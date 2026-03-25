package lars.com.dialogue;

import lars.com.model.DialogueLine;

import javax.swing.*;
import java.util.*;
import java.util.function.Consumer;

public class DialogueQueue {

    private final Deque<DialogueLine> pending = new ArrayDeque<>();
    private final Consumer<DialogueLine> displayer;

    private boolean busy = false;

    public DialogueQueue(Consumer<DialogueLine> displayer) {
        this.displayer = displayer;
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
        tryNext();
    }

    public synchronized void interrupt() {
        pending.clear();
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
