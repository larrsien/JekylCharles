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

public abstract class CharacterWindow extends JWindow {

    private static final int OUR_WIDTH=318;
    private static final int OUR_HEIGHT=350;

    private static final int DRAG_THRESHOLD = 5;

    private final double FLOAT_SPEED = 0.05; // Скорость плавания (меньше = медленнее)
    private final int FLOAT_AMPLITUDE = 10;
    private Timer floatingTimer;
    private double floatingOffset = 0;
    private int baseY; // Базовая Y позиция
    private final boolean floatingEnabled = true;

    public final CharacterId characterId;

    protected final SpriteManager spriteManager;
    protected final DialogueQueue dialogueQueue;

    protected CharacterState currentState = CharacterState.IDLE;
    protected int currentFrame = 0;

    protected Timer idleTimer;
    protected Timer idleToSleepTimer;

    private Point   pressPoint;
    private Point dragOffset;
    private boolean isDragging = false;

    protected ReactionBubble reactionBubble;

    protected boolean isConfirmationOpen = false;
    protected boolean isTrickShowing     = false;

    protected JPanel spritePanel;

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
    }

    protected abstract JPanel buildSpritePanel();

    protected abstract void handleRightClick(MouseEvent e);

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

    public void setState(CharacterState state) {
        if (currentState == state) return;
        currentState = state;
        currentFrame = 0;
        if (spritePanel != null) spritePanel.repaint();
    }

    public CharacterState getCurrentState() { return currentState; }

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
        idleTimer = new Timer(100, e -> {
            if (currentState == CharacterState.IDLE || currentState == CharacterState.CURIOUS) {
                currentFrame = (currentFrame + 1) % spriteManager.getFrameCount(currentState);
                if (spritePanel != null) spritePanel.repaint();
            }
        });
        idleTimer.start();
        idleToSleepTimer = new Timer(5 * 60 * 1000, e -> {
            if (currentState == CharacterState.IDLE) setState(CharacterState.SLEEPING);
        });
        idleToSleepTimer.setRepeats(false);
        idleToSleepTimer.start();
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
                    setLocation(loc.x + e.getX() - dragOffset.x, loc.y + e.getY() - dragOffset.y);
                }
            }
        };

        spritePanel.addMouseListener(ma);
        spritePanel.addMouseMotionListener(mma);
    }

    // левый клик у двоих вызывает реакт
    protected void handleLeftClick(MouseEvent e) {
        if (isConfirmationOpen || isTrickShowing) return;

        if (currentState == CharacterState.SLEEPING) {
            setState(CharacterState.IDLE);
            resetIdleTimers();
            return;
        }
        onLeftClick();
    }

    protected void onLeftClick() { }

    protected void resetIdleTimers() {
        if (idleToSleepTimer != null) {
            idleToSleepTimer.stop();
            idleToSleepTimer.start();
        }
        if (idleTimer != null) {
            idleTimer.stop();
            idleTimer.start();
        }
    }

    public boolean isTrickShowing() {
        return isTrickShowing;
    }
    public void setTrickShowing(boolean v) {
        isTrickShowing = v;
    }
    public boolean isConfirmationOpen() {
        return isConfirmationOpen;
    }

    public void cleanup() {
        if (floatingTimer != null) floatingTimer.stop();
        if (idleTimer != null) idleTimer.stop();
        if (idleToSleepTimer != null) idleToSleepTimer.stop();
        clearBubble();
        dispose();
    }

    protected BufferedImage currentSprite() {
        return spriteManager.getFrame(currentState, currentFrame);
    }

}
