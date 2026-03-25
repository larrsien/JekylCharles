package lars.com.events;

import lars.com.UI.AmonWindow;

import javax.swing.*;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MealReminder {
    private final AmonWindow amonWindow;
    private final ScheduledExecutorService scheduler;
    private final Random random;

    // Время приемов пищи
    private Map<String, MealTime> mealTimes;

    // Отслеживание, какие напоминания уже были показаны сегодня
    private final Set<String> shownToday;
    private int lastDayOfYear = -1;

    public MealReminder(AmonWindow amonWindow) {
        this.amonWindow = amonWindow;
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.random = new Random();
        this.shownToday = new HashSet<>();

        initializeMealTimes();
    }

    private void initializeMealTimes() {
        mealTimes = new HashMap<>();

        mealTimes.put("breakfast", new MealTime(
                12, 0,
                Arrays.asList("Вы когда-нибудь замечали, что утро без еды окрашивается в унылые тона? Я, признаться, не замечал. Я и не завтракаю-то вовсе! Но вы – существо иное. Мне более по душе, когда Вы бодры и полны сил совершать свои человеческие глупости. Так что будьте добры, подкрепитесь.",
                        "Знаете, человеческий организм без еды – зрелище жалкое. Все равно что быть без монокля: функционировать, но выглядеть попросту непрезентабельно. Исправьте это недоразумение. Не забывайте о приёмах пищи.",
                        "Советую Вам последовать примеру нормальных существ и чем-нибудь перекусить. Понимаю, это хлопотно. Отвлекает. Забирает время, которое можно было потратить на что-то более «полезное». Но Вашему организму это нужно. А мне – нужны Вы. Для самых-самых разных целей."
                )
        ));

        mealTimes.put("lunch", new MealTime(
                19, 0,
                Arrays.asList("День в разгаре, а Вы всё ещё не ели? Конечно, есть некоторая изысканная прелесть в том, чтобы наблюдать за чьим-то медленным угасанием, но всё же рекомендую Вам сходить на обед. Я пока присмотрю за Вашими делами. ...Вы ведь мне доверяете? Полностью? Ха-ха-ха.",
                        "Вы всерьёз полагаете, что у обеда можно украсть время и потратить его на работу? О, наивность! Даже я знаю: сытный обед можно только перенести. Ступайте покушать поскорее.",
                        "Я тут прикинул кое-что. Если Вы пропустите очередной приём пищи, то к вечеру станете таким голодным, что продолжать присматривать за Вами будет совершенно неинтересно. Подсобите мне немножко – сходите покушать.",
                        "Ах, этот чудный момент, когда организм требует пищи, а Вы думаете о том, чтобы ещё чуть-чуть поработать. Благородно. И ужасно глупо. Лучше сдайтесь сразу и сходите пообедайте."
                )
        ));

        mealTimes.put("snack", new MealTime(
                1, 0,
                Arrays.asList("Вы случайно не пропустили приём пищи? Ваша преданность делам, конечно, ценна – примерно как красивая безделушка: глаз радует, а пользы... Ну да неважно. Ваше здоровье тоже по-своему важно. Хотя бы потому, что я планирую наблюдать за Вами ещё долго.",
                        "Как там говорится? Еда – это топливо? Без топлива Вы не сможете совершать те нелепые и забавные ошибки, за которыми я имею удовольствие наблюдать. Не забывайте поесть.",
                        "Знаете, даже мне известно, что иногда нужно подкрепляться. Иначе откуда брать силы на то, чтобы тратить время впустую? Поддерживайте форму. Не забывайте о приёмах пищи. Я должен видеть Вас в лучшем виде."
                )
        ));
    }

    public void start() {
        System.out.println("Запуск системы напоминаний о еде");

        // Проверяем каждую минуту, не пора ли напомнить о еде
        scheduler.scheduleAtFixedRate(this::checkMealTimes, 0, 1, TimeUnit.MINUTES);
    }

    private void checkMealTimes() {
        LocalTime now = LocalTime.now();
        int currentDayOfYear = java.time.LocalDate.now().getDayOfYear();

        // Сбрасываем показанные напоминания в начале нового дня
        if (currentDayOfYear != lastDayOfYear) {
            shownToday.clear();
            lastDayOfYear = currentDayOfYear;
            System.out.println("Новый день - сброс напоминаний о еде");
        }

        // Проверяем каждое время приема пищи
        for (Map.Entry<String, MealTime> entry : mealTimes.entrySet()) {
            String mealName = entry.getKey();
            MealTime mealTime = entry.getValue();

            // Если уже показывали это напоминание сегодня - пропускаем
            if (shownToday.contains(mealName)) {
                continue;
            }

            // Проверяем, совпадает ли время (час и минута)
            if (now.getHour() == mealTime.hour && now.getMinute() == mealTime.minute) {
                showMealReminder(mealName, mealTime);
                shownToday.add(mealName);
            }
        }
    }

    private void showMealReminder(String mealName, MealTime mealTime) {
        if (!amonWindow.isMealReminderEnabled()) {
            return;
        }

        // Выбираем случайное сообщение из списка
        String message = mealTime.messages.get(random.nextInt(mealTime.messages.size()));
        SwingUtilities.invokeLater(() -> amonWindow.reactToEvent(message));
    }

    public void stop() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
            }
        }
    }

    // Вспомогательный класс для хранения информации о времени приема пищи
    private static class MealTime {
        int hour;
        int minute;
        List<String> messages;

        MealTime(int hour, int minute, List<String> messages) {
            this.hour = hour;
            this.minute = minute;
            this.messages = messages;
        }
    }
}
