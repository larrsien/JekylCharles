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
    private static final int OUR_HEIGHT = 390;

    private static final String HEADER = "Кого выбираете?";
    private static final String FONT_NAME = "Georgia";
    private static final int FONT_SIZE = 13;
    private static final int BORDER_THICKNESS = 1;
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final int TEXT_PADDING_TOP = 44;

    private static final Color BUTTON_COLOR = new Color(79, 71, 73);
    private static final int BUTTON_WIDTH = 148;
    private static final int BUTTON_HEIGHT = 34;
    private static final int BUTTON_GAP = 10;

    private static final String[] PEOPLE = {
            "Рен",
            "Искра",
            "Наната",
            "Джемини",
            "Леста",
            "Вран"
    };

    private static final String[] CONGRATULATIONS = {
            "Бро, я заново понимаю, НАСКОЛЬКО ты важен для меня в этой разлуке. Ты бесконечно важен.. Ты мне снишься, я рисую, думая о тебе и понимаю, что без тебя из меня будто кусок вырвали. Я это к чему, мазафакащитгаддемн, С ДНЕМ РОЖДЕНИЯ, НЕГОДНИК. Кстати, ты чертовски хорош всегда. Бичез.",
            "Мау. С ДНЕМ РОЖДЕНИЯ ХАПИ ХАПИ!!!! Будь здоров, будь счастлив, будь хорошиком. Ты потрясающий человек, с которым очень интересно общаться, а также замечательный друг!!! Желаю чтобы следующий год был еще лучше! Продолжай творить, вдохновлять и делать то, что делает тебя счастливым!!! Я верю что чего бы ты ни захотел, ты обязательно всего добьешься! И забей на всяких долбоебов, ведь плевать что говорят крысы за спиной у кисы. В конце концов это тебя официальный аккаунт репостнул. !BIRTHDAY FOR THE BIRTHDAY GOD!",
            "Поздравляю тебя с днём рождения, Кир! Чтобы каждый твой день освещался ярким и тёплым солнцем в лучах которого будут сбываться все твои мечты, желаю тебе больше радости в жизни и больших успехов в арт сфере. Я люто горжусь тобой и твоими работами!! Ты большой умничка. Побольше радуй самого себя, ведь ты достоин любого счастья!",
            "Congratulation on your anniversary, but don't get too old nor get new secrets... I am worried.",
            "Чтож, с чего бы начать? Пожалуй, во-первых, с днем рождения! Во-вторых, очень рада знакомству с тобой. Ты удивительный во всех отношениях человек, и дружба с тобой никогда не перестает удивлять. Так сказать во всех смыслах) Самым интересным образом, в тебе умудряется сочетаться чуткость, понимание, умение поддерживать и сопереживать близким, и умение доводить этих близких до желания мочь общаться через экран не только словами ;) С другой стороны хочется упомянуть также,  твои более \"профессиональные\" качества — упорство, стремление к развитию своих навыков, начитанность и то, насколько ты бережно подходишь к концепту своего творчества. Я искренне восхищаюсь тобой как \"творцом\". Детальность, продуманность твоих работ, то насколько скрупулёзно ты к ним подходишь не могут не вызывать чистого, искреннего восторга. Искренне надеюсь, что этот день ты проведешь замечательно, в компании дорогих и близких тебе людей! И пусть все последующие дни  будут такими же: легкими, приятными, и удивляющими (но только в хороших смыслах!)",
            "с днём рождения!! <33 я ещё чуть-чуть повожу вас за нос, потому что МОГУ себе позволить. в архиве Амона есть «личное дело» — к нему подходит пароль номер 1. там я расписала побольше всякого, но а пока: знайте, что я вами очень-очень сильно дорожу, восхищаюсь и бесконечно желаю вам счастья! БЕЗУМНЕЙШЕ каждый день радуюсь, что вы есть в моей жизни и что мы дружим. вы восхитительный человек — в очень и очень многих аспектах. пожалуйста, будьте счастливы и верьте в себя и свою ценность (в особенности для других людей!). может возникнуть закономерный вопрос: «вран, а зачем было так выебываться и создавать вхол эсс АМОНА, чтобы это сказать или же доказать?», но знаете ЧТО.           SOLVE MY FUCKING RIDDLES!!!! (звук с щенком пибблом wash my bellay) надеюсь, вам нравится этот подарочек <3 пусть ваш день рождения пройдёт замечательно! в конце будут части аж двух паролей. это тоже мой подарок. или это подарок от Амона? кто знает! с днём рождения!! йееей!!! [GLITCH:5\\3\\URE и 4\\3\\1R]"
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
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("frames/birthday.png");

            if(inputStream != null) {
                backgroundImage = ImageIO.read(inputStream);
            } else {
                File file = new File("frames/birthday.png");
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

        int buttonX      = (OUR_WIDTH - BUTTON_WIDTH) / 2;
        int firstButtonY = TEXT_PADDING_TOP + 35;

        for (int i = 0; i < PEOPLE.length; i++) {
            buttonBounds[i] = new Rectangle(buttonX, firstButtonY + i * (BUTTON_HEIGHT + BUTTON_GAP), BUTTON_WIDTH,
                    BUTTON_HEIGHT);
        }

        add(new DialogPanel());

        positionDialog();
    }

    private void positionDialog() {
        Point amonLocation = charlesWindow.getLocation();

        int x = amonLocation.x - getWidth() + 20;
        int y = amonLocation.y - 50;

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

            g2d.setColor(buttonColor.darker());
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 10, 10);

            Font buttonFont = new Font(FONT_NAME, Font.BOLD, 14);
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
