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

    private final Random random = new Random();
    private static final List<String> LEFT_CLICK_PHRASES = List.of(
            "...",
            "Что?",
            "Я здесь.",
            "Зачем?"
    );
    private static final List<String> RIGHT_CLICK_PHRASES = List.of(
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

    public JekyllWindow(SpriteManager spriteManager, DialogueQueue dialogueQueue) {
        super(CharacterId.JEKYLL, spriteManager, dialogueQueue);
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
        panel.setPreferredSize(new Dimension(OUR_WIDTH, OUR_HEIGHT));
        return panel;
    }

    @Override
    protected void handleRightClick(MouseEvent e) {
        if (isTrickShowing || isConfirmationOpen) return;

        if (currentState == CharacterState.SLEEPING) {
            setState(CharacterState.IDLE);
            resetIdleTimers();
        }

        if (RIGHT_CLICK_PHRASES == null || RIGHT_CLICK_PHRASES.isEmpty()) return;

        if (dialogueQueue.isActive()) return;

        String text = RIGHT_CLICK_PHRASES.get(random.nextInt(RIGHT_CLICK_PHRASES.size()));
        dialogueQueue.add(new DialogueLine(CharacterId.JEKYLL, text));
    }

    @Override
    protected void onLeftClick() {
        if (dialogueQueue.isActive()) return;

        String text = LEFT_CLICK_PHRASES.get(random.nextInt(LEFT_CLICK_PHRASES.size()));
        dialogueQueue.add(new DialogueLine(CharacterId.JEKYLL, text));
    }

}
