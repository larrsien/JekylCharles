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

    private static final int OUR_WIDTH  = 250;
    private static final int OUR_HEIGHT = 322; //было 274

    private static final int FONT_SIZE = 13;
    private static final String FONT_NAME = "Georgia";
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final int BORDER_THICKNESS = 1;
    private static final int TEXT_PADDING_TOP = 70;

    private static final Color BUTTON_COLOR = new Color(75, 75, 78);
    private static final int BUTTON_WIDTH  = 120;
    private static final int BUTTON_HEIGHT = 29;
    private static final int BUTTON_GAP    = 8;

    private final Runnable onTimerClick;
    private final Runnable onBirthdayClick;

    private BufferedImage backgroundImage;

    private Rectangle surpriseButtonBounds;
    private Rectangle timerButtonBounds;
    private Rectangle halloweenButtonBounds;
    private Rectangle exitButtonBounds;

    private boolean surpriseHovered = false;
    private boolean timerHovered = false;
    private boolean halloweenHovered = false;
    private boolean exitHovered = false;

    private static final String HEADER = "Мой небольшой каталог ‘дозволенного’. Нажимай, не стесняйся.";
    private final CharlesWindow charlesWindow;

    // для глитч текста в знаке вопроса
    private Timer glitchTimer;
    private String glitchText = "???";
    private int glitchOffsetX = 0;
    private int glitchOffsetY = 0;
    private Color glitchColor = Color.WHITE;
    private boolean glitchActive = false;

    private static final char[] GLITCH_CHARS = {'?', '!', '#', '@', '%', '&', '/', '\\', '|', '<', '>', '█', '▓', '░'};
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
        setSize(OUR_WIDTH, OUR_HEIGHT);
        setAlwaysOnTop(true);

        int buttonX = (OUR_WIDTH - BUTTON_WIDTH) / 2;
        int firstButtonY = TEXT_PADDING_TOP + 47;

        surpriseButtonBounds = new Rectangle(buttonX, firstButtonY, BUTTON_WIDTH, BUTTON_HEIGHT);
        timerButtonBounds = new Rectangle(buttonX, firstButtonY + (BUTTON_HEIGHT + BUTTON_GAP), BUTTON_WIDTH, BUTTON_HEIGHT);
        halloweenButtonBounds = new Rectangle(buttonX, timerButtonBounds.y + BUTTON_HEIGHT + BUTTON_GAP, BUTTON_WIDTH, BUTTON_HEIGHT);
        exitButtonBounds = new Rectangle(buttonX, halloweenButtonBounds.y + BUTTON_HEIGHT + BUTTON_GAP, BUTTON_WIDTH, BUTTON_HEIGHT);

        add(new ContextPanel());

        positionDialog();
        startGlitchEffect();
    }

    private void positionDialog() {
        Point charlesPos = charlesWindow.getLocation();
        int charW = charlesWindow.getWidth();
        int charH = charlesWindow.getHeight();

        int x = charlesPos.x + (charW / 2) - (getWidth() / 2) - 110;

        int y = charlesPos.y + (charH / 2) - 100;

        setLocation(x, y);
    }

    private void startGlitchEffect() {
        glitchTimer = new Timer(100, e -> {
            if (!charlesWindow.isSurpriseRevealed()) {
                // с шансом 30% включаем глитч на один кадр
                glitchActive = random.nextInt(100) < 30;

                if (glitchActive) {
                    // случайно искажаем символы
                    char[] chars = "???".toCharArray();
                    for (int i = 0; i < chars.length; i++) {
                        if (random.nextInt(100) < 50) {
                            chars[i] = GLITCH_CHARS[random.nextInt(GLITCH_CHARS.length)];
                        }
                    }
                    glitchText = new String(chars);

                    // случайное смещение
                    glitchOffsetX = random.nextInt(5) - 2;
                    glitchOffsetY = random.nextInt(3) - 1;

                    // случайный цвет — красноватый или белый
                    glitchColor = random.nextBoolean()
                            ? new Color(255, random.nextInt(50), random.nextInt(50))
                            : Color.WHITE;
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
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("frames/question.png");

            if(inputStream != null) {
                backgroundImage = ImageIO.read(inputStream);
            } else {
                File file = new File("frames/question.png");
                if (file.exists()) {
                    backgroundImage = ImageIO.read(file);
                }
            }

        } catch (IOException e) {
            System.out.println("Не удалось загрузить фон в лоуд бэкграунд имедж");
        }
    }

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
                    halloweenHovered = halloweenButtonBounds.contains(p);
                    exitHovered = exitButtonBounds.contains(p);
                    repaint();
                }
            });

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseExited(MouseEvent e) {
                    surpriseHovered = timerHovered = exitHovered = halloweenHovered = false;
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    Point p = e.getPoint();
                    if (surpriseButtonBounds.contains(p)) {
                        dispose();
                        charlesWindow.onContextMenuClosed();
                        charlesWindow.setContextMenuOpen(false);
                        if (onBirthdayClick != null) {
                            onBirthdayClick.run();
                        }
                    } else if (timerButtonBounds.contains(p)) {
                        charlesWindow.setContextMenuOpen(false);
                        charlesWindow.onContextMenuClosed();
                        dispose();
                        if (onTimerClick != null) onTimerClick.run();
                    } else if (halloweenButtonBounds.contains(p)) {
                        dispose();
                        charlesWindow.onContextMenuClosed();
                        charlesWindow.setContextMenuOpen(false);
                        charlesWindow.showHalloweenReaction();
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

            // Фоновая рамка
            if (backgroundImage != null) {
                g2d.drawImage(backgroundImage, 0, 0, OUR_WIDTH, OUR_HEIGHT, this);
            }

            // Заголовок
            drawTextWithBorder(g2d, HEADER);

            // Кнопки
            if (!charlesWindow.isSurpriseRevealed()) {
                drawGlitchButton(g2d, surpriseButtonBounds, surpriseHovered, BUTTON_COLOR);
            } else {
                drawButton(g2d, surpriseButtonBounds, "Сюрприз!", surpriseHovered, BUTTON_COLOR);
            }
            drawButton(g2d, timerButtonBounds,"Таймер", timerHovered, BUTTON_COLOR);
            drawButton(g2d, halloweenButtonBounds, "Хеллоуин?...", halloweenHovered, BUTTON_COLOR);
            drawButton(g2d, exitButtonBounds, "Выход", exitHovered, BUTTON_COLOR);
        }

        private void drawTextWithBorder(Graphics2D g2d, String text) {
            Font font = new Font(FONT_NAME, Font.BOLD, FONT_SIZE);
            g2d.setFont(font);
            FontMetrics fm = g2d.getFontMetrics();

            String[] lines = text.split("\n");
            int lineHeight = fm.getHeight();
            int startY = TEXT_PADDING_TOP + fm.getAscent();

            for (String line : lines) {
                int x = (OUR_WIDTH - fm.stringWidth(line)) / 2;

                g2d.setColor(Color.BLACK);
                for (int dx = -BORDER_THICKNESS; dx <= BORDER_THICKNESS; dx++) {
                    for (int dy = -BORDER_THICKNESS; dy <= BORDER_THICKNESS; dy++) {
                        if (dx != 0 || dy != 0) {
                            g2d.drawString(line, x + dx, startY + dy);
                        }
                    }
                }

                g2d.setColor(TEXT_COLOR);
                g2d.drawString(line, x, startY);
                startY += lineHeight;
            }
        }

        private void drawButton(Graphics2D g2d, Rectangle bounds, String text,
                                boolean isHovered, Color baseColor) {
            Color buttonColor = isHovered ? new Color(68, 63, 82) : baseColor;

            g2d.setColor(buttonColor);
            g2d.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 12, 12);

            g2d.setColor(new Color(148, 143, 165));
            g2d.setStroke(new BasicStroke(1.5f));
            g2d.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 12, 12);

            Font buttonFont = new Font(FONT_NAME, Font.BOLD, FONT_SIZE);
            g2d.setFont(buttonFont);
            FontMetrics fm = g2d.getFontMetrics();

            int textX = bounds.x + (bounds.width  - fm.stringWidth(text)) / 2;
            int textY = bounds.y + (bounds.height - fm.getHeight()) / 2 + fm.getAscent();

            // Тень (чуть синеватая, не чисто чёрная)
            g2d.setColor(new Color(0, 0, 20, 130));
            g2d.drawString(text, textX + 1, textY + 1);

            // Основной текст — чуть жемчужный, не ярко-белый
            g2d.setColor(new Color(230, 226, 238));
            g2d.drawString(text, textX, textY);
        }
    }

    private void drawGlitchButton(Graphics2D g2d, Rectangle bounds, boolean isHovered, Color baseColor) {
        Color buttonColor = isHovered ? new Color(68, 63, 82) : baseColor;  // ← как у drawButton

        int bx = bounds.x + (glitchActive ? glitchOffsetX : 0);
        int by = bounds.y + (glitchActive ? glitchOffsetY : 0);

        g2d.setColor(buttonColor);
        g2d.fillRoundRect(bx, by, bounds.width, bounds.height, 12, 12);

        // Обводка — как у всех остальных кнопок
        g2d.setColor(new Color(148, 143, 165));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(bx, by, bounds.width, bounds.height, 12, 12);

        Font buttonFont = new Font(FONT_NAME, Font.BOLD, 14);
        g2d.setFont(buttonFont);
        FontMetrics fm = g2d.getFontMetrics();

        int textX = bounds.x + (bounds.width - fm.stringWidth(glitchText)) / 2 + glitchOffsetX;
        int textY = bounds.y + (bounds.height - fm.getHeight()) / 2 + fm.getAscent() + glitchOffsetY;

        if (glitchActive) {
            g2d.setColor(new Color(255, 0, 0, 150));
            g2d.drawString(glitchText, textX + 2, textY);
            g2d.setColor(new Color(0, 0, 255, 150));
            g2d.drawString(glitchText, textX - 2, textY);
        }

        // Тень — как у обычных кнопок
        g2d.setColor(new Color(0, 0, 20, 130));
        g2d.drawString(glitchText, textX + 1, textY + 1);

        g2d.setColor(glitchColor);
        g2d.drawString(glitchText, textX, textY);
    }
}
