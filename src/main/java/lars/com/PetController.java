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


public class PetController {

    private final DialogueQueue queue;
    private JekyllWindow jekyll;
    private CharlesWindow charles;
    private final DualResponseLibrary dualResponseLibrary;

    public PetController(SpriteManager jekyllSprites, SpriteManager charlesSprites) {

        dualResponseLibrary = DualResponseLibrary.createDefault();

        // Маршрутизатор
        queue = new DialogueQueue(line -> {
            if (line.speaker == CharacterId.JEKYLL) {
                PetController.this.jekyll.showBubble(line.text);
            } else {
                PetController.this.charles.showBubble(line.text);
            }
        });

        jekyll  = new JekyllWindow(jekyllSprites, queue);
        charles = new CharlesWindow(charlesSprites, queue);

        // Синхронизация состояний
        jekyll.setOnStateChange(this::onCharacterStateChanged);
        charles.setOnStateChange(this::onCharacterStateChanged);

        positionWindows();

        jekyll.setVisible(true);
        charles.setVisible(true);
    }

    private void onCharacterStateChanged(CharacterId character, CharacterState newState) {
        switch (newState) {
            case SLEEPING:
                // Оба засыпают
                if (character == CharacterId.JEKYLL) {
                    charles.setStateSilent(CharacterState.SLEEPING);
                } else {
                    jekyll.setStateSilent(CharacterState.SLEEPING);
                }
                break;

            case CURIOUS:
                if (character == CharacterId.JEKYLL) {
                    charles.setStateSilent(CharacterState.CURIOUS);
                } else {
                    jekyll.setStateSilent(CharacterState.CURIOUS);
                }
                break;

            case IDLE:
                // Если один проснулся, то будим второго
                if (character == CharacterId.JEKYLL && charles.getCurrentState() == CharacterState.SLEEPING) {
                    charles.setStateSilent(CharacterState.IDLE);
                    charles.resetIdleTimers();
                } else if (character == CharacterId.CHARLES && jekyll.getCurrentState() == CharacterState.SLEEPING) {
                    jekyll.setStateSilent(CharacterState.IDLE);
                    jekyll.resetIdleTimers();
                } else if (character == CharacterId.CHARLES && jekyll.getCurrentState() == CharacterState.CURIOUS) {
                    jekyll.setStateSilent(CharacterState.IDLE);
                    jekyll.resetIdleTimers();
                } else if (character == CharacterId.JEKYLL && charles.getCurrentState() == CharacterState.CURIOUS) {
                    charles.setStateSilent(CharacterState.IDLE);
                    charles.resetIdleTimers();
                }
                break;

            default:
                break;
        }
    }

    public void react(String category) {
        if (!dualResponseLibrary.hasCategory(category)) {
            category = "default";
        }
        List<DialogueLine> lines = dualResponseLibrary.getResponse(category);
        if (!lines.isEmpty()) {
            queue.addAll(lines);
        }
    }

    public void reactAs(CharacterId who, String text) {
        queue.add(new DialogueLine(who, text));
    }

    private void positionWindows() {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(GraphicsEnvironment.getLocalGraphicsEnvironment()
                        .getDefaultScreenDevice().getDefaultConfiguration());

        int bottom = screen.height - insets.bottom;
        int charlesX = screen.width - charles.getWidth()  - 20;
        int jekyllX = charlesX - jekyll.getWidth()   - 10;

        charles.placeAt(charlesX, bottom - charles.getHeight());
        jekyll.placeAt(jekyllX,   bottom - jekyll.getHeight());
    }

    public CharlesWindow  getCharles() {
        return charles;
    }

    public void cleanup() {
        jekyll.cleanup();
        charles.cleanup();
    }
}
