package lars.com.UI;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.InputStream;
import java.util.Random;

public class TimerWidget extends JDialog {
    private static final int SIZE = 190;
    private static final Color BG_COLOR = new Color(28, 28, 30);
    private static final Color SURFACE_COLOR = new Color(44, 44, 46);
    private static final Color ACCENT_COLOR = new Color(255, 255, 255);
    private static final Color MUTED_COLOR = new Color(140, 140, 140);
    private static final Color BUTTON_COLOR = new Color(60, 60, 62);

    private int focusMinutes = 20;
    private int breakMinutes = 5;
    private boolean isRunning = false;
    private boolean isFocusPhase = true;
    private int secondsRemaining = 0;

    private Timer countdownTimer;
    private TimerPanel timerPanel;
    private final CharlesWindow charlesWindow;

    private static final double TRICK_CHANCE = 0.1;
    private final Random random = new Random();

    // кнопки стрелок
    private final Rectangle focusUpBounds   = new Rectangle(33,  78, 30, 18);
    private final Rectangle focusDownBounds = new Rectangle(33, 134, 30, 18);
    private final Rectangle breakUpBounds   = new Rectangle(127, 78, 30, 18);
    private final Rectangle breakDownBounds = new Rectangle(127, 134, 30, 18);

    // кнопки плэй и закрыть
    private final Rectangle playBounds = new Rectangle(80, 152, 30, 30);
    private final Rectangle closeBounds = new Rectangle(160, 8, 22, 22);

    private JTextField focusField;
    private JTextField breakField;

    private boolean isTrickShowing = false;


    public TimerWidget(CharlesWindow charlesWindow) {
        super((Frame) null, false);
        this.charlesWindow = charlesWindow;
        setUndecorated(true);
        initializeWindow();
    }

    private void initializeWindow() {
        setSize(SIZE, SIZE);
        setAlwaysOnTop(true);
        setBackground(new Color(0, 0, 0, 0));
        setFocusableWindowState(true);

        timerPanel = new TimerPanel();
        add(timerPanel);

        positionWidget();
        initializeFields();
        setupMouse();
    }

    private void positionWidget() {
        Point p = charlesWindow.getLocation();
        setLocation(p.x - SIZE + 20, p.y - 60);
    }

    private void playSound() {
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("sounds/timer.wav");

            if (is == null) {
                File file = new File("src/main/resources/sounds/timer.wav");
                if (!file.exists()) {
                    System.out.println("Звук не найден");
                    return;
                }
                is = new java.io.FileInputStream(file);
            }

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(
                    new java.io.BufferedInputStream(is));
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();

            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });

        } catch (Exception e) {
            System.out.println("Ошибка воспроизведения звука: " + e.getMessage());
        }
    }

    private void initializeFields() {
        timerPanel.setLayout(null);

        focusField = createTimeField(focusMinutes);
        focusField.setBounds(28, 98, 40, 34);
        timerPanel.add(focusField);

        breakField = createTimeField(breakMinutes);
        breakField.setBounds(122, 98, 40, 34);
        timerPanel.add(breakField);
    }

    private JTextField createTimeField(int initialValue) {
        JTextField field = new JTextField(String.format("%02d", initialValue));
        field.setHorizontalAlignment(JTextField.CENTER);
        field.setFont(new Font("Times New Roman", Font.BOLD, 15));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setOpaque(true);
        field.setBackground(SURFACE_COLOR);

        // скруглённая рамка под стиль виджета
        field.setBorder(new AbstractBorder() {
            private static final int ARC = 8;

            @Override
            public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(70, 70, 72));
                g2.drawRoundRect(x, y, w - 1, h - 1, ARC, ARC);
                g2.dispose();
            }

            @Override
            public Insets getBorderInsets(Component c) {
                return new Insets(2, 4, 2, 4);
            }
        });

        // при получении фокуса — выделить всё для удобной замены
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.selectAll();
            }

            @Override
            public void focusLost(FocusEvent e) {
                applyFieldValue(field);
            }
        });

        field.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                field.selectAll();
            }
        });

        // Enter — применить и снять фокус
        field.addActionListener(e -> {
            applyFieldValue(field);
            timerPanel.requestFocusInWindow();
        });

        ((AbstractDocument) field.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void replace(FilterBypass fb, int offset, int length,
                                String text, AttributeSet attrs) throws BadLocationException {
                // текст после замены
                String current = fb.getDocument().getText(0, fb.getDocument().getLength());
                String result = current.substring(0, offset) + text + current.substring(offset + length);
                // разрешаем только если итог — не больше 2 цифр
                if (result.matches("\\d{0,2}")) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        });

        return field;
    }

    private void applyFieldValue(JTextField field) {
        try {
            int value = Integer.parseInt(field.getText().trim());
            value = Math.max(1, Math.min(99, value));

            if (field == focusField) {
                focusMinutes = value;
            } else {
                breakMinutes = value;
            }

            field.setText(String.format("%02d", value));
            timerPanel.repaint();

        } catch (NumberFormatException e) {
            if (field == focusField) {
                field.setText(String.format("%02d", focusMinutes));
            } else {
                field.setText(String.format("%02d", breakMinutes));
            }
        }
    }

    private void setupMouse() {
        timerPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (isTrickShowing) return;

                Point p = e.getPoint();

                if (closeBounds.contains(p)) {
                    stopTimer();
                    dispose();
                    charlesWindow.onTimerWidgetClosed();
                    return;
                }

                if (!isRunning) {
                    if (focusUpBounds.contains(p)) {
                        focusMinutes = Math.min(99, focusMinutes + 1);
                        focusField.setText(String.format("%02d", focusMinutes));
                        timerPanel.repaint();
                    }
                    if (focusDownBounds.contains(p)) {
                        focusMinutes = Math.max(1, focusMinutes - 1);
                        focusField.setText(String.format("%02d", focusMinutes));
                        timerPanel.repaint();
                    }
                    if (breakUpBounds.contains(p)) {
                        breakMinutes = Math.min(99, breakMinutes + 1);
                        breakField.setText(String.format("%02d", breakMinutes));
                        timerPanel.repaint();
                    }
                    if (breakDownBounds.contains(p)) {
                        breakMinutes = Math.max(1, breakMinutes - 1);
                        breakField.setText(String.format("%02d", breakMinutes));
                        timerPanel.repaint();
                    }
                }

                if (playBounds.contains(p)) {
                    if (isRunning) {
                        stopTimer();
                    } else {
                        startFakeTimer();
                    }
                    timerPanel.repaint();
                }
            }
        });

        // перетаскивание
        final Point[] drag = {null};
        timerPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (isTrickShowing) return;

                Point p = e.getPoint();

                if (focusField.getBounds().contains(p) ||
                        breakField.getBounds().contains(p)) {
                    drag[0] = null;
                    return;
                }

                if (!playBounds.contains(p) &&
                        !focusUpBounds.contains(p) &&
                        !focusDownBounds.contains(p) &&
                        !breakUpBounds.contains(p) &&
                        !breakDownBounds.contains(p) &&
                        !closeBounds.contains(p)) {
                    drag[0] = p;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                drag[0] = null;
            }
        });

        timerPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (isTrickShowing) return;

                if (drag[0] != null) {
                    Point loc = getLocation();
                    setLocation(loc.x + e.getX() - drag[0].x,
                            loc.y + e.getY() - drag[0].y);
                }
            }
        });
    }

    private void startFakeTimer() {
        applyFieldValue(focusField);
        applyFieldValue(breakField);

        if (random.nextDouble() < TRICK_CHANCE) {
            showTrickDialog();
            return;
        }
        startTimer();
    }

    private void showTrickDialog() {
        isTrickShowing = true;
        charlesWindow.setTrickShowing(true);
        TrickDialog trick = new TrickDialog(charlesWindow, () -> {
            isTrickShowing = false;
            charlesWindow.setTrickShowing(false);
            applyTrickOutcome();
        });
        trick.setVisible(true);
    }

    private void safeReact(String message) {
        if (!isTrickShowing) {
            charlesWindow.reactToEvent(message);
        }
    }

    private void applyTrickOutcome() {
        // рандом эсс число от 3 до 15
        int minutes = 3 + random.nextInt(13);
        int outcome = random.nextInt(3);

        switch (outcome) {
            case 0:
                safeReact("Ой, все исчезло. Как и твои амбиции на сегодня?");
                break;

            case 1:
                // прибавить или отнять от фокуса
                if (random.nextBoolean()) {
                    focusMinutes = Math.min(99, focusMinutes + minutes);
                    safeReact("Выжми из себя все соки, пока я смотрю.");
                } else {
                    // отнимаем, но не меньше 1 минуты
                    if (focusMinutes - minutes >= 1) {
                        focusMinutes -= minutes;
                        safeReact("Как быстро тает твой энтузиазм. Надеюсь, ты так спешишь, чтобы уделить мне как можно больше внимания.");
                    } else {
                        // нечего отнимать и тогда минимум
                        focusMinutes = 1;
                        safeReact("Целых шестьдесят секунд, чтобы доказать свою полезность…");
                    }
                }
                focusField.setText(String.format("%02d", focusMinutes));
                timerPanel.repaint();
                startTimer();
                break;

            case 2:
                // прибавить или отнять от перерыва
                if (random.nextBoolean()) {
                    breakMinutes = Math.min(99, breakMinutes + minutes);
                    charlesWindow.reactToEvent("Иллюзия свободы становится все длиннее.");
                } else {
                    if (breakMinutes - minutes >= 1) {
                        breakMinutes -= minutes;
                        charlesWindow.reactToEvent("Я позволил себе забрать пару твоих свободных минут. Мне они нужнее. А ты... возвращайся к работе. Твой отдых теперь мой.");
                    } else {
                        breakMinutes = 1;
                        charlesWindow.reactToEvent("Если ты себя наказываешь за что-то таким отрезком времени, то лучше расскажи мне. Я придумаю пытку изощреннее.");
                    }
                }
                breakField.setText(String.format("%02d", breakMinutes));
                timerPanel.repaint();
                startTimer();
                break;
        }
    }

    private void startTimer() {
        applyFieldValue(focusField);
        applyFieldValue(breakField);

        focusField.setVisible(false);
        breakField.setVisible(false);
        isRunning = true;
        isFocusPhase = true;
        secondsRemaining = focusMinutes * 60;

        countdownTimer = new Timer(1000, e -> {
            secondsRemaining--;
            if (secondsRemaining <= 0) switchPhase();
            timerPanel.repaint();
        });
        countdownTimer.start();

        if (random.nextDouble() >= TRICK_CHANCE) {
            charlesWindow.reactToEvent("Постарайся не разочаровать меня. Сколько в этот раз ты продержишься, прежде чем начнёшь умолять меня об перерыве?");
        }
        timerPanel.repaint();
    }

    private void stopTimer() {
        isRunning = false;
        if (countdownTimer != null) countdownTimer.stop();
        focusField.setVisible(true);
        breakField.setVisible(true);
        focusField.setText(String.format("%02d", focusMinutes));
        breakField.setText(String.format("%02d", breakMinutes));
    }

    private void switchPhase() {
        isFocusPhase = !isFocusPhase;
        if (isFocusPhase) {
            secondsRemaining = focusMinutes * 60;
            playSound();
            charlesWindow.reactToEvent("Время вышло. Ты был хорошим мальчиком. Теперь ты можешь снова обратить внимание на меня.");
        } else {
            secondsRemaining = breakMinutes * 60;
            playSound();
            charlesWindow.reactToEvent("Пора снова надевать ошейник рутины. Занимайся своими делами, пока я не придумал тебе новые.");
        }
    }

    public void stopAndDispose() {
        stopTimer();
        dispose();
    }

    private class TimerPanel extends JPanel {

        public TimerPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(SIZE, SIZE));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // круглый фон
            g2.setColor(BG_COLOR);
            g2.fillOval(0, 0, SIZE, SIZE);

            // кнопка закрытия
            g2.setColor(SURFACE_COLOR);
            g2.fillOval(closeBounds.x, closeBounds.y, closeBounds.width, closeBounds.height);
            g2.setColor(MUTED_COLOR);
            g2.setFont(new Font("Arial", Font.BOLD, 12));
            drawCenteredString(g2, "Х", closeBounds);

            // заголовок
            g2.setColor(ACCENT_COLOR);
            g2.setFont(new Font("Times New Roman", Font.PLAIN, 14));
            String title = isRunning ? (isFocusPhase ? "Фокус" : "Перерыв") : "Таймер (минуты)";
            g2.drawString(title, SIZE / 2 - g2.getFontMetrics().stringWidth(title) / 2, 40);

            if (isRunning) {
                drawRunningMode(g2);
            } else {
                drawSetupMode(g2);
            }

            drawPlayButton(g2);
        }

        private void drawSetupMode(Graphics2D g2) {
            // центры полей
            int focusCx = 28 + 20;   // = 48
            int breakCx = 122 + 20;  // = 142

            // подписи над полями
            g2.setColor(MUTED_COLOR);
            g2.setFont(new Font("Times New Roman", Font.PLAIN, 13));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString("Фокус",
                    focusCx - fm.stringWidth("Фокус") / 2, 70);
            g2.drawString("Перерыв",
                    breakCx - fm.stringWidth("Перерыв") / 2, 70);

            // стрелки вверх
            drawArrow(g2, focusUpBounds, true);
            drawArrow(g2, breakUpBounds, true);

            // разделитель по центру между двумя полями
            g2.setColor(MUTED_COLOR);
            g2.setFont(new Font("Times New Roman", Font.BOLD, 22));
            g2.drawString("/", (focusCx + breakCx) / 2 - 5, 122);

            // стрелки вниз
            drawArrow(g2, focusDownBounds, false);
            drawArrow(g2, breakDownBounds, false);
        }

        private void drawRunningMode(Graphics2D g2) {
            int mins = secondsRemaining / 60;
            int secs = secondsRemaining % 60;
            String timeStr = String.format("%02d:%02d", mins, secs);

            g2.setColor(ACCENT_COLOR);
            g2.setFont(new Font("Times New Roman", Font.BOLD, 42));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(timeStr, SIZE / 2 - fm.stringWidth(timeStr) / 2, 112);

            int totalSecs = (isFocusPhase ? focusMinutes : breakMinutes) * 60;
            double progress = 1.0 - (double) secondsRemaining / totalSecs;

            g2.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(SURFACE_COLOR);
            g2.drawArc(15, 15, SIZE - 30, SIZE - 30, 90, -360);

            g2.setColor(isFocusPhase ? new Color(180, 180, 190) : new Color(100, 220, 180));
            g2.drawArc(15, 15, SIZE - 30, SIZE - 30, 90, -(int) (360 * progress));
            g2.setStroke(new BasicStroke(1));
        }

        private void drawArrow(Graphics2D g2, Rectangle bounds, boolean up) {
            int cx = bounds.x + bounds.width / 2;
            int cy = bounds.y + bounds.height / 2;
            int[] xp = {cx - 6, cx + 6, cx};
            int[] yp = up ? new int[]{cy + 4, cy + 4, cy - 4} : new int[]{cy - 4, cy - 4, cy + 4};

            g2.setColor(MUTED_COLOR);
            g2.fillPolygon(xp, yp, 3);
        }

        private void drawPlayButton(Graphics2D g2) {
            g2.setColor(BUTTON_COLOR);
            g2.fillOval(playBounds.x, playBounds.y, playBounds.width, playBounds.height);

            g2.setColor(ACCENT_COLOR);
            if (!isRunning) {
                // центр кнопки
                int cx = playBounds.x + playBounds.width / 2 + 1; // +1 визуальная компенсация треугольника
                int cy = playBounds.y + playBounds.height / 2;
                int[] xp = {cx - 6, cx - 6, cx + 8};
                int[] yp = {cy - 7, cy + 7, cy};
                g2.fillPolygon(xp, yp, 3);
            } else {
                int cx = playBounds.x + playBounds.width / 2;
                int cy = playBounds.y + playBounds.height / 2;
                g2.fillRect(cx - 7, cy - 6, 5, 13);
                g2.fillRect(cx + 2, cy - 6, 5, 13);
            }
        }

        private void drawCenteredString(Graphics2D g2, String text, Rectangle bounds) {
            FontMetrics fm = g2.getFontMetrics();
            int x = bounds.x + (bounds.width - fm.stringWidth(text)) / 2;
            int y = bounds.y + (bounds.height - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(text, x, y);
        }
    }
}
