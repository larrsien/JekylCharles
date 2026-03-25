package lars.com.UI;

import lars.com.dialogue.DialogueQueue;
import lars.com.graphic.SpriteManager;
import lars.com.model.CharacterId;
import lars.com.model.CharacterState;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.util.function.BiConsumer;

public abstract class CharacterWindow extends JWindow {

    // ─── Размеры (protected — доступны наследникам) ─────────────────────────
    protected static final int WINDOW_WIDTH  = 318;
    protected static final int WINDOW_HEIGHT = 350;

    private static final int DRAG_THRESHOLD = 5;

    // ─── Плавание ───────────────────────────────────────────────────────────
    private final double FLOAT_SPEED     = 0.05;
    private final int    FLOAT_AMPLITUDE = 10;
    private Timer   floatingTimer;
    private double  floatingOffset = 0;
    private int     baseY;

    // ─── Идентификация ──────────────────────────────────────────────────────
    public final CharacterId characterId;

    // ─── Спрайты и диалоги ──────────────────────────────────────────────────
    protected final SpriteManager  spriteManager;
    protected final DialogueQueue  dialogueQueue;

    // ─── Состояние ──────────────────────────────────────────────────────────
    protected CharacterState currentState = CharacterState.IDLE;
    protected int            currentFrame = 0;

    // ─── Таймеры idle ───────────────────────────────────────────────────────
    protected Timer idleTimer;
    protected Timer idleToSleepTimer;

    // ─── Перетаскивание ─────────────────────────────────────────────────────
    private Point   pressPoint;
    private Point   dragOffset;
    private boolean isDragging = false;

    // ─── UI-элементы ────────────────────────────────────────────────────────
    protected ReactionBubble reactionBubble;
    protected boolean isConfirmationOpen = false;
    protected boolean isTrickShowing     = false;
    protected JPanel  spritePanel;

    // ─── Коллбэк синхронизации состояний ────────────────────────────────────
    //     BiConsumer<CharacterId, CharacterState>: кто + какое новое состояние
    private BiConsumer<CharacterId, CharacterState> onStateChange;

    // ════════════════════════════════════════════════════════════════════════
    //  Конструктор
    // ════════════════════════════════════════════════════════════════════════

    public CharacterWindow(CharacterId characterId,
                           SpriteManager spriteManager,
                           DialogueQueue dialogueQueue) {
        this.characterId   = characterId;
        this.spriteManager = spriteManager;
        this.dialogueQueue = dialogueQueue;

        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setAlwaysOnTop(true);
        setBackground(new Color(0, 0, 0, 0));

        spritePanel = buildSpritePanel();
        add(spritePanel);

        setupMouse();
        setupIdleTimers();
        setupFloatAnimation();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Абстрактные методы
    // ════════════════════════════════════════════════════════════════════════

    protected abstract JPanel buildSpritePanel();
    protected abstract void  handleRightClick(MouseEvent e);

    // ════════════════════════════════════════════════════════════════════════
    //  Синхронизация состояний
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Устанавливается PetController-ом после создания обоих окон.
     * Коллбэк вызывается при КАЖДОМ изменении состояния.
     */
    public void setOnStateChange(BiConsumer<CharacterId, CharacterState> callback) {
        this.onStateChange = callback;
    }

    /**
     * Изменить состояние И уведомить PetController (для синхронизации).
     */
    public void setState(CharacterState state) {
        if (currentState == state) return;
        currentState = state;
        currentFrame = 0;
        if (spritePanel != null) spritePanel.repaint();

        // Уведомляем контроллер
        if (onStateChange != null) {
            onStateChange.accept(characterId, state);
        }
    }

    /**
     * Изменить состояние БЕЗ уведомления (вызывается контроллером,
     * чтобы избежать бесконечной рекурсии при синхронизации).
     */
    public void setStateSilent(CharacterState state) {
        if (currentState == state) return;
        currentState = state;
        currentFrame = 0;
        if (spritePanel != null) spritePanel.repaint();
    }

    public CharacterState getCurrentState() {
        return currentState;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Баблы
    // ════════════════════════════════════════════════════════════════════════

    public void showBubble(String text) {
        clearBubble();
        reactionBubble = new ReactionBubble(text, this, () -> dialogueQueue.onBubbleDone());
        reactionBubble.setVisible(true);
    }

    public void clearBubble() {
        if (reactionBubble != null) {
            reactionBubble.dispose();
            reactionBubble = null;
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Плавающая анимация
    // ════════════════════════════════════════════════════════════════════════

    private void setupFloatAnimation() {
        floatingTimer = new Timer(16, e -> {
            floatingOffset += FLOAT_SPEED;
            int dy = (int) (Math.sin(floatingOffset) * FLOAT_AMPLITUDE);
            Point loc = getLocation();
            setLocation(loc.x, baseY + dy);
        });
        floatingTimer.start();
    }

    @Override
    public void setLocation(int x, int y) {
        super.setLocation(x, y);
    }

    public void placeAt(int x, int y) {
        baseY = y;
        setLocation(x, y);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Idle / Sleep таймеры
    // ════════════════════════════════════════════════════════════════════════

    private void setupIdleTimers() {
        int frameDelay = spriteManager.getFrameDelay(CharacterState.IDLE);
        idleTimer = new Timer(frameDelay, e -> {
            if (currentState == CharacterState.IDLE || currentState == CharacterState.CURIOUS) {
                currentFrame = (currentFrame + 1) % Math.max(1,
                        spriteManager.getFrameCount(currentState));
                if (spritePanel != null) spritePanel.repaint();
            }
        });
        idleTimer.start();

        idleToSleepTimer = new Timer(5 * 60 * 1000, e -> {
            if (currentState == CharacterState.IDLE) {
                setState(CharacterState.SLEEPING);   // уведомит контроллер
            }
        });
        idleToSleepTimer.setRepeats(false);
        idleToSleepTimer.start();
    }

    public void resetIdleTimers() {
        if (idleToSleepTimer != null) {
            idleToSleepTimer.stop();
            idleToSleepTimer.start();
        }
        if (idleTimer != null) {
            idleTimer.stop();
            idleTimer.start();
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Мышь
    // ════════════════════════════════════════════════════════════════════════

    private void setupMouse() {
        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                pressPoint = e.getPoint();
                dragOffset = e.getPoint();
                isDragging = false;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (isDragging) {
                    isDragging = false;
                    baseY = getLocation().y;
                    return;
                }
                if (SwingUtilities.isRightMouseButton(e)) {
                    handleRightClick(e);
                } else {
                    handleLeftClick(e);
                }
            }
        };

        MouseMotionAdapter mma = new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (pressPoint == null) return;
                int dx = e.getX() - pressPoint.x;
                int dy = e.getY() - pressPoint.y;
                if (!isDragging && Math.abs(dx) + Math.abs(dy) > DRAG_THRESHOLD) {
                    isDragging = true;
                }
                if (isDragging) {
                    Point loc = getLocation();
                    setLocation(loc.x + e.getX() - dragOffset.x,
                            loc.y + e.getY() - dragOffset.y);
                }
            }
        };

        spritePanel.addMouseListener(ma);
        spritePanel.addMouseMotionListener(mma);
    }

    protected void handleLeftClick(MouseEvent e) {
        if (isConfirmationOpen || isTrickShowing) return;

        if (currentState == CharacterState.SLEEPING) {
            setState(CharacterState.IDLE);
            resetIdleTimers();
            return;
        }
        onLeftClick();
    }

    /** Переопределяется в наследниках для кастомной реакции на ЛКМ. */
    protected void onLeftClick() { }

    // ════════════════════════════════════════════════════════════════════════
    //  Утилиты
    // ════════════════════════════════════════════════════════════════════════

    public boolean isTrickShowing()        { return isTrickShowing; }
    public void    setTrickShowing(boolean v) { isTrickShowing = v; }
    public boolean isConfirmationOpen()    { return isConfirmationOpen; }

    protected BufferedImage currentSprite() {
        return spriteManager.getFrame(currentState, currentFrame);
    }

    public void cleanup() {
        if (floatingTimer != null)     floatingTimer.stop();
        if (idleTimer != null)         idleTimer.stop();
        if (idleToSleepTimer != null)  idleToSleepTimer.stop();
        clearBubble();
        dispose();
    }
}
