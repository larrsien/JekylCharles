package lars.com.UI;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

public class TrickDialog extends JWindow {

    private static final int OUR_WIDTH = 300;
    private static final int OUR_HEIGHT = 150;
    private static final int FONT_SIZE = 13;
    private static final String FONT_NAME = "Georgia";
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final int BORDER_THICKNESS = 1;
    private static final Color BUTTON_COLOR = new Color(44, 40, 54);
    private static final int BUTTON_WIDTH = 80;
    private static final int BUTTON_HEIGHT = 35;

    // реализация глитчей
    private static final char[] GLITCH_CHARS = {'Д', 'А', '!', '#', '@', '?', '█', '▓', '░', '|'};

    private final CharlesWindow charlesWindow;
    private BufferedImage backgroundImage;
    private final Runnable onResult;
    private final Random random = new Random();

    private Rectangle button1Bounds;
    private Rectangle button2Bounds;
    private boolean button1Hovered = false;
    private boolean button2Hovered = false;

    private Timer glitchTimer;
    private int glitch1OffsetX = 0, glitch1OffsetY = 0;
    private int glitch2OffsetX = 0, glitch2OffsetY = 0;
    private boolean glitch1Active = false, glitch2Active = false;

    private TrickPanel trickPanel;

    public TrickDialog(CharlesWindow charlesWindow, Runnable onResult) {
        this.charlesWindow = charlesWindow;
        this.onResult = onResult;

        loadBackgroundImage();
        initializeWindow();
        startGlitchTimer();
    }

    private void loadBackgroundImage() {
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("frames/reaction.png");
            if (is != null) {
                backgroundImage = ImageIO.read(is);
            } else {
                System.out.println("Фон TrickDialog не найден");
            }
        } catch (IOException e) {
            System.out.println("Ошибка загрузки фона TrickDialog");
        }
    }

    private void initializeWindow() {
        setSize(OUR_WIDTH, OUR_HEIGHT);
        setAlwaysOnTop(true);
        setBackground(new Color(0, 0, 0, 0));

        int totalWidth = BUTTON_WIDTH * 2 + 20;
        int startX = (OUR_WIDTH - totalWidth) / 2;
        int buttonY = OUR_HEIGHT - BUTTON_HEIGHT - 25;

        button1Bounds = new Rectangle(startX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT);
        button2Bounds = new Rectangle(startX + BUTTON_WIDTH + 20, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT);

        trickPanel = new TrickPanel();
        add(trickPanel);

        Point p = charlesWindow.getLocation();
        setLocation(p.x - OUR_WIDTH + 20, p.y + 130);
    }

    private void startGlitchTimer() {
        glitchTimer = new Timer(80, e -> {
            if (button1Hovered) {
                glitch1Active = random.nextInt(100) < 40;
                glitch1OffsetX = glitch1Active ? random.nextInt(5) - 2 : 0;
                glitch1OffsetY = glitch1Active ? random.nextInt(3) - 1 : 0;
            } else {
                glitch1Active = false;
                glitch1OffsetX = 0;
                glitch1OffsetY = 0;
            }

            if (button2Hovered) {
                glitch2Active = random.nextInt(100) < 40;
                glitch2OffsetX = glitch2Active ? random.nextInt(5) - 2 : 0;
                glitch2OffsetY = glitch2Active ? random.nextInt(3) - 1 : 0;
            } else {
                glitch2Active = false;
                glitch2OffsetX = 0;
                glitch2OffsetY = 0;
            }

            trickPanel.repaint();
        });
        glitchTimer.start();
    }

    private void handleChoice() {
        if (glitchTimer != null) glitchTimer.stop();
        dispose();
        if (onResult != null) onResult.run();
    }

    private class TrickPanel extends JPanel {
        public TrickPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(OUR_WIDTH, OUR_HEIGHT));

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    Point p = e.getPoint();
                    button1Hovered = button1Bounds.contains(p);
                    button2Hovered = button2Bounds.contains(p);
                    repaint();
                }
            });

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseExited(MouseEvent e) {
                    button1Hovered = false;
                    button2Hovered = false;
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    Point p = e.getPoint();
                    if (button1Bounds.contains(p) || button2Bounds.contains(p)) {
                        handleChoice();
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            if (backgroundImage != null) {
                g2d.drawImage(backgroundImage, 0, 0, OUR_WIDTH, OUR_HEIGHT, this);
            }

            // текст заголовка
            Font font = new Font(FONT_NAME, Font.BOLD, FONT_SIZE);
            g2d.setFont(font);
            FontMetrics fm = g2d.getFontMetrics();
            String text = "Что выберешь?";
            int x = (OUR_WIDTH - fm.stringWidth(text)) / 2;
            int y = 50;

            g2d.setColor(Color.BLACK);
            for (int dx = -BORDER_THICKNESS; dx <= BORDER_THICKNESS; dx++) {
                for (int dy = -BORDER_THICKNESS; dy <= BORDER_THICKNESS; dy++) {
                    if (dx != 0 || dy != 0) g2d.drawString(text, x + dx, y + dy);
                }
            }
            g2d.setColor(TEXT_COLOR);
            g2d.drawString(text, x, y);

            drawButton(g2d, button1Bounds, "???", button1Hovered, glitch1Active, glitch1OffsetX, glitch1OffsetY);
            drawButton(g2d, button2Bounds, "???", button2Hovered, glitch2Active, glitch2OffsetX, glitch2OffsetY);
        }

        private void drawButton(Graphics2D g2d, Rectangle bounds, String text,
                                boolean hovered, boolean glitchActive, int offsetX, int offsetY) {
            Color color = hovered ? BUTTON_COLOR.brighter() : BUTTON_COLOR;

            int bx = bounds.x + (glitchActive ? offsetX : 0);
            int by = bounds.y + (glitchActive ? offsetY : 0);

            g2d.setColor(color);
            g2d.fillRoundRect(bx, by, bounds.width, bounds.height, 10, 10);
            g2d.setColor(color.darker());
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(bx, by, bounds.width, bounds.height, 10, 10);

            Font f = new Font(FONT_NAME, Font.BOLD, 14);
            g2d.setFont(f);
            FontMetrics fm = g2d.getFontMetrics();

            String displayText = text;
            if (glitchActive) {
                char[] chars = text.toCharArray();
                for (int i = 0; i < chars.length; i++) {
                    if (random.nextInt(100) < 50) {
                        chars[i] = GLITCH_CHARS[random.nextInt(GLITCH_CHARS.length)];
                    }
                }
                displayText = new String(chars);
            }

            int tx = bounds.x + (bounds.width - fm.stringWidth(displayText)) / 2 + offsetX;
            int ty = bounds.y + (bounds.height - fm.getHeight()) / 2 + fm.getAscent() + offsetY;

            if (glitchActive) {
                g2d.setColor(new Color(255, 0, 0, 150));
                g2d.drawString(displayText, tx + 2, ty);
                g2d.setColor(new Color(0, 0, 255, 150));
                g2d.drawString(displayText, tx - 2, ty);
            }

            g2d.setColor(new Color(0, 0, 0, 100));
            g2d.drawString(displayText, tx + 1, ty + 1);
            g2d.setColor(Color.WHITE);
            g2d.drawString(displayText, tx, ty);
        }
    }
}
