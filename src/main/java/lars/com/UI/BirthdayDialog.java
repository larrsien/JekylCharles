package lars.com.UI;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class BirthdayDialog extends JWindow {

    private static final int OUR_WIDTH = 230;
    private static final int OUR_HEIGHT = 290;

    private static final String HEADER = "Кого выбираете?";
    private static final String FONT_NAME = "Georgia";
    private static final int FONT_SIZE = 13;
    private static final int BORDER_THICKNESS = 1;
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final int TEXT_PADDING_TOP = 70;

    private static final Color BUTTON_COLOR = new Color(75, 75, 78);
    private static final int BUTTON_WIDTH  = 100;
    private static final int BUTTON_HEIGHT = 28;
    private static final int BUTTON_GAP = 8;

    private static final String[] PEOPLE = {
            "Аден",
            "Катя",
            "Каргли",
            "Янчи"
    };

    private static final String[] CONGRATULATIONS = {
            "самое большое и важное поздравление ждет тебя впереди — ты откопаешь его чуть позже. а пока я просто хочу сказать: я действительно очень дорожу тобой. хонорабл меншн ту вран за то, что она вложила свои силы и ресурсы в этого пета. я так же постаралась вложить сюда всю свою любовь и те крошечные мелочи, которые понятны только нам. мне хотелось создать для тебя что-то, что будет наполнено нашими общими воспоминаниями, нашими шутками и мирами, которые мы построили. и тем, сколько мы создали вместе. с днем рождения.",
            "привет дуня. если бы шарль и джекилл были женщинами ты бы видел меня не как поздравление а отцифрованную версию меня с ними на свиданиях. к счастью они мужчины и засейвили мое поздравление тебе. с днеееееееееем РОЖДЕНИЯ с днем тебя happy doonya daaaaaaaaaay!!! улыбайся так много и часто как ты улыбаешься сегодня. ты очень любим всеми, кто тебя окружает, живи счастливую жизнь, дари любовь в ответ и привыкай к дням рождения как к праздникам потому что теперь тебя никто не оставит",
            "короче. вот мое поздравление для дуни. желаю тебе никогда не мыться",
            "дуня дуНЕЧКА ДУНЯША!!! ПРИВЕТ!!! С ДНЁМ РОЖДЕНИЯ!!!!! никогда не болей и не грусти, пусть всё будет у тебя замечательно пожалуйста я очень прошу!!!! ты классный никогда не лысей!!!! а ещё не слушай каргли мыться полезно!!!!"
    };

    private final CharlesWindow charlesWindow;
    private BufferedImage backgroundImage;

    private final Rectangle[] buttonBounds = new Rectangle[PEOPLE.length];
    private final boolean[] hovered = new boolean[PEOPLE.length];


    public BirthdayDialog(CharlesWindow charlesWindow) {
        this.charlesWindow = charlesWindow;

        loadBackgroundImage();
        initializeWindow();

        addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowLostFocus(WindowEvent e) {
                close();
            }
        });
    }

    private void close() {
        charlesWindow.setBirthdayOpen(false);
        dispose();
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

    private void initializeWindow() {
        setSize(OUR_WIDTH, OUR_HEIGHT);
        setBackground(new Color(0, 0, 0, 0));
        setAlwaysOnTop(true);
        setFocusableWindowState(true);

        int buttonX = (OUR_WIDTH - BUTTON_WIDTH) / 2;
        int firstButtonY = TEXT_PADDING_TOP + 30;

        for (int i = 0; i < PEOPLE.length; i++) {
            buttonBounds[i] = new Rectangle(buttonX, firstButtonY + i * (BUTTON_HEIGHT + BUTTON_GAP), BUTTON_WIDTH,
                    BUTTON_HEIGHT);
        }

        add(new DialogPanel());

        positionDialog();
    }

    private void positionDialog() {
        Point charlesPos = charlesWindow.getLocation();
        int charW = charlesWindow.getWidth();
        int charH = charlesWindow.getHeight();

        int x = charlesPos.x + (charW / 2) - (getWidth() / 2) - 110;

        int y = charlesPos.y + (charH / 2) - 100;

        setLocation(x, y);
    }

    private class DialogPanel extends JPanel {
        public DialogPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(OUR_WIDTH, OUR_HEIGHT));

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    Point p = e.getPoint();
                    for (int i = 0; i < PEOPLE.length; i++) {
                        hovered[i] = buttonBounds[i].contains(p);
                    }
                    repaint();
                }
            });

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseExited(MouseEvent e) {
                    for (int i = 0; i < hovered.length; i++) hovered[i] = false;
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    Point p = e.getPoint();
                    for (int i = 0; i < PEOPLE.length; i++) {
                        if (buttonBounds[i].contains(p)) {
                            charlesWindow.setBirthdayOpen(false);
                            dispose();
                            charlesWindow.reactToEvent(CONGRATULATIONS[i]);
                            return;
                        }
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
            for (int i = 0; i < PEOPLE.length; i++) {
                drawButton(g2d, buttonBounds[i], PEOPLE[i], hovered[i], BUTTON_COLOR);
            }
        }

        private void drawTextWithBorder(Graphics2D g2d, String text) {
            Font font = new Font(FONT_NAME, Font.BOLD, FONT_SIZE);
            g2d.setFont(font);

            FontMetrics fm = g2d.getFontMetrics();
            int x = (OUR_WIDTH - fm.stringWidth(text)) / 2;
            int y = TEXT_PADDING_TOP + fm.getAscent();

            // Чёрная обводка
            g2d.setColor(Color.BLACK);
            for (int dx = -BORDER_THICKNESS; dx <= BORDER_THICKNESS; dx++) {
                for (int dy = -BORDER_THICKNESS; dy <= BORDER_THICKNESS; dy++) {
                    if (dx != 0 || dy != 0) {
                        g2d.drawString(text, x + dx, y + dy);
                    }
                }
            }

            // Основной текст
            g2d.setColor(TEXT_COLOR);
            g2d.drawString(text, x, y);
        }

        private void drawButton(Graphics2D g2d, Rectangle bounds, String text,
                                boolean isHovered, Color baseColor) {
            Color buttonColor = isHovered ? baseColor.brighter() : baseColor;

            g2d.setColor(buttonColor);
            g2d.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 10, 10);

            g2d.setColor(new Color(130, 127, 145));
            g2d.setStroke(new BasicStroke(1.5f));
            g2d.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 10, 10);

            Font buttonFont = new Font(FONT_NAME, Font.BOLD, FONT_SIZE);
            g2d.setFont(buttonFont);
            FontMetrics fm = g2d.getFontMetrics();

            int textX = bounds.x + (bounds.width  - fm.stringWidth(text)) / 2;
            int textY = bounds.y + (bounds.height  - fm.getHeight()) / 2 + fm.getAscent();

            // Тень текста
            g2d.setColor(new Color(0, 0, 0, 100));
            g2d.drawString(text, textX + 1, textY + 1);

            // Основной текст
            g2d.setColor(Color.WHITE);
            g2d.drawString(text, textX, textY);
        }
    }

}
