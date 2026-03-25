package lars.com.reactions;

import lars.com.model.CharacterId;
import lars.com.model.DialogueLine;

import java.util.*;

public class DualResponseLibrary {

    /**
     * Библиотека реплик для Джекилла и Шарля.
     *
     *  1. Монолог одного персонажа (soliloquy):
     *     Одна реплика от конкретного CharacterId.
     *     Добавляется через addMono(category, CharacterId, "текст", ...).
     *
     *  2. Диалог (dialogue):
     *     Цепочка реплик от обоих персонажей поочерёдно.
     *     Добавляется через addDialogue(category, DialogueLine...).
     *
     *  3. Смешанный пул:
     *     В категории могут быть и монологи, и диалоги.
     *     getResponse(category) случайно вернёт один из вариантов.
     *
     * ── Использование: ─────────────────────────────────────────────────────────
     *
     *   DualResponseLibrary lib = new DualResponseLibrary();
     *
     *   // Один персонаж:
     *   lib.addMono("browser", CharacterId.CHARLES,
     *       "Что будете искать сегодня?",
     *       "Браузер? Снова?");
     *
     *   // Диалог двух:
     *   lib.addDialogue("browser",
     *       new DialogueLine(CharacterId.JEKYLL,  "Интернет..."),
     *       new DialogueLine(CharacterId.CHARLES, "Тихо, я думаю."),
     *       new DialogueLine(CharacterId.JEKYLL,  "Ты всегда думаешь."));
     *
     *   // Получить случайный вариант (монолог или диалог):
     *   List<DialogueLine> lines = lib.getResponse("browser");
     *   dialogueQueue.addAll(lines);
     *
     * ──────────────────────────────────────────────────────────────────────────
     */

    private final Map<String, List<List<DialogueLine>>> pool = new HashMap<>();
    private final Random random = new Random();
    /**
     * Добавить один или несколько монологов одного персонажа в категорию.
     * Каждая строка — отдельный вариант (не цепочка).
     */
    public void addMono(String category, CharacterId who, String... texts) {
        List<List<DialogueLine>> variants = pool.computeIfAbsent(category, k -> new ArrayList<>());
        for (String text : texts) {
            variants.add(Collections.singletonList(new DialogueLine(who, text)));
        }
    }

    /**
     * Добавить один диалог (цепочку реплик) как один вариант в категорию.
     * Вызывать этот метод несколько раз, чтобы добавить несколько разных диалогов.
     */
    public void addDialogue(String category, DialogueLine... lines) {
        List<List<DialogueLine>> variants = pool.computeIfAbsent(category, k -> new ArrayList<>());
        variants.add(Arrays.asList(lines));
    }

    /** Удобный вариант с List. */
    public void addDialogue(String category, List<DialogueLine> lines) {
        List<List<DialogueLine>> variants = pool.computeIfAbsent(category, k -> new ArrayList<>());
        variants.add(new ArrayList<>(lines));
    }

    /**
     * Вернуть случайный вариант из категории.
     * Если категория не найдена — берёт из "default".
     * Если и "default" нет — возвращает пустой список.
     */
    public List<DialogueLine> getResponse(String category) {
        List<List<DialogueLine>> variants = pool.get(category);
        if (variants == null || variants.isEmpty()) {
            variants = pool.get("default");
        }
        if (variants == null || variants.isEmpty()) {
            return Collections.emptyList();
        }
        return variants.get(random.nextInt(variants.size()));
    }

    /**
     * Есть ли хоть один вариант для данной категории?
     */
    public boolean hasCategory(String category) {
        List<List<DialogueLine>> variants = pool.get(category);
        return variants != null && !variants.isEmpty();
    }

    /**
     * Все зарегистрированные категории.
     */
    public Set<String> categories() {
        return Collections.unmodifiableSet(pool.keySet());
    }

    /**
     * Создаёт и заполняет библиотеку начальными репликами.
     * Замените/дополните этот метод своими текстами.
     */
    public static DualResponseLibrary createDefault() {
        DualResponseLibrary lib = new DualResponseLibrary();

        // ── default ───────────────────────────────────────────────────────────
        lib.addMono("default", CharacterId.CHARLES,
                "Хм-м. Занятно.",
                "Вы не перестаёте меня удивлять.",
                "Любопытное наблюдение.");
        lib.addMono("default", CharacterId.JEKYLL,
                "...",
                "Понятно.",
                "Надо же.");

        lib.addMono("browser", CharacterId.CHARLES,
                "Что будете искать сегодня?",
                "Концепт интернета — действительно любопытная вещь.",
                "Хотите совет? Делайте пароли посложнее.");
        lib.addMono("browser", CharacterId.JEKYLL,
                "Опять в браузер.",
                "Ищете что-то конкретное или просто бродите?");

        lib.addDialogue("browser",
                new DialogueLine(CharacterId.JEKYLL,  "Браузер снова открыт."),
                new DialogueLine(CharacterId.CHARLES, "Разумеется. Люди не могут без него и часа."),
                new DialogueLine(CharacterId.JEKYLL,  "Ты тоже так думаешь о нас?"),
                new DialogueLine(CharacterId.CHARLES, "О вас — особенно."));

        lib.addDialogue("browser",
                new DialogueLine(CharacterId.CHARLES, "Снова в сеть? Что на этот раз?"),
                new DialogueLine(CharacterId.JEKYLL,  "Может, что-то полезное."),
                new DialogueLine(CharacterId.CHARLES, "Хотелось бы верить, Джекилл. Хотелось бы."));

        lib.addMono("discord", CharacterId.CHARLES,
                "Дискорд? Пусть Вас нисколько не смущает моё присутствие.",
                "Ваш смех от разговоров с друзьями — такая мимолётная вещь.");
        lib.addMono("discord", CharacterId.JEKYLL,
                "Общаетесь с кем-то?",
                "Голосовой чат? Интересно, о чём говорят.");

        lib.addDialogue("discord",
                new DialogueLine(CharacterId.JEKYLL,  "Дискорд."),
                new DialogueLine(CharacterId.CHARLES, "Знаю. Слышу каждый раз."),
                new DialogueLine(CharacterId.JEKYLL,  "Ты подслушиваешь?"),
                new DialogueLine(CharacterId.CHARLES, "Я наблюдаю. Это совершенно разные вещи."));

        lib.addMono("steam", CharacterId.CHARLES,
                "О, неужели Стим? Что же Вы собираетесь запустить?",
                "Вы не думали о том, чтобы оставлять меня с Вами на время игр?");
        lib.addMono("steam", CharacterId.JEKYLL,
                "Стим. Значит, скоро будет не до нас.",
                "Во что играем?");

        lib.addDialogue("steam",
                new DialogueLine(CharacterId.CHARLES, "Стим открыт. Любопытно."),
                new DialogueLine(CharacterId.JEKYLL,  "Ты тоже это заметил."),
                new DialogueLine(CharacterId.CHARLES, "Я замечаю всё, Джекилл."),
                new DialogueLine(CharacterId.JEKYLL,  "Это немного пугает."),
                new DialogueLine(CharacterId.CHARLES, "Немного? Вы меня разочаровываете."));

        // ── idle ──────────────────────────────────────────────────────────────
        lib.addMono("idle_special", CharacterId.CHARLES,
                "Время — такая любопытная вещь. Вы так усердно его тратите.",
                "Прошли очередные десять минут Вашей жизни. С пользой, надеюсь?");
        lib.addMono("idle_special", CharacterId.JEKYLL,
                "Тихо. Даже слишком.",
                "Вы всё ещё здесь?");

        lib.addDialogue("idle_special",
                new DialogueLine(CharacterId.JEKYLL,  "Шарль."),
                new DialogueLine(CharacterId.CHARLES, "Что?"),
                new DialogueLine(CharacterId.JEKYLL,  "Ничего. Просто тихо."),
                new DialogueLine(CharacterId.CHARLES, "Тишина — это роскошь. Наслаждайтесь."));

        // ── правый клик по Джекиллу (используется JekyllWindow напрямую) ────
        // Эти фразы передаются в конструктор JekyllWindow как отдельный список.

        return lib;
    }
}
