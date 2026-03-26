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

public class ReactionBubble extends JWindow {

    private static final int OUR_WIDTH = 300;
    private static final int OUR_HEIGHT = 150;

    private static final int TEXT_PADDING_LEFT = 50;
    private static final int TEXT_PADDING_TOP = 43;
    private static final int TEXT_PADDING_RIGHT = 25;
    private static final int TEXT_PADDING_BOTTOM = 40;

    private static final int FONT_SIZE = 12;
    private static final String FONT_NAME = "Georgia";
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final int BORDER_THICKNESS = 1;

    private static final int TYPING_SPEED = 50;
    private static final int AUTO_ADVANCE_DELAY = 1000;
    private static final int FINAL_PAGE_DELAY = 3000;

    private final CharacterWindow characterWindow;
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

    public ReactionBubble(String reaction, CharacterWindow characterWindow, Runnable onDone) {
        this.characterWindow = characterWindow;
        this.onDone = onDone;
        this.reaction = reaction;

        loadBackgroundImage();
        initializeWindow();
        splitTextIntoPages();
        startTyping();
        scheduleAutoClose();
    }


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
        Point loc = characterWindow.getLocation();
        int charW = characterWindow.getWidth();
        int charH = characterWindow.getHeight();

        int x = loc.x + charW / 2 - getWidth() / 2;
        int y = loc.y + charH - getHeight() - 40;

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
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("frames/reaction.png");

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
        int maxLines = Math.max(1, availableHeight / lineHeight);

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

        if (currentPage < textPages.size() - 1) {
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
        }
    }

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

        return typingTime + pageTransitionTime + FINAL_PAGE_DELAY;
    }

    public void cleanup() {
        if (typingTimer != null)      typingTimer.stop();
        if (autoAdvanceTimer != null)  autoAdvanceTimer.stop();
        if (closeTimer != null)        closeTimer.stop();
        dispose();

        if (onDone != null) {
            onDone.run();
        }
    }

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
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            if (backgroundImage != null) {
                g2d.drawImage(backgroundImage, 0, 0, OUR_WIDTH, OUR_HEIGHT, this);
            }

            if (currentDisplayText != null && !currentDisplayText.isEmpty()) {
                drawText(g2d);
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
    }
}
