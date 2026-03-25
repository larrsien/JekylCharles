package lars.com.UI;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

public class ContextMenu extends JWindow {

    private static final int OUR_WIDTH = 230;
    private static final int OUR_HEIGHT = 318;

    private static final int FONT_SIZE = 13;
    private static final String FONT_NAME = "Georgia";
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final int BORDER_THICKNESS = 1;
    private static final int TEXT_PADDING_TOP = 49;

    private static final Color BUTTON_COLOR = new Color(79, 71, 73);
    private static final int BUTTON_WIDTH = 148;
    private static final int BUTTON_HEIGHT = 34;
    private static final int BUTTON_GAP = 10;

    private final Runnable onTimerClick;
    private final Runnable onBirthdayClick;

    private BufferedImage backgroundImage;

    private Rectangle surpriseButtonBounds;
    private Rectangle timerButtonBounds;
    private Rectangle settingsButtonBounds;
    private Rectangle exitButtonBounds;

    private boolean surpriseHovered = false;
    private boolean timerHovered = false;
    private boolean settingsHovered = false;
    private boolean exitHovered = false;

    private static final String HEADER = "Хм-м, что же\nВы хотите сделать?";

    // ═══ КЛЮЧЕВОЕ ИЗМЕНЕНИЕ: CharlesWindow вместо AmonWindow ═══
    private final CharlesWindow charlesWindow;

    private Timer glitchTimer;
    private String glitchText = "???";
    private int glitchOffsetX = 0;
    private int glitchOffsetY = 0;
    private Color glitchColor = Color.WHITE;
    private boolean glitchActive = false;

    private static final char[] GLITCH_CHARS = {
            '?', '!', '#', '@', '%', '&', '/', '\\', '|', '<', '>', '█', '▓', '░'
    };
    private final Random random = new Random();

    public ContextMenu(CharlesWindow charlesWindow, Runnable onBirthdayClick, Runnable onTimerClick) {
        this.charlesWindow = charlesWindow;
        this.onTimerClick = onTimerClick;
        this.onBirthdayClick = onBirthdayClick;

        loadBackgroundImage();
        initializeWindow();

        addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowLostFocus(WindowEvent e) {
                if (glitchTimer != null) glitchTimer.stop();
                charlesWindow.onContextMenuClosed();
                dispose();
            }
        });
    }

    private void initializeWindow() {
        setSize(OUR_WIDTH, OUR_HEIGHT);
        setBackground(new Color(0, 0, 0, 0));
        setAlwaysOnTop(true);

        int buttonX = (OUR_WIDTH - BUTTON_WIDTH) / 2;
        int firstBtnY = TEXT_PADDING_TOP + 50;

        surpriseButtonBounds = new Rectangle(buttonX, firstBtnY, BUTTON_WIDTH, BUTTON_HEIGHT);
        timerButtonBounds = new Rectangle(buttonX, firstBtnY + BUTTON_HEIGHT + BUTTON_GAP,
                BUTTON_WIDTH, BUTTON_HEIGHT);
        settingsButtonBounds = new Rectangle(buttonX, timerButtonBounds.y + BUTTON_HEIGHT + BUTTON_GAP,
                BUTTON_WIDTH, BUTTON_HEIGHT);
        exitButtonBounds = new Rectangle(buttonX, settingsButtonBounds.y + BUTTON_HEIGHT + BUTTON_GAP,
                BUTTON_WIDTH, BUTTON_HEIGHT);

        add(new ContextPanel());
        positionDialog();
        startGlitchEffect();
    }

    private void positionDialog() {
        Point loc = charlesWindow.getLocation();
        int x = loc.x - getWidth() + 20;
        int y = loc.y + 10;
        setLocation(x, y);
    }

    private void startGlitchEffect() {
        glitchTimer = new Timer(100, e -> {
            if (!charlesWindow.isSurpriseRevealed()) {
                glitchActive = random.nextInt(100) < 30;
                if (glitchActive) {
                    char[] chars = "???".toCharArray();
                    for (int i = 0; i < chars.length; i++) {
                        if (random.nextInt(100) < 50) {
                            chars[i] = GLITCH_CHARS[random.nextInt(GLITCH_CHARS.length)];
                        }
                    }
                    glitchText = new String(chars);
                    glitchOffsetX = random.nextInt(5) - 2;
                    glitchOffsetY = random.nextInt(5) - 2;
                    glitchColor = new Color(
                            200 + random.nextInt(56),
                            random.nextInt(100),
                            random.nextInt(100));
                } else {
                    glitchText = "???";
                    glitchOffsetX = 0;
                    glitchOffsetY = 0;
                    glitchColor = Color.WHITE;
                }
                repaint();
            }
        });
        glitchTimer.start();
    }

    private void loadBackgroundImage() {
        try {
            InputStream is = getClass().getClassLoader()
                    .getResourceAsStream("frames/reaction.png");
            if (is != null) {
                backgroundImage = ImageIO.read(is);
            } else {
                File file = new File("src/main/resources/frames/reaction.png");
                if (file.exists()) {
                    backgroundImage = ImageIO.read(file);
                }
            }
        } catch (IOException e) {
            System.out.println("Не удалось загрузить фон контекстного меню");
        }
    }

    // ─── Вспомогательные методы отрисовки (заглушки — заполни своим кодом) ──

    private void drawTextWithBorder(Graphics2D g2d, String text) {
        g2d.setFont(new Font(FONT_NAME, Font.BOLD, FONT_SIZE));
        g2d.setColor(TEXT_COLOR);
        String[] lines = text.split("\n");
        int y = TEXT_PADDING_TOP;
        for (String line : lines) {
            FontMetrics fm = g2d.getFontMetrics();
            int x = (OUR_WIDTH - fm.stringWidth(line)) / 2;
            g2d.drawString(line, x, y);
            y += fm.getHeight();
        }
    }

    private void drawButton(Graphics2D g2d, Rectangle bounds, String label, boolean hovered, Color color) {
        g2d.setColor(hovered ? color.brighter() : color);
        g2d.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 8, 8);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font(FONT_NAME, Font.PLAIN, FONT_SIZE));
        FontMetrics fm = g2d.getFontMetrics();
        int tx = bounds.x + (bounds.width - fm.stringWidth(label)) / 2;
        int ty = bounds.y + (bounds.height + fm.getAscent()) / 2 - 2;
        g2d.drawString(label, tx, ty);
    }

    private void drawGlitchButton(Graphics2D g2d, Rectangle bounds, boolean hovered, Color color) {
        g2d.setColor(hovered ? color.brighter() : color);
        g2d.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 8, 8);
        g2d.setColor(glitchColor);
        g2d.setFont(new Font(FONT_NAME, Font.BOLD, FONT_SIZE));
        FontMetrics fm = g2d.getFontMetrics();
        int tx = bounds.x + (bounds.width - fm.stringWidth(glitchText)) / 2 + glitchOffsetX;
        int ty = bounds.y + (bounds.height + fm.getAscent()) / 2 - 2 + glitchOffsetY;
        g2d.drawString(glitchText, tx, ty);
    }

    // ─── Панель ─────────────────────────────────────────────────────────────

    private class ContextPanel extends JPanel {
        public ContextPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(OUR_WIDTH, OUR_HEIGHT));

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    Point p = e.getPoint();
                    surpriseHovered = surpriseButtonBounds.contains(p);
                    timerHovered = timerButtonBounds.contains(p);
                    settingsHovered = settingsButtonBounds.contains(p);
                    exitHovered = exitButtonBounds.contains(p);
                    repaint();
                }
            });

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseExited(MouseEvent e) {
                    surpriseHovered = timerHovered = exitHovered = settingsHovered = false;
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    Point p = e.getPoint();
                    if (surpriseButtonBounds.contains(p)) {
                        dispose();
                        charlesWindow.onContextMenuClosed();
                        charlesWindow.setContextMenuOpen(false);
                        if (onBirthdayClick != null) onBirthdayClick.run();
                    } else if (timerButtonBounds.contains(p)) {
                        charlesWindow.setContextMenuOpen(false);
                        charlesWindow.onContextMenuClosed();
                        dispose();
                        if (onTimerClick != null) onTimerClick.run();
                    } else if (settingsButtonBounds.contains(p)) {
                        dispose();
                        charlesWindow.onContextMenuClosed();
                        charlesWindow.setContextMenuOpen(false);
                        charlesWindow.showSettingsWindow();
                    } else if (exitButtonBounds.contains(p)) {
                        dispose();
                        charlesWindow.onContextMenuClosed();
                        charlesWindow.setContextMenuOpen(false);
                        charlesWindow.showConfirmationDialog();
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

            drawTextWithBorder(g2d, HEADER);

            if (!charlesWindow.isSurpriseRevealed()) {
                drawGlitchButton(g2d, surpriseButtonBounds, surpriseHovered, BUTTON_COLOR);
            } else {
                drawButton(g2d, surpriseButtonBounds, "Сюрприз!", surpriseHovered, BUTTON_COLOR);
            }
            drawButton(g2d, timerButtonBounds, "Таймер", timerHovered, BUTTON_COLOR);
            drawButton(g2d, settingsButtonBounds, "Настройки", settingsHovered, BUTTON_COLOR);
            drawButton(g2d, exitButtonBounds, "Выход", exitHovered, BUTTON_COLOR);
        }
    }
}
