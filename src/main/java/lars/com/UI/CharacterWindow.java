package lars.com.UI;

import lars.com.dialogue.DialogueQueue;
import lars.com.graphic.SpriteManager;
import lars.com.model.CharacterId;
import lars.com.model.CharacterState;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.function.BiConsumer;

public abstract class CharacterWindow extends JWindow {

    protected static final int OUR_WIDTH  = 327;
    protected static final int OUR_HEIGHT = 443;
    private static final double DRAG_SCALE = 1.15;

    private static final int DRAG_THRESHOLD = 5;

    private static final double FLOAT_SPEED = 0.05;
    private static final int FLOAT_AMPLITUDE = 10;
    private Timer floatingTimer;
    private double floatingOffset = 0;
    private int baseY;

    public final CharacterId characterId;

    protected final SpriteManager  spriteManager;
    protected final DialogueQueue  dialogueQueue;

    protected CharacterState currentState = CharacterState.IDLE;
    protected int currentFrame = 0;

    protected Timer idleTimer;
    protected Timer idleToSleepTimer;

    private Point pressPoint;
    private Point dragOffset;
    private boolean isDragging = false;

    protected ReactionBubble reactionBubble;
    protected boolean isConfirmationOpen = false;
    protected boolean isTrickShowing = false;
    protected JPanel  spritePanel;

    private final Random random = new Random();

    // используем биконсьюмен, чтобы не привязать кэрэктер виндоу жестко к джекиллу и шарлю
    private BiConsumer<CharacterId, CharacterState> onStateChange;

    public CharacterWindow(CharacterId characterId, SpriteManager spriteManager, DialogueQueue dialogueQueue) {
        this.characterId   = characterId;
        this.spriteManager = spriteManager;
        this.dialogueQueue = dialogueQueue;

        setSize(OUR_WIDTH, OUR_HEIGHT);
        setAlwaysOnTop(true);
        setBackground(new Color(0, 0, 0, 0));

        spritePanel = buildSpritePanel();
        add(spritePanel);

        setupMouse();
        setupIdleTimers();
        setupFloatAnimation();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowDeactivated(WindowEvent e) {
                SwingUtilities.invokeLater(() -> {
                    setAlwaysOnTop(false);
                    setAlwaysOnTop(true);
                });
            }
        });
    }

    protected abstract JPanel buildSpritePanel();
    protected abstract void  handleRightClick(MouseEvent e);

    // устанавлиаается петконтроллером после создания обоих окон
    // коллюэк вызывается при каждом изменении состояния
    public void setOnStateChange(BiConsumer<CharacterId, CharacterState> callback) {
        this.onStateChange = callback;
    }

    public void setState(CharacterState state) {
        if (currentState == state) return;
        currentState = state;
        currentFrame = pickRandomFrame(state);
        if (spritePanel != null) spritePanel.repaint();

        // Уведомляем контроллер
        if (onStateChange != null) {
            onStateChange.accept(characterId, state);
        }
    }

    private int pickRandomFrame(CharacterState state) {
        int count = spriteManager.getFrameCount(state);
        return count > 1 ? random.nextInt(count) : 0;
    }

    public void setStateSilent(CharacterState state) {
        if (currentState == state) return;
        currentState = state;
        currentFrame = 0;
        if (spritePanel != null) spritePanel.repaint();
    }

    public CharacterState getCurrentState() {
        return currentState;
    }


    public void showBubble(String text) {
        if (currentState == CharacterState.SLEEPING) {
            setState(CharacterState.CURIOUS);
            resetIdleTimers();
        }
        clearBubble();
        reactionBubble = new ReactionBubble(text, this, () -> {
            reactionBubble = null;
            dialogueQueue.onBubbleDone();
        });
        reactionBubble.setVisible(true);
    }

    public void clearBubble() {
        if (reactionBubble != null) {
            reactionBubble.cleanup();
            reactionBubble = null;
        }
    }

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

    private void setupIdleTimers() {
        int frameDelay = spriteManager.getFrameDelay(CharacterState.IDLE);
        idleTimer = new Timer(frameDelay, e -> {
            if (currentState == CharacterState.CURIOUS) {
                currentFrame = (currentFrame + 1) % Math.max(1,
                        spriteManager.getFrameCount(currentState));
                if (spritePanel != null) spritePanel.repaint();
            }
        });
        idleTimer.start();

        idleToSleepTimer = new Timer(10 * 60 * 1000, e -> {
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
                    Point loc = getLocation();
                    int curW = getWidth();
                    int curH = getHeight();
                    setLocation(loc.x + (curW - OUR_WIDTH) / 2,
                            loc.y + (curH - OUR_HEIGHT) / 2);
                    setSize(OUR_WIDTH, OUR_HEIGHT);
                    spritePanel.setPreferredSize(new Dimension(OUR_WIDTH, OUR_HEIGHT));

                    baseY = getLocation().y;
                    if (floatingTimer != null) floatingTimer.start();
                    setState(CharacterState.IDLE);
                    resetIdleTimers();
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
                    if (floatingTimer != null) floatingTimer.stop();
                    setState(CharacterState.DRAGGING);
                    dialogueQueue.interrupt();
                    clearBubble();
                    onDragStart();

                    int newW = (int)(OUR_WIDTH * DRAG_SCALE);
                    int newH = (int)(OUR_HEIGHT * DRAG_SCALE);
                    Point loc = getLocation();
                    setLocation(loc.x - (newW - OUR_WIDTH) / 2,
                            loc.y - (newH - OUR_HEIGHT) / 2);
                    setSize(newW, newH);
                    spritePanel.setPreferredSize(new Dimension(newW, newH));
                    dragOffset = new Point(
                            (int)(dragOffset.x * DRAG_SCALE),
                            (int)(dragOffset.y * DRAG_SCALE)
                    );
                }
                if (isDragging) {
                    int newX = getLocation().x + e.getX() - dragOffset.x;
                    int newY = getLocation().y + e.getY() - dragOffset.y;

                    Rectangle sb = GraphicsEnvironment.getLocalGraphicsEnvironment()
                            .getMaximumWindowBounds();
                    newX = Math.max(sb.x, Math.min(newX, sb.x + sb.width - getWidth()));
                    newY = Math.max(sb.y, Math.min(newY, sb.y + sb.height - getHeight()));

                    setLocation(newX, newY);
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

    protected void onDragStart() { }

    // переопределим в наследниках
    protected void onLeftClick() { }

    public boolean isTrickShowing() {
        return isTrickShowing;
    }

    public void setTrickShowing(boolean v) {
        isTrickShowing = v;
    }

    public boolean isConfirmationOpen() {
        return isConfirmationOpen;
    }

    protected BufferedImage currentSprite() {
        return spriteManager.getFrame(currentState, currentFrame);
    }

    public void cleanup() {
        if (floatingTimer != null) floatingTimer.stop();
        if (idleTimer != null) idleTimer.stop();
        if (idleToSleepTimer != null) idleToSleepTimer.stop();
        clearBubble();
        dispose();
    }
}
