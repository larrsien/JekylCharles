package lars.com.UI;

import lars.com.graphic.SpriteManager;
import lars.com.model.AmonState;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

import java.util.List;

public class BugCloneWindow extends JWindow {
    private static final int OUR_WIDTH = 290;
    private static final int OUR_HEIGHT = 300;

    private final SpriteManager spriteManager;
    private int currentFrame;
    private Timer animationTimer;
    private Timer disappearanceTimer;

    private BugClonePanel bugClonePanel;

    private List<BufferedImage> selectedVariant;

    public BugCloneWindow(SpriteManager spriteManager, Point position) {
        this.spriteManager = spriteManager;
        this.currentFrame = 0;
        this.selectedVariant = spriteManager.getRandomBugVariant();

        initializeWindow(position);
        initializeAnimation();
        scheduleDisappear();
    }

    private void initializeWindow(Point position) {
        setSize(OUR_WIDTH, OUR_HEIGHT);
        setAlwaysOnTop(true);
        setBackground(new Color(0, 0, 0, 0));

        bugClonePanel = new BugClonePanel();
        add(bugClonePanel);

        setLocation(position);
    }

    private void initializeAnimation() {
        int frameCount = selectedVariant != null ? selectedVariant.size() : 1;
        animationTimer = new Timer(spriteManager.getFrameDelay(AmonState.BUG), e -> {
            currentFrame = (currentFrame + 1) % frameCount;
            bugClonePanel.repaint();
        });
        animationTimer.start();
    }

    private void scheduleDisappear() {
        // 2 секунды
        disappearanceTimer = new Timer(2000, e -> cleanup());
        disappearanceTimer.setRepeats(false);
        disappearanceTimer.start();
    }

    public void cleanup() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
        if (disappearanceTimer != null) {
            disappearanceTimer.stop();
        }
        dispose();
    }

    private class BugClonePanel extends JPanel {
        public BugClonePanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(OUR_WIDTH, OUR_HEIGHT));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            BufferedImage sprite = (selectedVariant != null && !selectedVariant.isEmpty())
                    ? selectedVariant.get(currentFrame % selectedVariant.size())
                    : null;
            if (sprite != null) {
                g2d.drawImage(sprite, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }
}
