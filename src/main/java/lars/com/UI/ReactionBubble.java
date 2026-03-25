package lars.com.UI;

import java.util.List;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

public class ReactionBubble extends JWindow {

    private static final int OUR_WIDTH = 300;
    private static final int OUR_HEIGHT = 150;

    private static final int TEXT_PADDING_LEFT = 30;
    private static final int TEXT_PADDING_TOP = 40;
    private static final int TEXT_PADDING_RIGHT = 25;
    private static final int TEXT_PADDING_BOTTOM = 40;

    private static final int FONT_SIZE = 13;
    private static final String FONT_NAME = "Georgia";
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final int BORDER_THICKNESS = 1;

    private static final int TYPING_SPEED = 50;
    private static final int AUTO_ADVANCE_DELAY = 1000;
    private static final int FINAL_PAGE_DELAY = 3000;

    // ─── Глитч-константы ────────────────────────────────────────────────────
    private static final char[] GLITCH_CHARS =
            {'▓','░','▒','│','┤','║','╗','╝','┐','└','┘','┌','═','╬','◆','▪','╫','╪','▀','▄'};
    private static final int GLITCH_NOISE_FRAMES = 10;
    private static final int GLITCH_SPEED = 90;
    private static final Color COLOR_NOISE    = new Color(200,210,225, 115);
    private static final Color COLOR_SETTLING = new Color(210,220,235, 204);
    private static final Color COLOR_FINAL    = new Color(230,238,248, 255);

    private static final int FLICKER_PERIOD_MS = 260;
    private static final int FLICKER_DURATION  = 1800;

    private static final String GLITCH_FONT_NAME = "Courier New";

    // ─── Глитч-состояние ────────────────────────────────────────────────────
    private String glitchFragment   = null;
    private String glitchDisplay    = "";
    private Timer glitchTimer;
    private Timer flickerTimer;
    private int glitchPhase = 0;
    private int glitchFrameCount = 0;
    private boolean flickerVisible = true;
    private final Random glitchRandom = new Random();

    // ═══ КЛЮЧЕВОЕ ИЗМЕНЕНИЕ: CharacterWindow вместо AmonWindow ═══
    private final CharacterWindow parentWindow;
    private final Runnable onDone;

    private BufferedImage backgroundImage;
    private ReactionPanel reactionPanel;

    private final String reaction;
    private List<String> textPages;
    private int currentPage = 0;
    private String currentDisplayText = "";
    private int currentCharIndex = 0;

    private Timer typingTimer;
    private Timer autoAdvanceTimer;
    private Timer closeTimer;

    private boolean isTyping = false;

    // ════════════════════════════════════════════════════════════════════════
    //  Конструкторы
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Новый конструктор для CharacterWindow + DialogueQueue.
     */
    public ReactionBubble(String reaction, CharacterWindow parentWindow, Runnable onDone) {
        this.parentWindow = parentWindow;
        this.onDone       = onDone;
        this.reaction     = parseGlitchMarker(reaction);

        loadBackgroundImage();
        initializeWindow();
        splitTextIntoPages();
        startTyping();
        scheduleAutoClose();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Глитч-маркеры
    // ════════════════════════════════════════════════════════════════════════

    private boolean isGlitchEnabled() {
        // Глитч поддерживается только для Шарля
        if (parentWindow instanceof CharlesWindow) {
            return ((CharlesWindow) parentWindow).isGlitchEnabled();
        }
        return false;
    }

    private String parseGlitchMarker(String raw) {
        if (raw == null) return "";

        if (!isGlitchEnabled()) {
            return raw.replaceAll("(?i)\\[GLITCH:[^\\]]*]", "").trim();
        }

        int start = raw.indexOf("[GLITCH:");
        if (start == -1) return raw;
        int end   = raw.indexOf("]", start);
        if (end == -1) return raw;

        String fragment = raw.substring(start + 8, end).trim();
        glitchFragment  = fragment;

        String cleaned  = raw.substring(0, start).trim() + raw.substring(end + 1);
        return cleaned.trim();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Инициализация
    // ════════════════════════════════════════════════════════════════════════

    private void initializeWindow() {
        setSize(OUR_WIDTH, OUR_HEIGHT);
        setAlwaysOnTop(true);
        setBackground(new Color(0, 0, 0, 0));

        reactionPanel = new ReactionPanel();
        add(reactionPanel);

        positionReaction();
        setupMouseListener();
    }

    private void positionReaction() {
        Point loc = parentWindow.getLocation();
        int x = loc.x - getWidth() + 20;
        int y = loc.y + 130;
        setLocation(x, y);
    }

    private void setupMouseListener() {
        reactionPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (isTyping) {
                    skipTyping();
                }
            }
        });
    }

    private void loadBackgroundImage() {
        try {
            InputStream inputStream = getClass().getClassLoader()
                    .getResourceAsStream("frames/reaction.png");
            if (inputStream != null) {
                backgroundImage = ImageIO.read(inputStream);
            } else {
                File file = new File("frames/reaction.png");
                if (file.exists()) {
                    backgroundImage = ImageIO.read(file);
                }
            }
        } catch (IOException e) {
            System.out.println("Ошибка загрузки фона диалога: " + e.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Пагинация текста
    // ════════════════════════════════════════════════════════════════════════

    private void splitTextIntoPages() {
        textPages = new ArrayList<>();

        if (reaction == null || reaction.isEmpty()) {
            textPages.add("");
            return;
        }

        FontMetrics fm = reactionPanel.getFontMetrics(new Font(FONT_NAME, Font.BOLD, FONT_SIZE));
        int availableWidth  = OUR_WIDTH  - TEXT_PADDING_LEFT - TEXT_PADDING_RIGHT;
        int availableHeight = OUR_HEIGHT - TEXT_PADDING_TOP  - TEXT_PADDING_BOTTOM;
        int lineHeight = fm.getHeight();
        int maxLines   = Math.max(1, availableHeight / lineHeight);

        String[] words = reaction.split(" ");
        StringBuilder currentLine = new StringBuilder();
        List<String> currentPageLines = new ArrayList<>();

        for (String word : words) {
            String test = currentLine.length() == 0 ? word : currentLine + " " + word;
            if (fm.stringWidth(test) > availableWidth) {
                currentPageLines.add(currentLine.toString());
                currentLine = new StringBuilder(word);

                if (currentPageLines.size() >= maxLines) {
                    textPages.add(String.join("\n", currentPageLines));
                    currentPageLines.clear();
                }
            } else {
                currentLine = new StringBuilder(test);
            }
        }

        if (currentLine.length() > 0) {
            currentPageLines.add(currentLine.toString());
        }
        if (!currentPageLines.isEmpty()) {
            textPages.add(String.join("\n", currentPageLines));
        }

        if (textPages.isEmpty()) {
            textPages.add("");
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Печать текста
    // ════════════════════════════════════════════════════════════════════════

    private void startTyping() {
        String pageText = textPages.get(currentPage);
        currentDisplayText = "";
        currentCharIndex = 0;
        isTyping = true;

        typingTimer = new Timer(TYPING_SPEED, e -> {
            if (currentCharIndex < pageText.length()) {
                currentDisplayText = pageText.substring(0, ++currentCharIndex);
                reactionPanel.repaint();
            } else {
                finishTyping();
            }
        });
        typingTimer.start();
    }

    private void finishTyping() {
        if (typingTimer != null) typingTimer.stop();
        isTyping = false;

        if (currentPage == textPages.size() - 1 && glitchFragment != null) {
            startGlitchAnimation();
        } else if (currentPage < textPages.size() - 1) {
            scheduleAutoAdvance();
        }
    }

    private void skipTyping() {
        if (typingTimer != null) typingTimer.stop();
        currentDisplayText = textPages.get(currentPage);
        isTyping = false;
        reactionPanel.repaint();

        if (currentPage < textPages.size() - 1) {
            scheduleAutoAdvance();
        } else if (glitchFragment != null) {
            startGlitchAnimation();
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Глитч-анимация
    // ════════════════════════════════════════════════════════════════════════

    private void startGlitchAnimation() {
        glitchPhase      = 1;
        glitchFrameCount = 0;
        glitchDisplay    = buildNoise(glitchFragment.length());
        reactionPanel.repaint();

        glitchTimer = new Timer(GLITCH_SPEED, e -> tickGlitch());
        glitchTimer.start();
    }

    private void tickGlitch() {
        glitchFrameCount++;

        if (glitchPhase == 1) {
            glitchDisplay = buildNoise(glitchFragment.length());
            if (glitchFrameCount >= GLITCH_NOISE_FRAMES) {
                glitchPhase      = 2;
                glitchFrameCount = 0;
            }
        } else if (glitchPhase == 2) {
            int revealed = glitchFrameCount + 1;
            if (revealed >= glitchFragment.length()) {
                glitchDisplay = glitchFragment;
                glitchPhase   = 3;
                glitchTimer.stop();
                startFlickerPhase();
                return;
            }
            StringBuilder sb = new StringBuilder();
            sb.append(glitchFragment, 0, revealed);
            for (int i = revealed; i < glitchFragment.length(); i++) {
                char c = glitchFragment.charAt(i);
                sb.append((c == ' ' || c == '◈')
                        ? c
                        : GLITCH_CHARS[glitchRandom.nextInt(GLITCH_CHARS.length)]);
            }
            glitchDisplay = sb.toString();
        }
        reactionPanel.repaint();
    }

    private void startFlickerPhase() {
        flickerVisible = true;
        flickerTimer = new Timer(FLICKER_PERIOD_MS, e -> {
            flickerVisible = !flickerVisible;
            reactionPanel.repaint();
        });
        flickerTimer.start();

        Timer stopFlicker = new Timer(FLICKER_DURATION, e -> {
            flickerTimer.stop();
            flickerVisible = true;
            glitchPhase = 4;
            reactionPanel.repaint();

            if (closeTimer != null) closeTimer.stop();
            closeTimer = new Timer(7000, ev -> cleanup());
            closeTimer.setRepeats(false);
            closeTimer.start();
        });
        stopFlicker.setRepeats(false);
        stopFlicker.start();
    }

    private String buildNoise(int length) {
        StringBuilder sb = new StringBuilder();
        sb.append("◈ ");
        int inner = Math.max(0, length - 4);
        for (int i = 0; i < inner; i++) {
            sb.append(GLITCH_CHARS[glitchRandom.nextInt(GLITCH_CHARS.length)]);
        }
        sb.append(" ◈");
        return sb.toString();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Таймеры
    // ════════════════════════════════════════════════════════════════════════

    private void scheduleAutoAdvance() {
        autoAdvanceTimer = new Timer(AUTO_ADVANCE_DELAY, e -> nextPage());
        autoAdvanceTimer.setRepeats(false);
        autoAdvanceTimer.start();
    }

    private void nextPage() {
        currentPage++;
        if (currentPage < textPages.size()) {
            startTyping();
        }
    }

    private void scheduleAutoClose() {
        int closeDelay = calculateTotalDisplayTime();
        closeTimer = new Timer(closeDelay, e -> cleanup());
        closeTimer.setRepeats(false);
        closeTimer.start();
    }

    private int calculateTotalDisplayTime() {
        if (reaction == null || reaction.isEmpty()) return 1000;

        int typingTime = reaction.length() * TYPING_SPEED;
        int pageTransitionTime = (textPages != null && textPages.size() > 1)
                ? (textPages.size() - 1) * AUTO_ADVANCE_DELAY : 0;
        int glitchTime = (glitchFragment != null)
                ? (GLITCH_NOISE_FRAMES + glitchFragment.length()) * GLITCH_SPEED + FLICKER_DURATION + 500
                : 0;
        int finalDelay = (glitchFragment != null) ? 4000 : FINAL_PAGE_DELAY;

        return typingTime + pageTransitionTime + glitchTime + finalDelay;
    }

    public int getTotalDisplayTime() {
        return calculateTotalDisplayTime();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Cleanup
    // ════════════════════════════════════════════════════════════════════════

    public void cleanup() {
        if (typingTimer != null)      typingTimer.stop();
        if (autoAdvanceTimer != null)  autoAdvanceTimer.stop();
        if (closeTimer != null)        closeTimer.stop();
        if (glitchTimer != null)       glitchTimer.stop();
        if (flickerTimer != null)      flickerTimer.stop();
        dispose();

        // Уведомляем DialogueQueue
        if (onDone != null) {
            onDone.run();
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Отрисовка
    // ════════════════════════════════════════════════════════════════════════

    private void drawStringWithBorder(Graphics2D g2d, String text, int x, int y, Color color) {
        g2d.setColor(new Color(0, 0, 0, 150));
        for (int dx = -BORDER_THICKNESS; dx <= BORDER_THICKNESS; dx++) {
            for (int dy = -BORDER_THICKNESS; dy <= BORDER_THICKNESS; dy++) {
                if (dx != 0 || dy != 0) {
                    g2d.drawString(text, x + dx, y + dy);
                }
            }
        }
        g2d.setColor(color);
        g2d.drawString(text, x, y);
    }

    private class ReactionPanel extends JPanel {
        ReactionPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(OUR_WIDTH, OUR_HEIGHT));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            if (backgroundImage != null) {
                g2d.drawImage(backgroundImage, 0, 0, OUR_WIDTH, OUR_HEIGHT, this);
            }

            if (currentDisplayText != null && !currentDisplayText.isEmpty()) {
                drawText(g2d);
            }

            // Глитч-фрагмент
            if (glitchPhase > 0 && glitchDisplay != null && !glitchDisplay.isEmpty()) {
                drawGlitch(g2d);
            }
        }

        private void drawText(Graphics2D g2d) {
            Font font = new Font(FONT_NAME, Font.BOLD, FONT_SIZE);
            FontMetrics fm = g2d.getFontMetrics(font);
            g2d.setFont(font);

            String[] lines = currentDisplayText.split("\n");
            int y = TEXT_PADDING_TOP + fm.getAscent();

            for (String line : lines) {
                drawStringWithBorder(g2d, line, TEXT_PADDING_LEFT, y, TEXT_COLOR);
                y += fm.getHeight();
            }
        }

        private void drawGlitch(Graphics2D g2d) {
            if (glitchPhase == 3 && !flickerVisible) return;

            Font font = new Font(GLITCH_FONT_NAME, Font.BOLD, FONT_SIZE - 1);
            g2d.setFont(font);
            FontMetrics fm = g2d.getFontMetrics(font);

            int x = (OUR_WIDTH - fm.stringWidth(glitchDisplay)) / 2;
            int y = OUR_HEIGHT - TEXT_PADDING_BOTTOM + 5;

            Color color;
            switch (glitchPhase) {
                case 1: color = COLOR_NOISE;    break;
                case 2: color = COLOR_SETTLING; break;
                default: color = COLOR_FINAL;   break;
            }
            drawStringWithBorder(g2d, glitchDisplay, x, y, color);
        }
    }
}
