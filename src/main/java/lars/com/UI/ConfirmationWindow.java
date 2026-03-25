package lars.com.UI;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class ConfirmationWindow extends JWindow {

    private static final int OUR_WIDTH = 300;
    private static final int OUR_HEIGHT = 150;

    private static final int TEXT_PADDING_TOP = 40;
    private static final int BUTTON_PADDING = 40;
    private static final int BUTTON_WIDTH = 80;
    private static final int BUTTON_HEIGHT = 35;
    private static final int BUTTON_SPACING = 20;

    private static final int FONT_SIZE = 13;
    private static final String FONT_NAME = "Georgia";
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final int BORDER_THICKNESS = 1;

    private final CharlesWindow charlesWindow;
    private BufferedImage backgroundImage;
    private ConfirmationPanel confirmationPanel;

    private final String message;
    private final Runnable onConfirm;
    private final Runnable onCancel;

    public ConfirmationWindow(String message, CharlesWindow charlesWindow,
                              Runnable onConfirm, Runnable onCancel) {
        this.message       = message;
        this.charlesWindow = charlesWindow;
        this.onConfirm     = onConfirm;
        this.onCancel      = onCancel;

        loadBackgroundImage();
        initializeWindow();
    }

    private void initializeWindow() {
        setSize(OUR_WIDTH, OUR_HEIGHT);
        setAlwaysOnTop(true);
        setBackground(new Color(0, 0, 0, 0));

        confirmationPanel = new ConfirmationPanel();
        add(confirmationPanel);

        positionDialog();
    }

    private void positionDialog() {
        Point loc = charlesWindow.getLocation();
        int x = loc.x - getWidth() - 10;
        int y = loc.y + 130;
        setLocation(x, y);
    }

    private void loadBackgroundImage() {
        try {
            InputStream is = getClass().getClassLoader()
                    .getResourceAsStream("frames/reaction.png");
            if (is != null) {
                backgroundImage = ImageIO.read(is);
            }
        } catch (IOException e) {
            System.out.println("Ошибка загрузки фона подтверждения");
        }
    }

    private void drawStringWithBorder(Graphics2D g2d, String text, int x, int y) {
        g2d.setColor(new Color(0, 0, 0, 150));
        for (int dx = -BORDER_THICKNESS; dx <= BORDER_THICKNESS; dx++) {
            for (int dy = -BORDER_THICKNESS; dy <= BORDER_THICKNESS; dy++) {
                if (dx != 0 || dy != 0) g2d.drawString(text, x + dx, y + dy);
            }
        }
        g2d.setColor(TEXT_COLOR);
        g2d.drawString(text, x, y);
    }

    private class ConfirmationPanel extends JPanel {

        private Rectangle yesButton;
        private Rectangle noButton;
        private boolean yesHovered = false;
        private boolean noHovered = false;

        ConfirmationPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(OUR_WIDTH, OUR_HEIGHT));

            int totalWidth = BUTTON_WIDTH * 2 + BUTTON_SPACING;
            int startX = (OUR_WIDTH - totalWidth) / 2;
            int buttonY = OUR_HEIGHT - BUTTON_HEIGHT - 25;

            yesButton = new Rectangle(startX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT);
            noButton  = new Rectangle(startX + BUTTON_WIDTH + BUTTON_SPACING, buttonY,
                    BUTTON_WIDTH, BUTTON_HEIGHT);

            addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    yesHovered = yesButton.contains(e.getPoint());
                    noHovered  = noButton.contains(e.getPoint());
                    repaint();
                }
            });

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseExited(MouseEvent e) {
                    yesHovered = noHovered = false;
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    Point p = e.getPoint();
                    if (yesButton.contains(p)) {
                        dispose();
                        if (onConfirm != null) onConfirm.run();
                    } else if (noButton.contains(p)) {
                        dispose();
                        if (onCancel != null) onCancel.run();
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

            // Текст сообщения
            g2d.setFont(new Font(FONT_NAME, Font.BOLD, FONT_SIZE));
            FontMetrics fm = g2d.getFontMetrics();
            int textX = (OUR_WIDTH - fm.stringWidth(message)) / 2;
            drawStringWithBorder(g2d, message, textX, TEXT_PADDING_TOP);

            // Кнопки
            Color btnColor = new Color(79, 71, 73);
            drawButton(g2d, yesButton, "Да", yesHovered, btnColor);
            drawButton(g2d, noButton,  "Нет", noHovered, btnColor);
        }

        private void drawButton(Graphics2D g2d, Rectangle bounds, String label,
                                boolean hovered, Color color) {
            g2d.setColor(hovered ? color.brighter() : color);
            g2d.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 8, 8);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font(FONT_NAME, Font.PLAIN, FONT_SIZE));
            FontMetrics fm = g2d.getFontMetrics();
            int tx = bounds.x + (bounds.width - fm.stringWidth(label)) / 2;
            int ty = bounds.y + (bounds.height + fm.getAscent()) / 2 - 2;
            g2d.drawString(label, tx, ty);
        }
    }
}



