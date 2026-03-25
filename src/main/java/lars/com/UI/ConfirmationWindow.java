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

    // Отступы
    private static final int TEXT_PADDING_TOP = 40;
    private static final int BUTTON_PADDING = 40;
    private static final int BUTTON_WIDTH = 80;
    private static final int BUTTON_HEIGHT = 35;
    private static final int BUTTON_SPACING = 20;

    // Шрифт
    private static final int FONT_SIZE = 13;
    private static final String FONT_NAME = "Georgia";
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final int BORDER_THICKNESS = 1;

    private final AmonWindow amonWindow;
    private BufferedImage backgroundImage;
    private ConfirmationPanel confirmationPanel;

    private final String message;
    private final Runnable onConfirm;
    private final Runnable onCancel;

    public ConfirmationWindow(String message, AmonWindow amonWindow, Runnable onConfirm, Runnable onCancel) {
        this.message = message;
        this.amonWindow = amonWindow;
        this.onConfirm = onConfirm;
        this.onCancel = onCancel;

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
        Point amonLocation = amonWindow.getLocation();

        int x = amonLocation.x - getWidth() - 10;
        int y = amonLocation.y + 130;

        setLocation(x, y);
    }

    private void loadBackgroundImage() {
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("frames/reaction.png");

            if (inputStream != null) {
                backgroundImage = ImageIO.read(inputStream);
            } else {
                System.out.println("Фон диалога не найден в ресурсах");
            }
        } catch (IOException e) {
            System.out.println("Ошибка загрузки фона диалога: " + e.getMessage());
        }
    }

    public void cleanup() {
        dispose();
        System.out.println("Диалог подтверждения закрыт");
    }

    private class ConfirmationPanel extends JPanel {
        private Rectangle yesButtonBounds;
        private Rectangle noButtonBounds;
        private boolean yesButtonHovered = false;
        private boolean noButtonHovered = false;

        public ConfirmationPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(OUR_WIDTH, OUR_HEIGHT));

            // Вычисляем позиции кнопок
            int totalButtonWidth = BUTTON_WIDTH * 2 + BUTTON_SPACING;
            int startX = (OUR_WIDTH - totalButtonWidth) / 2;
            int buttonY = OUR_HEIGHT - BUTTON_HEIGHT - BUTTON_PADDING;

            yesButtonBounds = new Rectangle(startX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT);
            noButtonBounds = new Rectangle(startX + BUTTON_WIDTH + BUTTON_SPACING, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT);

            setupMouseListener();
        }

        private void setupMouseListener() {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    Point clickPoint = e.getPoint();

                    if (yesButtonBounds.contains(clickPoint)) {
                        cleanup();
                        if (onConfirm != null) {
                            onConfirm.run();
                        }
                    } else if (noButtonBounds.contains(clickPoint)) {
                        cleanup();
                        if (onCancel != null) {
                            onCancel.run();
                        }
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    yesButtonHovered = false;
                    noButtonHovered = false;
                    repaint();
                }
            });

            addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    Point mousePoint = e.getPoint();

                    boolean wasYesHovered = yesButtonHovered;
                    boolean wasNoHovered = noButtonHovered;

                    yesButtonHovered = yesButtonBounds.contains(mousePoint);
                    noButtonHovered = noButtonBounds.contains(mousePoint);

                    // Перерисовываем только если состояние изменилось
                    if (wasYesHovered != yesButtonHovered || wasNoHovered != noButtonHovered) {
                        repaint();
                    }

                    // Меняем курсор
                    if (yesButtonHovered || noButtonHovered) {
                        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    } else {
                        setCursor(Cursor.getDefaultCursor());
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            // Включаем антиалиасинг
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Рисуем фоновое изображение
            if (backgroundImage != null) {
                g2d.drawImage(backgroundImage, 0, 0, OUR_WIDTH, OUR_HEIGHT, this);
            }

            // Рисуем текст вопроса
            drawTextWithBorder(g2d, message);

            // Рисуем кнопки
            drawButton(g2d, yesButtonBounds, "Да", yesButtonHovered, new Color(79, 71, 73));
            drawButton(g2d, noButtonBounds, "Нет", noButtonHovered, new Color(79, 71, 73));
        }

        private void drawTextWithBorder(Graphics2D g2d, String text) {
            Font font = new Font(FONT_NAME, Font.BOLD, FONT_SIZE);
            g2d.setFont(font);

            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(text);
            int x = (OUR_WIDTH - textWidth) / 2;
            int y = TEXT_PADDING_TOP + fm.getAscent();

            // Рисуем чёрную обводку
            g2d.setColor(Color.BLACK);
            for (int dx = -BORDER_THICKNESS; dx <= BORDER_THICKNESS; dx++) {
                for (int dy = -BORDER_THICKNESS; dy <= BORDER_THICKNESS; dy++) {
                    if (dx != 0 || dy != 0) {
                        g2d.drawString(text, x + dx, y + dy);
                    }
                }
            }

            // Рисуем основной текст
            g2d.setColor(TEXT_COLOR);
            g2d.drawString(text, x, y);
        }

        private void drawButton(Graphics2D g2d, Rectangle bounds, String text, boolean isHovered, Color baseColor) {
            // Цвет кнопки (ярче при наведении)
            Color buttonColor = isHovered ? baseColor.brighter() : baseColor;

            // Рисуем фон кнопки с закругленными углами
            g2d.setColor(buttonColor);
            g2d.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 10, 10);

            // Рисуем границу кнопки
            g2d.setColor(buttonColor.darker());
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 10, 10);

            // Рисуем текст кнопки
            Font buttonFont = new Font(FONT_NAME, Font.BOLD, 14);
            g2d.setFont(buttonFont);
            FontMetrics fm = g2d.getFontMetrics();

            int textWidth = fm.stringWidth(text);
            int textX = bounds.x + (bounds.width - textWidth) / 2;
            int textY = bounds.y + ((bounds.height - fm.getHeight()) / 2) + fm.getAscent();

            // Тень текста
            g2d.setColor(new Color(0, 0, 0, 100));
            g2d.drawString(text, textX + 1, textY + 1);

            // Основной текст
            g2d.setColor(Color.WHITE);
            g2d.drawString(text, textX, textY);
        }
    }
}



