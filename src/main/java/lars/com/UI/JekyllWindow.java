package lars.com.UI;

import lars.com.dialogue.DialogueQueue;
import lars.com.graphic.SpriteManager;
import lars.com.model.CharacterId;
import lars.com.model.CharacterState;
import lars.com.model.DialogueLine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Random;

public class JekyllWindow extends CharacterWindow {

    private final List<String> rightClickPhrases;
    private final Random random = new Random();

    public JekyllWindow(SpriteManager spriteManager,
                        DialogueQueue dialogueQueue,
                        List<String> rightClickPhrases) {
        super(CharacterId.JEKYLL, spriteManager, dialogueQueue);
        this.rightClickPhrases = rightClickPhrases;
    }

    @Override
    protected JPanel buildSpritePanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                BufferedImage sprite = currentSprite();
                if (sprite != null) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                    g2d.drawImage(sprite, 0, 0, getWidth(), getHeight(), null);
                }
            }
        };
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        return panel;
    }

    @Override
    protected void handleRightClick(MouseEvent e) {
        if (isTrickShowing || isConfirmationOpen) return;

        // Пробуждаем, если спал
        if (currentState == CharacterState.SLEEPING) {
            setState(CharacterState.IDLE);
            resetIdleTimers();
        }

        if (rightClickPhrases == null || rightClickPhrases.isEmpty()) return;

        String text = rightClickPhrases.get(random.nextInt(rightClickPhrases.size()));
        dialogueQueue.add(new DialogueLine(CharacterId.JEKYLL, text));
    }
}
