package lars.com.UI;

import lars.com.dialogue.DialogueQueue;
import lars.com.graphic.SpriteManager;
import lars.com.model.CharacterId;
import lars.com.model.CharacterState;
import lars.com.model.DialogueLine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.prefs.Preferences;
import java.util.List;

public class CharlesWindow extends CharacterWindow {

    private static final Preferences prefs = Preferences.userNodeForPackage(CharlesWindow.class);
    private static final String SURPRISE_REVEALED = "surpriseRevealed";

    // ток у Шарля
    private ContextMenu contextMenu;
    private ConfirmationWindow confirmationWindow;
    private BirthdayDialog birthdayDialog;
    private SettingsWindow settingsWindow;
    private TimerWidget timerWidget;

    // флаги окон (НЕ переобъявляем isConfirmationOpen и isTrickShowing, они у родителя)
    private boolean isContextMenuOpen = false;
    private boolean isBirthdayOpen    = false;
    private boolean isSettingsOpen    = false;

    // левая кнопка мыши
    private static final List<String> LEFT_CLICK_PHRASES = List.of(
            "Хм-м?",
            "Что-то хотели?",
            "Я Вас слушаю.",
            "Не стоит трогать меня без повода."
    );
    private final Random random = new Random();

    public CharlesWindow(SpriteManager spriteManager, DialogueQueue dialogueQueue) {
        super(CharacterId.CHARLES, spriteManager, dialogueQueue);
    }

    @Override
    protected JPanel buildSpritePanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                BufferedImage sprite = currentSprite();
                if (sprite != null) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    g2d.drawImage(sprite, 0, 0, getWidth(), getHeight(), null);
                }
            }
        };

        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(OUR_WIDTH, OUR_HEIGHT));
        return panel;
    }

    @Override
    protected void handleRightClick(MouseEvent e) {
        if (isConfirmationOpen) return;

        if (currentState == CharacterState.SLEEPING) {
            setState(CharacterState.IDLE);
            resetIdleTimers();
        }

        if (isBirthdayOpen) {
            closeBirthday();
            return;
        }

        if (isSettingsOpen) {
            closeSettings();
            return;
        }

        clearBubble();
        dialogueQueue.interrupt();

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

    protected void onLeftClick() {
        if (isContextMenuOpen) {
            if (contextMenu != null) { contextMenu.dispose(); contextMenu = null; }
            isContextMenuOpen = false;
        }

        if (dialogueQueue.isActive()) return;

        if (isBirthdayOpen || isSettingsOpen || isConfirmationOpen || isTrickShowing) return;

        String text = LEFT_CLICK_PHRASES.get(random.nextInt(LEFT_CLICK_PHRASES.size()));
        dialogueQueue.add(new DialogueLine(CharacterId.CHARLES, text));
    }

    @Override
    public void showBubble(String text) {
        if (isContextMenuOpen || isBirthdayOpen || isSettingsOpen || isConfirmationOpen) {
            dialogueQueue.onBubbleDone();
            return;
        }
        super.showBubble(text);
    }

    private void showBirthdayMenuDialog() {
        boolean revealed = prefs.getBoolean(SURPRISE_REVEALED, false);

        if (!revealed) {
            setState(CharacterState.CURIOUS);
            reactToEvent("О? Неужто Вас заинтересовали эти знаки вопроса?");
        }

        isBirthdayOpen = true;
        birthdayDialog = new BirthdayDialog(this);
        birthdayDialog.setVisible(true);
    }

    private void closeBirthday() {
        if (birthdayDialog != null) {
            birthdayDialog.dispose();
            birthdayDialog = null;
        }

        isBirthdayOpen = false;
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

    public void setContextMenuOpen(boolean v) {
        isContextMenuOpen = v;
    }

    public void setBirthdayOpen(boolean v) {
        isBirthdayOpen = v;
    }

    public void showConfirmationDialog() {
        isConfirmationOpen = true;
        confirmationWindow = new ConfirmationWindow(
                "Вы уверены, что хотите уйти?",
                this,
                this::shutDownApplication,
                () -> { isConfirmationOpen = false; confirmationWindow = null; }
        );
        confirmationWindow.setVisible(true);
    }

    public void showSettingsWindow() {
        if (isSettingsOpen) { closeSettings(); return; }
        isSettingsOpen = true;
        settingsWindow = new SettingsWindow(this);
        settingsWindow.setVisible(true);
    }

    public void onSettingsWindowClosed() {
        settingsWindow = null;
        isSettingsOpen = false;
    }

    private void closeSettings() {
        if (settingsWindow != null) {
            settingsWindow.dispose();
            settingsWindow = null;
        }

        isSettingsOpen = false;
    }

    private void shutDownApplication() {
        cleanup();
        System.exit(0);
    }

    public boolean isSurpriseRevealed() {
        return prefs.getBoolean(SURPRISE_REVEALED, false);
    }

    public void setSurpriseRevealed(boolean v) {
        prefs.putBoolean(SURPRISE_REVEALED, v);
    }

    public void reactToEvent(String message) {
        dialogueQueue.add(new DialogueLine(CharacterId.CHARLES, message));
    }

    @Override
    protected void onDragStart() {
        if (contextMenu != null) {
            contextMenu.dispose();
            contextMenu = null;
        }

        if (birthdayDialog != null) {
            birthdayDialog.dispose();
            birthdayDialog = null;
        }

        if (settingsWindow != null) {
            settingsWindow.dispose();
            settingsWindow = null;
        }

        if (confirmationWindow != null) {
            confirmationWindow.dispose();
            confirmationWindow = null;
        }

        isContextMenuOpen  = false;
        isBirthdayOpen     = false;
        isSettingsOpen     = false;
        isConfirmationOpen = false;
    }
}
