package lars.com.graphic;

import lars.com.model.CharacterState;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpriteManager {

    private static final String SPRITE_BASE = "sprites/";

    private final String characterName;
    private final String filePrefix;

    private final Map<CharacterState, List<BufferedImage>> spriteCache = new HashMap<>();
    private final Map<CharacterState, Integer> frameDelays = new HashMap<>();
    private final Map<Integer, List<BufferedImage>> bugVariants = new HashMap<>();
    private final Random random = new Random();

    /**
     * @param characterName имя персонажа (напр. "jekyll", "charles").
     *                      Используется и как папка, и как префикс файлов.
     */
    public SpriteManager(String characterName) {
        this.characterName = characterName.toLowerCase();
        this.filePrefix = this.characterName + "_";
        loadDefaultFrameDelays();
    }

    // ─── Загрузка спрайтов ──────────────────────────────────────────────────

    private void loadSprites(CharacterState state) {
        // sprites/charles/idle/
        String path = SPRITE_BASE + characterName + "/" + state.getStateName() + "/";
        List<BufferedImage> frames = new ArrayList<>();

        try {
            String firstFileName = (state == CharacterState.BUG)
                    ? filePrefix + "0.gif"
                    : filePrefix + "0.png";

            InputStream inputStream = getClass().getClassLoader()
                    .getResourceAsStream(path + firstFileName);

            if (inputStream == null) {
                // Fallback: загрузка из файловой системы (dev-режим)
                File dir = new File("src/main/resources/" + path);
                if (!dir.exists()) {
                    System.out.println("[" + characterName + "] Папка спрайтов не найдена: " + path);
                    return;
                }
                loadFromFileSystem(state, dir, frames);
            } else {
                // Загрузка из classpath (из JAR)
                inputStream.close(); // закрываем проверочный стрим
                loadFromClasspath(state, path, frames);
            }

            if (frames.isEmpty() && bugVariants.isEmpty()) {
                System.out.println("[" + characterName + "] Фреймов не нашлось для " + state);
            } else if (!frames.isEmpty()) {
                spriteCache.put(state, frames);
                System.out.println("[" + characterName + "] Загружены спрайты для " + state
                        + " (" + frames.size() + " кадров)");
            }

        } catch (IOException e) {
            System.out.println("[" + characterName + "] Ошибка загрузки спрайтов для " + state
                    + ": " + e.getMessage());
        }
    }

    private void loadFromFileSystem(CharacterState state, File dir, List<BufferedImage> frames)
            throws IOException {

        if (state == CharacterState.BUG) {
            File[] gifFiles = dir.listFiles(
                    (d, name) -> name.startsWith(filePrefix) && name.endsWith(".gif"));

            if (gifFiles != null && gifFiles.length > 0) {
                sortByFrameNumber(gifFiles, filePrefix + "(\\d+)\\.gif");

                for (int i = 0; i < gifFiles.length; i++) {
                    List<BufferedImage> variantFrames = extractGifFrames(gifFiles[i]);
                    if (!variantFrames.isEmpty()) {
                        bugVariants.put(i, variantFrames);
                    }
                }
                // Первый вариант — в кеш по умолчанию
                if (!bugVariants.isEmpty()) {
                    frames.addAll(bugVariants.get(0));
                }
            }
        } else {
            File[] pngFiles = dir.listFiles(
                    (d, name) -> name.startsWith(filePrefix) && name.endsWith(".png"));

            if (pngFiles != null && pngFiles.length > 0) {
                sortByFrameNumber(pngFiles, filePrefix + "(\\d+)\\.png");

                for (File f : pngFiles) {
                    BufferedImage img = ImageIO.read(f);
                    if (img != null) frames.add(img);
                }
            }
        }
    }

    private void loadFromClasspath(CharacterState state, String path, List<BufferedImage> frames)
            throws IOException {

        if (state == CharacterState.BUG) {
            int fileIndex = 0;
            while (true) {
                InputStream gifStream = getClass().getClassLoader()
                        .getResourceAsStream(path + filePrefix + fileIndex + ".gif");
                if (gifStream == null) break;

                List<BufferedImage> variantFrames = extractGifFramesFromStream(gifStream);
                if (!variantFrames.isEmpty()) {
                    bugVariants.put(fileIndex, variantFrames);
                }
                fileIndex++;
            }
            if (!bugVariants.isEmpty()) {
                frames.addAll(bugVariants.get(0));
            }
        } else {
            int frameIndex = 0;
            while (true) {
                InputStream frameStream = getClass().getClassLoader()
                        .getResourceAsStream(path + filePrefix + frameIndex + ".png");
                if (frameStream == null) break;

                BufferedImage img = ImageIO.read(frameStream);
                if (img != null) frames.add(img);
                frameIndex++;
            }
        }
    }

    // ─── Публичный API ──────────────────────────────────────────────────────

    public BufferedImage getFrame(CharacterState state, int frameIndex) {
        List<BufferedImage> sprites = getSprites(state);
        if (sprites == null || sprites.isEmpty()) return null;
        return sprites.get(frameIndex % sprites.size());
    }

    public List<BufferedImage> getSprites(CharacterState state) {
        if (!spriteCache.containsKey(state)) {
            loadSprites(state);
        }
        return spriteCache.get(state);
    }

    public int getFrameCount(CharacterState state) {
        List<BufferedImage> sprites = getSprites(state);
        return sprites != null ? sprites.size() : 0;
    }

    public int getFrameDelay(CharacterState state) {
        return frameDelays.getOrDefault(state, 100);
    }

    public void preloadAll() {
        for (CharacterState s : CharacterState.values()) {
            loadSprites(s);
        }
    }

    public List<BufferedImage> getRandomBugVariant() {
        if (bugVariants.isEmpty()) {
            return getSprites(CharacterState.BUG);
        }
        return bugVariants.get(random.nextInt(bugVariants.size()));
    }

    public String getCharacterName() {
        return characterName;
    }

    // ─── Утилиты ────────────────────────────────────────────────────────────

    private void sortByFrameNumber(File[] files, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Arrays.sort(files, (a, b) -> {
            int na = extractFrameNumber(a.getName(), pattern);
            int nb = extractFrameNumber(b.getName(), pattern);
            return Integer.compare(na, nb);
        });
    }

    private int extractFrameNumber(String fileName, Pattern pattern) {
        Matcher m = pattern.matcher(fileName);
        if (m.find()) {
            try { return Integer.parseInt(m.group(1)); }
            catch (NumberFormatException e) { return 0; }
        }
        return 0;
    }

    private List<BufferedImage> extractGifFrames(File gifFile) {
        try (ImageInputStream stream = ImageIO.createImageInputStream(gifFile)) {
            return extractGifFramesFromStream(stream);
        } catch (IOException e) {
            System.out.println("Ошибка чтения GIF: " + gifFile.getName());
            return new ArrayList<>();
        }
    }

    private List<BufferedImage> extractGifFramesFromStream(InputStream inputStream) {
        try (ImageInputStream stream = ImageIO.createImageInputStream(inputStream)) {
            return extractGifFramesFromStream(stream);
        } catch (IOException e) {
            System.out.println("Ошибка чтения GIF из стрима");
            return new ArrayList<>();
        }
    }

    private List<BufferedImage> extractGifFramesFromStream(ImageInputStream stream) throws IOException {
        List<BufferedImage> frames = new ArrayList<>();
        Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("gif");
        if (!readers.hasNext()) return frames;

        ImageReader reader = readers.next();
        reader.setInput(stream);
        int count = reader.getNumImages(true);
        for (int i = 0; i < count; i++) {
            frames.add(reader.read(i));
        }
        reader.dispose();
        return frames;
    }

    private void loadDefaultFrameDelays() {
        frameDelays.put(CharacterState.IDLE, 150);
        frameDelays.put(CharacterState.CURIOUS, 120);
        frameDelays.put(CharacterState.DRAGGING, 50);
        frameDelays.put(CharacterState.SLEEPING, 200);
        frameDelays.put(CharacterState.BUG, 120);
    }
}
