package lars.com.UI;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SettingsWindow extends JWindow {

    private static final int OUR_WIDTH = 230;
    private static final int OUR_HEIGHT = 300;

    private static final int  FONT_SIZE = 13;
    private static final String FONT_NAME = "Georgia";
    private static final int TEXT_PADDING_TOP = 20;

    private static final Color BUTTON_COLOR = new Color(79, 71, 73);
    private static final int BUTTON_WIDTH = 148;
    private static final int BUTTON_HEIGHT = 34;
    private static final int BUTTON_GAP = 10;

    private static final int CLICKS_TO_REACT = 5;

    private final CharlesWindow charlesWindow;
    private BufferedImage backgroundImage;

    private Rectangle toggleButtonBounds;
    private Rectangle fakeDisableButtonBounds;
    private Rectangle closeButtonBounds;

    private boolean toggleHovered = false;
    private boolean fakeHovered = false;
    private boolean closeHovered = false;

    // глитч кнопочка
    private static final char[] GLITCH_CHARS = {'?', '!', '#', '@', '%', '█', '▓', '░', '|', '<', '>', '◆', '▒'};
    private Timer   glitchTimer;
    private boolean glitchActive = false;
    private int glitchOffsetX = 0;
    private int glitchOffsetY = 0;
    private Color glitchColor = Color.WHITE;
    private String glitchText = "Отключить реакции";
    private int fakeClickCount = 0;
    private final Random random = new Random();

    public SettingsWindow(CharlesWindow charlesWindow) {
        this.charlesWindow = charlesWindow;
        loadBackgroundImage();
        initializeWindow();
    }

    private void loadBackgroundImage() {
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("frames/question.png");
            if (is != null) {
                backgroundImage = ImageIO.read(is);
            } else {
                File f = new File("frames/question.png");
                if (f.exists()) backgroundImage = ImageIO.read(f);
            }
        } catch (IOException e) {
            System.out.println("Не удалось загрузить фон для настроек");
        }
    }

    private void initializeWindow() {
        setSize(OUR_WIDTH, OUR_HEIGHT);
        setAlwaysOnTop(true);
        setBackground(new Color(0, 0, 0, 0));

        int centerX = (OUR_WIDTH - BUTTON_WIDTH) / 2;
        int startY = TEXT_PADDING_TOP + 30;

        // Кнопка переключения подсказок к шифру
        String cipherLabel = buildToggleLabel();
        int cipherHeight = calcButtonHeight(cipherLabel);
        toggleButtonBounds = new Rectangle(centerX, startY, BUTTON_WIDTH, cipherHeight);

        // Фейковая кнопка отключения реакций
        fakeDisableButtonBounds = new Rectangle(centerX, BUTTON_GAP,
                BUTTON_WIDTH, BUTTON_HEIGHT);

        // Кнопка закрытия
        closeButtonBounds = new Rectangle(centerX, fakeDisableButtonBounds.y + BUTTON_HEIGHT + BUTTON_GAP,
                BUTTON_WIDTH, BUTTON_HEIGHT);

        add(new SettingsPanel());
        positionDialog();
        startGlitchTimer();

        addWindowFocusListener(new WindowAdapter() {
            @Override public void windowLostFocus(WindowEvent e) { closeWindow(); }
        });
    }

    private void positionDialog() {
        Point amonLocation = charlesWindow.getLocation();

        int x = amonLocation.x - getWidth() + 20;
        int y = amonLocation.y + 10;

        setLocation(x, y);
    }

    private void closeWindow() {
        if (glitchTimer != null) glitchTimer.stop();
        dispose();
        charlesWindow.onSettingsWindowClosed();
    }

    private void startGlitchTimer() {
        glitchTimer = new Timer(90, e -> {
            if (fakeHovered || fakeClickCount > 0) {
                // Глитч активен изначально с шансом 35%
                glitchActive = random.nextInt(100) < 35;

                if (glitchActive) {
                    char[] chars = "Отключить реакции".toCharArray();
                    for (int i = 0; i < chars.length; i++) {
                        if (chars[i] != ' ' && chars[i] != '.' && random.nextInt(100) < 40) {
                            chars[i] = GLITCH_CHARS[random.nextInt(GLITCH_CHARS.length)];
                        }
                    }
                    glitchText = new String(chars);

                    // Смещение кнопки
                    glitchOffsetX = random.nextInt(5) - 2;
                    glitchOffsetY = random.nextInt(3) - 1;

                    glitchColor = random.nextBoolean() ? new Color(255, random.nextInt(60), random.nextInt(60))
                            : Color.WHITE;
                } else {
                    glitchText = "Отключить реакции";
                    glitchOffsetX = 0;
                    glitchOffsetY = 0;
                    glitchColor = Color.WHITE;
                }
            } else {
                glitchActive = false;
                glitchText = "Отключить реакции";
                glitchOffsetX = 0;
                glitchOffsetY = 0;
                glitchColor = Color.WHITE;
            }

            if (fakeHovered || fakeClickCount > 0) {
                repaint();
            }
        });
        glitchTimer.start();
    }

    private int calcButtonHeight(String text) {
        Canvas canvas = new Canvas();
        FontMetrics fm = canvas.getFontMetrics(new Font(FONT_NAME, Font.BOLD, FONT_SIZE));
        List<String> lines = wrapText(text, fm, BUTTON_WIDTH - 16);
        int lineH = fm.getHeight();
        int total = lines.size() * lineH + 14;
        return Math.max(BUTTON_HEIGHT, total);
    }

    private String buildToggleLabel() {
        return charlesWindow.isGlitchEnabled()
                ? "Отключить подсказки к шифру"
                : "Включить подсказки к шифру";
    }

    private void triggerAmonReaction() {
        if (glitchTimer != null) glitchTimer.stop();
        dispose();
        charlesWindow.onSettingsWindowClosed();

        // Небольшая задержка, чтобы окно успело закрыться
        Timer delay = new Timer(100, e -> {
            charlesWindow.reactToEvent("Отключить реакции? Ха-ха. Боюсь, это совершенно не в моих " +
                            "интересах! А значит, и не в Ваших. Скажем так: я оставлю " +
                            "эту кнопку здесь ради эстетики. Пользы от неё — ровно столько, " +
                            "сколько Вы и ожидали.");
        });
        delay.setRepeats(false);
        delay.start();
    }

    private static List<String> wrapText(String text, FontMetrics fm, int maxWidth) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder current = new StringBuilder();

        for (String word : words) {
            String test = current.length() == 0 ? word : current + " " + word;
            if (fm.stringWidth(test) <= maxWidth) {
                current = new StringBuilder(test);
            } else {
                if (current.length() > 0) lines.add(current.toString());
                current = new StringBuilder(word);
            }
        }
        if (current.length() > 0) lines.add(current.toString());
        return lines;
    }

    private class SettingsPanel extends JPanel {

        SettingsPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(OUR_WIDTH, OUR_HEIGHT));

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override public void mouseMoved(MouseEvent e) {
                    Point p = e.getPoint();
                    toggleHovered = toggleButtonBounds.contains(p);
                    fakeHovered  = fakeDisableButtonBounds.contains(p);
                    closeHovered = closeButtonBounds.contains(p);
                    repaint();
                }
            });

            addMouseListener(new MouseAdapter() {
                @Override public void mouseExited(MouseEvent e) {
                    toggleHovered = fakeHovered = closeHovered = false;
                    repaint();
                }

                @Override public void mouseReleased(MouseEvent e) {
                    Point p = e.getPoint();

                    if (toggleButtonBounds.contains(p)) {
                        charlesWindow.setGlitchEnabled(!charlesWindow.isGlitchEnabled());
                        rebuildButtonBounds();
                        repaint();

                    } else if (fakeDisableButtonBounds.contains(p)) {
                        fakeClickCount++;
                        glitchActive  = true;
                        glitchOffsetX = random.nextInt(7) - 3;
                        glitchOffsetY = random.nextInt(5) - 2;
                        repaint();

                        if (fakeClickCount >= CLICKS_TO_REACT) {
                            fakeClickCount = 0;
                            triggerAmonReaction();
                        }

                    } else if (closeButtonBounds.contains(p)) {
                        closeWindow();
                    }
                }
            });
        }

        private void rebuildButtonBounds() {
            int centerX = (OUR_WIDTH - BUTTON_WIDTH) / 2;
            int startY  = TEXT_PADDING_TOP + 30;

            String cipherLabel = buildToggleLabel();
            int cipherH = calcButtonHeight(cipherLabel);
            toggleButtonBounds = new Rectangle(centerX, startY, BUTTON_WIDTH, cipherH);

            fakeDisableButtonBounds = new Rectangle(centerX, toggleButtonBounds.y + BUTTON_GAP,
                    BUTTON_WIDTH, BUTTON_HEIGHT);

            closeButtonBounds = new Rectangle(centerX, fakeDisableButtonBounds.y + BUTTON_HEIGHT + BUTTON_GAP,
                    BUTTON_WIDTH, BUTTON_HEIGHT);
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

            // Кнопка подсказок к шифру
            drawMultilineButton(g2d, toggleButtonBounds, buildToggleLabel(), toggleHovered, BUTTON_COLOR);

            // Фейковая кнопка с глитчем
            drawGlitchButton(g2d);

            // Кнопка закрытия
            drawButton(g2d, closeButtonBounds, "Закрыть", closeHovered, BUTTON_COLOR);
        }

        private void drawButton(Graphics2D g2d, Rectangle b, String text, boolean hovered, Color base) {
            Color c = hovered ? base.brighter() : base;
            g2d.setColor(c);
            g2d.fillRoundRect(b.x, b.y, b.width, b.height, 10, 10);
            g2d.setColor(c.darker());
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(b.x, b.y, b.width, b.height, 10, 10);

            Font bf = new Font(FONT_NAME, Font.BOLD, FONT_SIZE);
            g2d.setFont(bf);
            FontMetrics fm = g2d.getFontMetrics();
            int tx = b.x + (b.width  - fm.stringWidth(text)) / 2;
            int ty = b.y + (b.height - fm.getHeight()) / 2 + fm.getAscent();

            g2d.setColor(new Color(0, 0, 0, 100));
            g2d.drawString(text, tx + 1, ty + 1);
            g2d.setColor(Color.WHITE);
            g2d.drawString(text, tx, ty);
        }

        private void drawMultilineButton(Graphics2D g2d, Rectangle b, String text, boolean hovered, Color base) {
            Color c = hovered ? base.brighter() : base;
            g2d.setColor(c);
            g2d.fillRoundRect(b.x, b.y, b.width, b.height, 10, 10);
            g2d.setColor(c.darker());
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(b.x, b.y, b.width, b.height, 10, 10);

            Font bf = new Font(FONT_NAME, Font.BOLD, FONT_SIZE);
            g2d.setFont(bf);
            FontMetrics fm   = g2d.getFontMetrics();
            List<String> lines = wrapText(text, fm, b.width - 16);
            int lineH = fm.getHeight();
            int totalH = lines.size() * lineH;
            int startY = b.y + (b.height - totalH) / 2 + fm.getAscent();

            for (String line : lines) {
                int tx = b.x + (b.width - fm.stringWidth(line)) / 2;
                g2d.setColor(new Color(0, 0, 0, 100));
                g2d.drawString(line, tx + 1, startY + 1);
                g2d.setColor(Color.WHITE);
                g2d.drawString(line, tx, startY);
                startY += lineH;
            }
        }

        private void drawGlitchButton(Graphics2D g2d) {
            Rectangle b = fakeDisableButtonBounds;
            Color base  = fakeHovered ? BUTTON_COLOR.brighter() : BUTTON_COLOR;

            int bx = b.x + (glitchActive ? glitchOffsetX : 0);
            int by = b.y + (glitchActive ? glitchOffsetY : 0);

            g2d.setColor(base);
            g2d.fillRoundRect(bx, by, b.width, b.height, 10, 10);
            g2d.setColor(base.darker());
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(bx, by, b.width, b.height, 10, 10);

            Font bf = new Font(FONT_NAME, Font.BOLD, FONT_SIZE);
            g2d.setFont(bf);
            FontMetrics fm = g2d.getFontMetrics();

            List<String> lines = wrapText(glitchText, fm, b.width - 16);
            int lineH  = fm.getHeight();
            int totalH = lines.size() * lineH;
            int startY = b.y + (b.height - totalH) / 2 + fm.getAscent()
                    + (glitchActive ? glitchOffsetY : 0);

            for (String line : lines) {
                int tx = b.x + (b.width - fm.stringWidth(line)) / 2
                        + (glitchActive ? glitchOffsetX : 0);

                // Хроматическая аберрация при глитче
                if (glitchActive) {
                    g2d.setColor(new Color(255, 0, 0, 120));
                    g2d.drawString(line, tx + 2, startY);
                    g2d.setColor(new Color(0, 0, 255, 120));
                    g2d.drawString(line, tx - 2, startY);
                }

                g2d.setColor(new Color(0, 0, 0, 100));
                g2d.drawString(line, tx + 1, startY + 1);
                g2d.setColor(glitchColor);
                g2d.drawString(line, tx, startY);

                startY += lineH;
            }
        }
    }
}
