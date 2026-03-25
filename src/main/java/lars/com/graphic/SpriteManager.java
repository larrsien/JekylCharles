package lars.com.graphic;

import lars.com.model.AmonState;

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
    private static final String SPRITE_PATH = "sprites/";
    private final Map<AmonState, List<BufferedImage>> spriteCache;
    private final Map<AmonState, Integer> frameDelays;
    private final Map<Integer, List<BufferedImage>> bugVariants = new HashMap<>();
    private final Random random = new Random();

    public SpriteManager() {
        this.spriteCache = new HashMap<>();
        this.frameDelays = new HashMap<>();
        loadDefaultFrameDelays();
    }

    private void loadSprites(AmonState state) {

        String path = SPRITE_PATH + state.getStateName() + "/";
        List<BufferedImage> frames = new ArrayList<>();

        try {
            String firstFileName = (state == AmonState.BUG) ? "amon_0.gif" : "amon_0.png";
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(path + firstFileName);

            if (inputStream == null) {
                File file = new File("src/main/resources/" + path);
                if (!file.exists()) {
                    System.out.println("Файл со спрайтом не найден");
                    return;
                }

                // для BUG ищем GIF файлы
                if (state == AmonState.BUG) {
                    File[] gifFiles = file.listFiles((dir, name) -> name.startsWith("amon_") && name.endsWith(".gif"));

                    if (gifFiles != null && gifFiles.length > 0) {
                        Arrays.sort(gifFiles, (f1, f2) -> {
                            Pattern pattern = Pattern.compile("amon_(\\d+)\\.gif");
                            int num1 = extractFrameNumber(f1.getName(), pattern);
                            int num2 = extractFrameNumber(f2.getName(), pattern);
                            return Integer.compare(num1, num2);
                        });

                        for (int i = 0; i < gifFiles.length; i++) {
                            List<BufferedImage> variantFrames = extractGifFrames(gifFiles[i]);
                            if (!variantFrames.isEmpty()) {
                                bugVariants.put(i, variantFrames);
                            }
                        }

                        if (!bugVariants.isEmpty()) {
                            frames.addAll(bugVariants.get(0));
                        }
                    }
                } else {
                    // оригинальный код для PNG
                    File[] files = file.listFiles((dir, name) -> name.startsWith("amon_") && name.endsWith(".png"));

                    if (files != null && files.length > 0) {
                        Arrays.sort(files, (f1, f2) -> {
                            Pattern pattern = Pattern.compile("amon_(\\d+)\\.png");
                            int num1 = extractFrameNumber(f1.getName(), pattern);
                            int num2 = extractFrameNumber(f2.getName(), pattern);
                            return Integer.compare(num1, num2);
                        });

                        for (File ourFile : files) {
                            BufferedImage image = ImageIO.read(ourFile);
                            if (image != null) {
                                frames.add(image);
                            }
                        }
                    }
                }
            } else {
                // из jar — для BUG ищем GIF
                if (state == AmonState.BUG) {
                    int fileIndex = 0;
                    while (true) {
                        InputStream gifStream = getClass().getClassLoader()
                                .getResourceAsStream(path + "amon_" + fileIndex + ".gif");
                        if (gifStream == null) break;
                        List<BufferedImage> variantFrames = extractGifFramesFromStream(gifStream);
                        if (!variantFrames.isEmpty()) {
                            bugVariants.put(fileIndex, variantFrames);
                        }
                        fileIndex++;
                    }
                } else {
                    int frameIndex = 0;
                    while (true) {
                        InputStream frameStream = getClass().getClassLoader()
                                .getResourceAsStream(path + "amon_" + frameIndex + ".png");
                        if (frameStream == null) break;
                        BufferedImage img = ImageIO.read(frameStream);
                        if (img != null) {
                            frames.add(img);
                        }
                        frameIndex++;
                    }
                }
            }

            if (frames.isEmpty()) {
                System.out.println("Фреймов не нашлось");
            } else {
                spriteCache.put(state, frames);
                System.out.println("Загружены спрайты для " + state);
            }

        } catch (IOException e) {
            System.out.println("Не удалось вообще ничего сделать, исключение в try/catch");
        }
    }

    public List<BufferedImage> getRandomBugVariant() {
        if (bugVariants.isEmpty()) {
            // fallback: возвращаем всё что есть
            return getSprites(AmonState.BUG);
        }
        int randomIndex = random.nextInt(bugVariants.size());
        return bugVariants.get(randomIndex);
    }

    // извлекает все кадры из GIF файла
    private List<BufferedImage> extractGifFrames(File gifFile) {
        try (ImageInputStream stream = ImageIO.createImageInputStream(gifFile)) {
            return extractGifFramesFromStream(stream);
        } catch (IOException e) {
            System.out.println("Ошибка чтения GIF: " + gifFile.getName());
            return new ArrayList<>();
        }
    }

    // извлекает все кадры из GIF стрима
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
        if (!readers.hasNext()) {
            System.out.println("GIF reader не найден");
            return frames;
        }

        ImageReader reader = readers.next();
        reader.setInput(stream);

        int frameCount = reader.getNumImages(true);
        for (int i = 0; i < frameCount; i++) {
            frames.add(reader.read(i));
        }
        reader.dispose();

        return frames;
    }

    private void loadDefaultFrameDelays() {
        frameDelays.put(AmonState.IDLE, 150);
        frameDelays.put(AmonState.CURIOUS, 120);
        frameDelays.put(AmonState.DRAGGING, 50);
        frameDelays.put(AmonState.SLEEPING, 200);
        frameDelays.put(AmonState.BUG, 120);
    }

    // найти один фрейм для состояния
    public BufferedImage getFrame(AmonState state, int frameIndex) {
        List<BufferedImage> sprites = getSprites(state);
        if (sprites == null || sprites.isEmpty()) {
            return null;
        }
        return sprites.get(frameIndex % sprites.size());
    }

    public List<BufferedImage> getSprites(AmonState state) {
        if (!spriteCache.containsKey(state)) {
            loadSprites(state);
        }
        return spriteCache.get(state);
    }

    public int getFrameCount(AmonState state) {
        List<BufferedImage> sprites = getSprites(state);
        return sprites != null ? sprites.size() : 0;
    }

    public int getFrameDelay(AmonState state) {
        return frameDelays.getOrDefault(state, 100);
    }

    public void preloadAll() {
        for (AmonState amonState : AmonState.values()) {
            loadSprites(amonState);
        }
    }

    // для извлечения номера кадра и нормальной сортировки
    private int extractFrameNumber(String fileName, Pattern pattern) {
        Matcher matcher = pattern.matcher(fileName);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }
}
