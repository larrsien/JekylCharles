package lars.com.UI;

import lars.com.graphic.SpriteManager;
import lars.com.model.AmonState;
import lars.com.reactions.ResponseLibrary;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.prefs.Preferences;

public class AmonWindow extends JWindow {

    private static final int OUR_WIDTH=318;
    private static final int OUR_HEIGHT=350;

    private final SpriteManager spriteManager;
    private AmonState currentState;
    private int currentFrame;
    private final Random random;

    // Для плавающей анимации
    private Timer floatingTimer;
    private double floatingOffset = 0;
    private final double floatingSpeed = 0.05; // Скорость плавания (меньше = медленнее)
    private final int floatingAmplitude = 10; // Амплитуда в пикселях (насколько высоко/низко)
    private int baseY; // Базовая Y позиция
    private final boolean floatingEnabled = true; // Включить/выключить плавание

    // Передвижение по рабочему столу
    private Point dragOffset;
    private boolean isDragging = false;
    private Point pressedPoint;
    private static final int DRAG_THRESHOLD = 5;
    private final ResponseLibrary responseLibrary;

    // Для IDLE анимации
    private Timer idleTimer;
    private Timer idleToSleepTimer;

    // проверки для не наложения окон
    private boolean isContextMenuOpen = false;
    private boolean isConfirmationOpen = false;
    private boolean isBirthdayOpen = false;
    private boolean isTrickShowing = false;
    private boolean isSettingsOpen = false;

    private ConfirmationWindow confirmationWindow;
    private ContextMenu contextMenu;
    private BirthdayDialog birthdayDialog;
    private SettingsWindow settingsWindow;

    // Реакция
    private ReactionBubble reactionBubble;
    private Timer reactionBubbleTimer;
    private TimerWidget timerWidget;


    private AmonPanel amonPanel;

    private static final Preferences prefs = Preferences.userNodeForPackage(AmonWindow.class);
    private static final String SURPRISE_REVEALED = "surpriseRevealed";
    private static final String GLITCH_ENABLED = "glitchEnabled";

    private static final String MEAL_REMINDER_ENABLED = "mealReminderEnabled";


    public AmonWindow(SpriteManager spriteManager) {
        this.spriteManager = spriteManager;
        this.random = new Random();
        this.currentState = AmonState.IDLE;
        this.currentFrame = 0;
        this.responseLibrary = new ResponseLibrary();

        initializeWindow();
        initializeFloating();
        initializeDragging();
        initializeIdleTimers();

        spriteManager.preloadAll();
    }

    // РЕАКЦИИ

    public void reactToEvent(String reaction) {
        if (isContextMenuOpen || isBirthdayOpen || isTrickShowing || isSettingsOpen) {
            return;
        }

        setState(AmonState.CURIOUS);
        showReactionBubble(reaction);

        // через пять минут возвращаемся в состояние idle
        Timer returnToIdle = new Timer(300000, e -> setState(AmonState.IDLE));
        returnToIdle.setRepeats(false);
        returnToIdle.start();
    }

    private void showReactionBubble(String reaction) {
        showReactionBubble(reaction, null);
    }
    private void showReactionBubble(String reaction, Runnable onComplete) {
        if (reactionBubble != null) {
            return;
        }

        if (reactionBubbleTimer != null && reactionBubbleTimer.isRunning()) {
            reactionBubbleTimer.stop();
        }

        if (confirmationWindow != null) {
            confirmationWindow.dispose();
        }

        reactionBubble = new ReactionBubble(reaction, this);
        reactionBubble.setVisible(true);

        int displayTime = reactionBubble.getTotalDisplayTime();

        reactionBubbleTimer = new Timer(displayTime + 500, e -> {
            if (reactionBubble != null) {
                reactionBubble.dispose();
                reactionBubble = null;
            }

            if (onComplete != null) {
                onComplete.run();
            }
        });

        reactionBubbleTimer.setRepeats(false);
        reactionBubbleTimer.start();
    }

    private void clearReaction() {
        if (reactionBubble != null) {
            reactionBubble.dispose();
            reactionBubble = null;
        }
        if (reactionBubbleTimer != null && reactionBubbleTimer.isRunning()) {
            reactionBubbleTimer.stop();
        }
    }

    // ИНИЦИАЛИЗАТОРЫ

    private void initializeWindow() {
        setSize(OUR_WIDTH, OUR_HEIGHT);
        setAlwaysOnTop(true);
        setBackground(new Color(0, 0,0, 0));

        amonPanel = new AmonPanel();
        add(amonPanel);

        currentFrame = random.nextInt(Math.max(1, spriteManager.getFrameCount(AmonState.IDLE)));

        positionWindow();
    }

    private void initializeFloating() {

        floatingTimer = new Timer(16, e -> {
            if (!isDragging && floatingEnabled) {
                floatingOffset += floatingSpeed;

                double offset = Math.sin(floatingOffset) * floatingAmplitude;

                Point currentLocation = getLocation();
                setLocation(currentLocation.x, baseY + (int) offset);
            }
        });
        floatingTimer.start();
    }

    private void initializeDragging() {
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // правая кнопка - показываем предложение на аннигиляцию Амона
                if (SwingUtilities.isRightMouseButton(e)) {
                    showContextMenu(e.getLocationOnScreen());
                    return;
                }
                if (SwingUtilities.isLeftMouseButton(e)) {
                    Point screenPos = e.getLocationOnScreen();
                    Point winPos = getLocation();
                    dragOffset = new Point(screenPos.x - winPos.x, screenPos.y - winPos.y);
                    pressedPoint = screenPos;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (isDragging) {
                    isDragging = false;
                    // Обновление базовой позиции для того, чтобы снова летал
                    Point currentLocation = getLocation();
                    baseY = currentLocation.y;
                    floatingOffset = 0;
                    setState(AmonState.IDLE);
                } else if (pressedPoint != null) {
                    handleClick(e);
                }
                pressedPoint = null;
            }
        };

        MouseMotionAdapter motionAdapter = new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (pressedPoint != null) {
                    Point currentPoint = e.getLocationOnScreen();
                    double distance = pressedPoint.distance(currentPoint);

                    if (distance > DRAG_THRESHOLD && !isDragging) {
                        isDragging = true;
                        setState(AmonState.DRAGGING);

                        idleToSleepTimer.restart();
                        idleTimer.restart();

                        if (reactionBubble != null) {
                            reactionBubble.dispose();
                            reactionBubble = null;
                        }
                        if (reactionBubbleTimer != null && reactionBubbleTimer.isRunning()) {
                            reactionBubbleTimer.stop();
                        }
                        if (confirmationWindow != null) {
                            confirmationWindow.dispose();
                            confirmationWindow = null;
                            isConfirmationOpen = false;
                        }
                        if (contextMenu != null) {
                            contextMenu.dispose();
                            contextMenu = null;
                            isContextMenuOpen = false;
                        }
                        if (birthdayDialog != null) {
                            birthdayDialog.dispose();
                            birthdayDialog = null;
                            isBirthdayOpen = false;
                        }

                        if (settingsWindow != null) {
                            settingsWindow.dispose();
                            settingsWindow = null;
                            isSettingsOpen = false;
                        }
                    }

                     if (isDragging) {
                         Point screenPos = e.getLocationOnScreen();
                         int newX = screenPos.x - dragOffset.x;
                         int newY = screenPos.y - dragOffset.y;

                         Rectangle screenBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
                         newX = Math.max(screenBounds.x, Math.min(newX, screenBounds.x + screenBounds.width - OUR_WIDTH));
                         newY = Math.max(screenBounds.y, Math.min(newY, screenBounds.y + screenBounds.height - OUR_HEIGHT));

                         setLocation(newX, newY);
                     }
                }
            }
        };

        amonPanel.addMouseListener(mouseAdapter);
        amonPanel.addMouseMotionListener(motionAdapter);
    }

    private void initializeIdleTimers() {
        idleTimer = new Timer(7 * 60 * 1000, e -> {
            if (currentState == AmonState.IDLE) {
                checkForIdleReaction();
            }
        });
        idleTimer.setRepeats(true);
        idleTimer.start();

        idleToSleepTimer = new Timer(10 * 60 * 1000, e -> {
            if (currentState == AmonState.IDLE) {
                setState(AmonState.SLEEPING);
            }
        });
        idleToSleepTimer.setRepeats(true);
        idleToSleepTimer.start();
    }

    private void checkForIdleReaction() {
        if (random.nextInt(100) < 3) {
            String reaction = responseLibrary.getRandomResponses("idle_special");
            showReactionBubble(reaction);
        }
    }

    // ФЛАГИ
    public void setContextMenuOpen(boolean open) {
        isContextMenuOpen = open;
    }

    public void setConfirmationOpen(boolean open) {
        isConfirmationOpen = open;
    }

    public void setBirthdayOpen(boolean open) {
        isBirthdayOpen = open;
    }

    public void setTrickShowing(boolean showing) {
        isTrickShowing = showing;
    }

    public void setGlitchEnabled(boolean enabled) {
        prefs.putBoolean(GLITCH_ENABLED, enabled);
    }

    public boolean isGlitchEnabled() {
        return prefs.getBoolean(GLITCH_ENABLED, true);
    }

    public boolean isSurpriseRevealed() {
        return prefs.getBoolean(SURPRISE_REVEALED, false);
    }

    public boolean isMealReminderEnabled() {
        return prefs.getBoolean(MEAL_REMINDER_ENABLED, true);
    }
    public void setMealReminderEnabled(boolean enabled) {
        prefs.putBoolean(MEAL_REMINDER_ENABLED, enabled);
    }

    private void showContextMenu(Point screenLocation) {
        if (isConfirmationOpen || isTrickShowing) {
            return;
        }

        // Будим из сна при правом клике
        if (currentState == AmonState.SLEEPING) {
            setState(AmonState.IDLE);
            idleToSleepTimer.restart();
            idleTimer.restart();
        }

        // Закрываем BirthdayDialog по правой кнопке
        if (isBirthdayOpen) {
            if (birthdayDialog != null) {
                birthdayDialog.dispose();
                birthdayDialog = null;
            }
            isBirthdayOpen = false;
            return;
        }

        // Закрываем Settings по правой кнопке (аналогично контекстному меню)
        if (isSettingsOpen) {
            if (settingsWindow != null) {
                settingsWindow.dispose();
                settingsWindow = null;
            }
            isSettingsOpen = false;
            return;
        }

        if (reactionBubble != null) {
            reactionBubble.dispose();
            reactionBubble = null;
        }

        if (contextMenu != null) {
            contextMenu.dispose();
            contextMenu = null;
            isContextMenuOpen = false;
            return;
        }

        isContextMenuOpen = true;
        contextMenu = new ContextMenu(this, this::showBirthdayMenuDialog, this::showTimerWidget);
        contextMenu.setVisible(true);

    }

    public void showTimerWidget() {
        if (timerWidget != null) {
            timerWidget.stopAndDispose();
            timerWidget = null;
            return;
        }
        timerWidget = new TimerWidget(this);
        timerWidget.setVisible(true);
    }

    public void onTimerWidgetClosed() {
        timerWidget = null;
    }

    public void onContextMenuClosed() {
        contextMenu = null;
        isContextMenuOpen = false;
    }

    public void onSettingsWindowClosed() {
        settingsWindow = null;
        isSettingsOpen = false;
    }


    private void showBirthdayMenuDialog() {
        boolean revealed = prefs.getBoolean(SURPRISE_REVEALED, false);

        if (!revealed) {
            setState(AmonState.CURIOUS);
            showReactionBubble("О? Неужто Вас заинтересовали эти " +
                    "знаки вопроса? Надо же, какое трогательное любопытство! Так уж вышло, что я " +
                    "ненадолго позаимствовал у Ваших дорогих друзей пару поздравлений. Не переживайте, никто из них " +
                    "совершенно           не был против! Ради такого случая, не хотите ли полюбоваться?", () -> {
                prefs.putBoolean(SURPRISE_REVEALED, true);
                SwingUtilities.invokeLater(() -> {
                    isBirthdayOpen = true;
                    birthdayDialog = new BirthdayDialog(this);
                    birthdayDialog.setVisible(true);
                });
            });
        } else {
            isBirthdayOpen = true;
            birthdayDialog = new BirthdayDialog(this);
            birthdayDialog.setVisible(true);
        }

    }

    public void showConfirmationDialog() {
        clearReaction();

        if (confirmationWindow != null) {
            confirmationWindow.dispose();
        }

        confirmationWindow = new ConfirmationWindow("Желаете со мной попрощаться?",
                this, this::shutDownApplication, () -> { confirmationWindow = null; setConfirmationOpen(false);});
        isConfirmationOpen = true;
        confirmationWindow.setVisible(true);
    }

    public void showSettingsWindow() {
        if (isSettingsOpen) {
            settingsWindow.dispose();
            settingsWindow = null;
            isSettingsOpen = false;
            return;
        }
        isSettingsOpen = true;
        settingsWindow = new SettingsWindow(this);
        settingsWindow.setVisible(true);
    }

    private void shutDownApplication() {
        cleanup();
        System.exit(0);
    }

    private void handleClick(MouseEvent e) {
        if (isContextMenuOpen) {
            if (contextMenu != null) {
                contextMenu.dispose();
                contextMenu = null;
            }
            isContextMenuOpen = false;
            // дальше не возвращаемся, даём показать реакцию
        }

        if (currentState == AmonState.SLEEPING) {
            setState(AmonState.IDLE);
            idleToSleepTimer.restart();
            idleTimer.restart();
            return; // реакцию не показываем, просто просыпаемся
        }

        if (isConfirmationOpen || isBirthdayOpen || isTrickShowing || isSettingsOpen) {
            return;
        }
        String reaction = responseLibrary.getRandomResponses("left_mouse_clicked");
        showReactionBubble(reaction);
    }

    public void setState(AmonState newState) {
        if (currentState != newState) {
            currentState = newState;

            currentFrame = random.nextInt(Math.max(1, spriteManager.getFrameCount(newState)));
            amonPanel.repaint();
        }
    }

    private void positionWindow() {
        Rectangle screenBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();

        int margin = 20;

        int x = screenBounds.x + screenBounds.width - OUR_WIDTH - margin;
        int y = screenBounds.y + screenBounds.height - OUR_HEIGHT - margin;

        x = Math.max(screenBounds.x, Math.min(x, screenBounds.x + screenBounds.width - OUR_WIDTH));
        y = Math.max(screenBounds.y, Math.min(y, screenBounds.y + screenBounds.height - OUR_HEIGHT));

        setLocation(x, y);
        baseY = y; // Игрик остается базовым для анимации
    }

    // для завершения работы приложения
    public void cleanup() {
        if (floatingTimer != null) {
            floatingTimer.stop();
        }
        if (reactionBubbleTimer != null) {
            reactionBubbleTimer.stop();
        }
        if (reactionBubble != null) {
            reactionBubble.dispose();
        }
        if (idleToSleepTimer != null) {
            idleToSleepTimer.stop();
        }
        dispose();
    }

    private class AmonPanel extends JPanel {
        public AmonPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(OUR_WIDTH, OUR_HEIGHT));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC); // ← это главное
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);

            BufferedImage sprite = spriteManager.getFrame(currentState, currentFrame);
            if (sprite != null) {
                g2d.drawImage(sprite, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }

}


