package lars.com.UI;

import lars.com.dialogue.DialogueQueue;
import lars.com.graphic.SpriteManager;
import lars.com.model.CharacterId;
import lars.com.model.CharacterState;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.prefs.Preferences;

public class CharlesWindow extends CharacterWindow {

    private static final Preferences prefs = Preferences.userNodeForPackage(CharlesWindow.class);
    private static final String SURPRISE_REVEALED = "surpriseRevealed";
    private static final String GLITCH_ENABLED = "glitchEnabled";

    private ContextMenu contextMenu;
    private ConfirmationWindow confirmationWindow;
    private BirthdayDialog birthdayDialog;
    private SettingsWindow settingsWindow;
    private TimerWidget timerWidget;

    private boolean isContextMenuOpen = false;
    private boolean isConfirmationOpen = false;
    private boolean isBirthdayOpen = false;
    private boolean isTrickShowing = false;
    private boolean isSettingsOpen = false;

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
                    g.drawImage(sprite, 0, 0, getWidth(), getHeight(), null);
                }
            }
        };
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(OUR_WIDTH, OUR_HEIGHT));
        return panel;
    }

    @Override
    protected void handleRightClick(MouseEvent e) {
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

    @Override
    protected void onLeftClick() {
        if (isContextMenuOpen) {
            closeContextMenu();
            return;
        }
        resetIdleTimers();
    }

    private void closeContextMenu() {
        if (contextMenu != null) { contextMenu.dispose(); contextMenu = null; }
        isContextMenuOpen = false;
    }

    public void onContextMenuClosed() {
        contextMenu = null;
        isContextMenuOpen = false;
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

    private void showBirthdayMenuDialog() {
        boolean revealed = prefs.getBoolean(SURPRISE_REVEALED, false);
        if (!revealed) {
            setState(CharacterState.CURIOUS);
            dialogueQueue.add(new DialogueLine(CharacterId.CHARLES,
                    "О? Неужто Вас заинтересовали эти знаки вопроса? " +
                            "Надо же, какое трогательное любопытство!"));
            prefs.putBoolean(SURPRISE_REVEALED, true);
        }
        isBirthdayOpen = true;
        birthdayDialog = new BirthdayDialog(this);
        birthdayDialog.setVisible(true);
    }

    private void closeBirthday() {
        if (birthdayDialog != null) { birthdayDialog.dispose(); birthdayDialog = null; }
        isBirthdayOpen = false;
    }

    public void showConfirmationDialog() {
        clearBubble();
        if (confirmationWindow != null) confirmationWindow.dispose();
        isConfirmationOpen = true;
        confirmationWindow = new ConfirmationWindow(
                "Желаете со мной попрощаться?",
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
        if (settingsWindow != null) { settingsWindow.dispose(); settingsWindow = null; }
        isSettingsOpen = false;
    }

    private void shutDownApplication() {
        cleanup();
        System.exit(0);
    }

    public boolean isGlitchEnabled() {
        return prefs.getBoolean(GLITCH_ENABLED, true);
    }

    public void setGlitchEnabled(boolean v) {
        prefs.putBoolean(GLITCH_ENABLED, v);
    }


    public void reactToEvent(String message) {
        dialogueQueue.add(new DialogueLine(CharacterId.CHARLES, message));
    }

}
