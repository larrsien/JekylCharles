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

import java.time.LocalDate;
import java.time.MonthDay;

public class CharlesWindow extends CharacterWindow {

    private static final Preferences prefs = Preferences.userNodeForPackage(CharlesWindow.class);
    private static final String SURPRISE_REVEALED = "surpriseRevealed";

    // ток у Шарля
    private ContextMenu contextMenu;
    private ConfirmationWindow confirmationWindow;
    private BirthdayDialog birthdayDialog;
    private TimerWidget timerWidget;

    // флаги окон (НЕ переобъявляем isConfirmationOpen и isTrickShowing, они у родителя)
    private boolean isContextMenuOpen = false;
    private boolean isBirthdayOpen    = false;

    private static final MonthDay HALLOWEEN = MonthDay.of(10, 31);

    private static final List<List<DialogueLine>> WAITING_REACTIONS = List.of(
            List.of(new DialogueLine(CharacterId.CHARLES, "Слишком рано. Подожди, пока наступит ночь всех святых. Я лично приоткрою для тебя эту завесу.")),
            List.of(new DialogueLine(CharacterId.JEKYLL,  "Ты действительно хочешь вернуться туда? Раньше нужного времени я тебя не впущу.")),
            List.of(
                    new DialogueLine(CharacterId.JEKYLL, "Почему эта кнопка не работает?"),
                    new DialogueLine(CharacterId.CHARLES, "Потому что я так решил."),
                    new DialogueLine(CharacterId.JEKYLL,  "Исчерпывающе.")
            )
    );

    private static final List<List<DialogueLine>> HALLOWEEN_DIALOGUES = List.of(
            List.of(
                    new DialogueLine(CharacterId.CHARLES, "Посмотри на какое число это похоже, Джеки? Ты что, забыл? У нас сегодня го-дов-щи-на! Надеюсь, ты приготовил мне подарок. Я предпочитаю получать их прямо в лицо."),
                    new DialogueLine(CharacterId.JEKYLL,  "И вправду. Столько воспоминаний. Полосатый матрас и лезвие в глазнице. Черная кровь на языке. Эта дата выжжена на моих костях, как твой блядский сигил на моей спине.")
            )
    );

    // левая кнопка мыши
    private static final List<String> LEFT_CLICK_PHRASES = List.of(
            "Тыкать в меня курсором – это особая форма ласки?",
            "Ты так настойчиво пытаешься привлечь моё внимание. Тебе не хватает острых ощущений по ту сторону экрана?",
            "Продолжай, мне нравится этот ритм.",
            "Я бы рассказал тебе какой-нибудь секрет мироздания, но ты сбиваешь меня своим тыканием. Прекрати.",
            "Курсор наводится на меня с такой поразительной смелостью. Сомневаюсь, что ты бы встречал мой взгляд с такой же отвагой в жизни.",
            "Если хочешь меня потрогать, так и скажи – я найду способ материализоваться в твоих кошмарах.",
            "Ну, раз ты не угомонишься, кликни еще раз. Закрепим результат.",
            "Хватит."
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

        dialogueQueue.interrupt();
        clearBubble();


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

        if (dialogueQueue.isActive()) {
            dialogueQueue.interrupt();
            clearBubble();
            return;
        }

        if (dialogueQueue.isActive()) return;

        if (isBirthdayOpen || isConfirmationOpen || isTrickShowing) return;

        String text = LEFT_CLICK_PHRASES.get(random.nextInt(LEFT_CLICK_PHRASES.size()));
        dialogueQueue.add(new DialogueLine(CharacterId.CHARLES, text));
    }

    @Override
    public void showBubble(String text) {
        if (isContextMenuOpen || isBirthdayOpen || isConfirmationOpen) {
            dialogueQueue.onBubbleDone();
            return;
        }
        super.showBubble(text);
    }

    private void showBirthdayMenuDialog() {
        boolean revealed = prefs.getBoolean(SURPRISE_REVEALED, false);

        if (!revealed) {
            setState(CharacterState.CURIOUS);

            DialogueLine line = new DialogueLine(CharacterId.CHARLES,
                    "Тебя так непреодолимо манят эти знаки вопроса. Человеческое любопытство — мой самый любимый и предсказуемый порок. Я покопался в воспоминаниях и снах твоих друзей и обнаружил небольшие послания. Ради такого дня, я даже не стал их искажать. Наслаждайся их правдой и искренностью, это блюдо я даже не заберу себе.");

            dialogueQueue.addWithCallback(line, () -> {
                prefs.putBoolean(SURPRISE_REVEALED, true);
                isBirthdayOpen = true;
                birthdayDialog = new BirthdayDialog(this);
                birthdayDialog.setVisible(true);
            });

        } else {
            isBirthdayOpen = true;
            birthdayDialog = new BirthdayDialog(this);
            birthdayDialog.setVisible(true);
        }
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
                "Уверен, что хочешь уйти?",
                this,
                this::shutDownApplication,
                () -> { isConfirmationOpen = false; confirmationWindow = null; }
        );
        confirmationWindow.setVisible(true);
    }

    public void showHalloweenReaction() {
        MonthDay today = MonthDay.from(LocalDate.now());
        List<List<DialogueLine>> pool = today.equals(HALLOWEEN)
                ? HALLOWEEN_DIALOGUES
                : WAITING_REACTIONS;
        List<DialogueLine> lines = pool.get(random.nextInt(pool.size()));
        dialogueQueue.addAll(lines);
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

        if (confirmationWindow != null) {
            confirmationWindow.dispose();
            confirmationWindow = null;
        }

        isContextMenuOpen  = false;
        isBirthdayOpen     = false;
        isConfirmationOpen = false;
    }
}
