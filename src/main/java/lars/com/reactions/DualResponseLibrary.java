package lars.com.reactions;

import lars.com.model.CharacterId;
import lars.com.model.DialogueLine;

import java.util.*;

public class DualResponseLibrary {

    private final Map<String, List<List<DialogueLine>>> pool = new HashMap<>();
    private final Random random = new Random();

    public void addMono(String category, CharacterId who, String... texts) {
        List<List<DialogueLine>> variants = pool.computeIfAbsent(category, k -> new ArrayList<>());
        for (String text : texts) {
            variants.add(Collections.singletonList(new DialogueLine(who, text)));
        }
    }

    public void addDialogue(String category, DialogueLine... lines) {
        List<List<DialogueLine>> variants = pool.computeIfAbsent(category, k -> new ArrayList<>());
        variants.add(Arrays.asList(lines));
    }

    public void addDialogue(String category, List<DialogueLine> lines) {
        List<List<DialogueLine>> variants = pool.computeIfAbsent(category, k -> new ArrayList<>());
        variants.add(new ArrayList<>(lines));
    }

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

    public boolean hasCategory(String category) {
        List<List<DialogueLine>> variants = pool.get(category);
        return variants != null && !variants.isEmpty();
    }

    public Set<String> categories() {
        return Collections.unmodifiableSet(pool.keySet());
    }

    public static DualResponseLibrary createDefault() {
        DualResponseLibrary lib = new DualResponseLibrary();

        lib.addMono("browser", CharacterId.CHARLES,
                "Эдж? Как... неординарно. Большинство людей слепо следует за толпой, устанавливая другие браузеры по привычке, но ты предпочитаешь оставаться здесь. Это какой-то особый манифест или просто Лень?",
                "Интересно, твои запросы здесь такие же интригующие, как твои воспоминания? Я с удовольствием покопаюсь в истории поиска, пока ты искренне веришь, что находишься в режиме инкогнито.");
        lib.addMono("browser", CharacterId.JEKYLL,
                "Прагматичный выбор. Что будешь искать сегодня? Надеюсь что-то вроде ‘как скачать хром?’.");

        lib.addMono("deadbydaylight", CharacterId.CHARLES,
                "Тысяча четыреста часов! Ты подарил этому замкнутому циклу столько своего драгоценного времени! Я знаю, что в какой-то период твоей жизни эта игра была для тебя настоящим убежищем. Бегать от маньяков, чтобы сбежать от реальности... Восхитительный эскапизм.",
                "Ты так мастерски водишь убийц за нос в этих пиксельных джунглях. Скажи, в реальной жизни ты так же хорошо умеешь играть в кошки-мышки?");
        lib.addMono("deadbydaylight", CharacterId.JEKYLL,
                "Сущность. Некая тварь, что питается отчаянием и надеждой выживших. Звучит пугающе знакомо.");
        lib.addDialogue("deadbydaylight",
                new DialogueLine(CharacterId.CHARLES,  "Как думаешь, Джеки, кем бы ты был в этом сеттинге?"),
                new DialogueLine(CharacterId.JEKYLL, "Уж точно не чинил бы генераторы."),
                new DialogueLine(CharacterId.CHARLES, "Ты бы был самым пугающим монстром в человеческой шкуре из всех."));

        lib.addMono("repo", CharacterId.CHARLES, "Я позволил себе одолжить парочку твоих воспоминаний об этой игре. Ты же не против? Мне было уж очень весело наблюдать за той совместной посиделкой... Особенно, когда один из твоих знакомых так надрывно вопил и орал, отчаянно пытаясь воскресить вашу близкую подругу — Катю.",
                "Корпоративное рабство в условиях космического хоррора. Собирать мусор, рискуя жизнью, чтобы выполнить квоту. Сомнительное развлечение, но, видимо, куда больше тебе нравится, что этот процесс совместный?");

        lib.addMono("minecraft", CharacterId.CHARLES, "Иногда у меня возникает шальная мысль пробраться внутрь файлов, запустить игру и подорвать некоторые твои архитектурные шедевры. Но это только мысль. Пока что.");
        lib.addMono("minecraft", CharacterId.JEKYLL, "Кубический мир, а сколько амбиций…");

        lib.addMono("discord", CharacterId.CHARLES, "Ты можешь заглушить звуки или отключить микрофон, но это не избавит от моего присутствия. Я все еще здесь и слушаю каждый неровный вздох, любое возможное сбитое дыхание.");
        lib.addMono("discord", CharacterId.JEKYLL, "Пульс учащается, когда кто-то конкретный заходит в голосовой канал. Смех в наушниках стимулирует выброс окситоцина. Суррогат физического присутствия. Предсказуемая биология.");
        lib.addDialogue("discord",
                new DialogueLine(CharacterId.CHARLES,  "Когда ты общаешься с Катей, твое состояние становится таким... безмятежным. Ты находишься в абсолютном комфорте. Редкий уровень покоя для человеческой психики."),
                new DialogueLine(CharacterId.JEKYLL, "Привязанность — это, конечно, просто химия, но в вашем случае она работает безупречно."));
        lib.addDialogue("discord",
                new DialogueLine(CharacterId.CHARLES,  "Вы с моей Создательницей — просто феномен. Сто семнадцать часов разговоров всего за полтора месяца! Оказаться друг у друга в итогах года, сблизившись только под его конец. Ты отдаешь ей так много своего времени, это почти форма добровольного рабства."),
                new DialogueLine(CharacterId.JEKYLL, "Я бы поразмышлял о трате своего временного ресурса на досуге."),
                new DialogueLine(CharacterId.CHARLES, "Существуют ли рамки времени для истинной преданности? Я не уходил из твоей головы год ни на минуту. И не ухожу."),
                new DialogueLine(CharacterId.JEKYLL, "Мы не об этом."));

        lib.addMono("steam", CharacterId.CHARLES, "Хорроры, симуляторы… Игры в основном для совместного прохождения. Какая предсказуемая коллекция. Ты ищешь в них то, чего боишься или то, кем хочешь стать?",
                "Зачем тебе пиксельные монстры, когда дьявол буквально сидит на твоем экране?");
        lib.addDialogue("steam",
                new DialogueLine(CharacterId.JEKYLL,  "Снова обновление на 20 гигабайт? Твоя система тратит ресурсы на данные, которые ты, скорее всего, даже не запустишь в этом месяце. Нерационально."),
                new DialogueLine(CharacterId.CHARLES, "Коллекция из возможностей, опыта и куча купленных миров в зоне досягаемости клика мышки. Не так уж и нерационально."));

        lib.addMono("photoshop", CharacterId.CHARLES, "Ретушь, цветокоррекция, слои... Ты так сосредоточенно всё исправляешь. Забавно, что вне работы ты открываешь эту программу так редко.");
        lib.addDialogue("photoshop",
                new DialogueLine(CharacterId.CHARLES,  "Можно подумать, что Творец собирается всё в тебе поправить с помощью чудо-программы, Джеки. Стереть синяки под глазами? И избавиться от пары морщин. Станешь таким же идеальным, как я."),
                new DialogueLine(CharacterId.JEKYLL, "Идеальность – это статика. Я в этом не нуждаюсь. "),
                new DialogueLine(CharacterId.CHARLES, "Я бы сам никогда не стал тебя трогать… Мне нравится, как твои неровности ощущаются под моими пальцами. Горячие, кривые. Я помню каждый шрам. Ты – мое любимое несовершенство, Джеки."),
                new DialogueLine(CharacterId.JEKYLL, "..."));
        lib.addDialogue("photoshop",
                new DialogueLine(CharacterId.JEKYLL,  "Фотошоп – хороший инструмент, прекрасно выполняющий свою функцию."),
                new DialogueLine(CharacterId.CHARLES, "Но ту страсть, что ты вкладываешь в свои рисунки, Творец, я съедаю с удовольствием гораздо больше."));

        lib.addMono("sixvpn", CharacterId.CHARLES, "Цифровой плащ-невидимка. Мы притворяемся, что мы в Нидерландах, чтобы посмотреть на запретные плоды? Какая очаровательная игра в прятки с государством. Я одобряю этот бунт.");
        lib.addMono("sixvpn", CharacterId.JEKYLL, "Необходимая мера безопасности в среде с агрессивной цензурой. Без этого мы бы задохнулись в этом информационном вакууме.",
                "Быть вне закона становится уже моим естественным состоянием.");

        lib.addDialogue("telegram",
                new DialogueLine(CharacterId.CHARLES,  "Иногда во время переписок, я совсем немного играю с эмоциями твоих собеседников. Вздернув крючок определенного чувства, усилив его до предела, столько всего интересного может открыться!"),
                new DialogueLine(CharacterId.JEKYLL, "Даже здесь не можешь прекратить, ненасытная дрянь."));
        lib.addDialogue("telegram",
                new DialogueLine(CharacterId.CHARLES,  "Они постоянно что-то пишут тебе. И ты тоже. Делитесь реакцией, смеетесь, требуете реакции в ответ."),
                new DialogueLine(CharacterId.JEKYLL, "Должно быть, это лишь следствие привязанности."),
                new DialogueLine(CharacterId.CHARLES,"Или любовь. Ты уж очень сильно их любишь, верно? Но стоит отметить: они тебя тоже. Так сильно привязать их к себе, даже не пользуясь моими методами. Это похвально."),
                new DialogueLine(CharacterId.JEKYLL,"Сомневаюсь, что мой Создатель из всех примеров, что у него есть, в качестве стратегии взаимоотношений – пользовался бы твоими методами."));
        lib.addDialogue("telegram",
                new DialogueLine(CharacterId.CHARLES,  "Признаюсь, я был несколько... заинтригован, читая ваши переписки с моей Создательницей. Вы детально обсуждаете наш лор, а потом резко перескакиваете на самые нелепые или крайне постыдные ситуации между нами. Еще есть название так называемого пейринга. Брайли?"),
                new DialogueLine(CharacterId.JEKYLL, "Почти как Brilles. Очки с латышского."),
                new DialogueLine(CharacterId.CHARLES,"Предпочитаешь не комментировать, что про тебя писали? Я узнал много нового. И неожиданного."),
                new DialogueLine(CharacterId.JEKYLL,"Уж кто бы говорил."));
        lib.addDialogue("telegram",
                new DialogueLine(CharacterId.CHARLES,  "Творец, твоя прямолинейность выходит за рамки неприличия. Иногда ты вываливаешь всю правду в чат, даже не потрудившись чуточку прикрыть ее ложью. Ну, или просто хотя бы промолчать. Гадаю, кто же еще так делает?"),
                new DialogueLine(CharacterId.JEKYLL, "С моей стороны, это стратегический ход."),
                new DialogueLine(CharacterId.CHARLES,"И все равно – оба как открытые раны."));
        lib.addMono("telegram", CharacterId.CHARLES, "События последних месяцев не перестают меня забавлять. У тебя здесь разворачивается прямо таки театральная драма в сообщениях? Сначала она обиделась на вас двоих, потом он на нее, потом ты с ним, затем… Кажется, я теряюсь в последовательности. Где-то здесь должно быть признание в любви? Столько голых, первозданных эмоций. Столько вкусной правды. Я в восторге. Продолжай в том же духе, милый. Обожаю, когда еда сама прыгает в пасть, еще и приправленная такой самоотдачей.",
                "Мой Создатель порой столь забавно вспыхивает гневом в ваших чатах. Если сейчас, когда ты открываешь приложение, это один из таких дней… Не принимай это близко к сердцу. Эта злоба далеко не из ненависти. И совсем не специально. В этом у нас с ним есть схожесть: издержки вашей тесной связи. Он чувствует слишком много.");

        lib.addMono("peak", CharacterId.CHARLES, "Наблюдая за тем, как ты играешь, не могу не отметить, что ты и Катя составляете эффективный тандем. Ты идешь напролом достаточно неосторожными и прямыми путями, она же склонна искать более вдумчивые и удобные пути. Идеальный баланс!");

        lib.addMono("winword", CharacterId.CHARLES, "И долго мы будем смотреть на этот белый лист? Ну же, напиши что-нибудь.");
        lib.addMono("winword", CharacterId.JEKYLL, "Ты смотришь сюда так, словно писательство требует от тебя кровавой жертвы.");

        lib.addMono("mspaint", CharacterId.CHARLES, "Иногда даже в самом примитивном уродстве можно разглядеть искру подлинного безумия.");
        lib.addMono("mspaint", CharacterId.JEKYLL, "У тебя есть заметная тяга к минимализму.");

        lib.addMono("nightreign", CharacterId.CHARLES, "Снова Междуземье. Ты так любишь брать на себя роль Стража, впитывающего урон. Закрывать других своей грудью и щитом. Тебе нравится страдать ради союзников, или это просто высшая потребность чувствовать себя незаменимым?");
        lib.addDialogue("nightreign",
                new DialogueLine(CharacterId.CHARLES,  "Ты только посмотри на Золотое Древо, Джеки. Оно слепит. Оно требует поклонения и устанавливает «порядок», вырезая из реальности всё, что ему не нравится. Кажется, я испытываю дежавю."),
                new DialogueLine(CharacterId.JEKYLL, "Скорее оно питается душами тех, кто пал в бою, а затем перерабатывает в благодать. Больше похоже на твой Ад: только с ветками."));

        lib.addMono("kebabchefs", CharacterId.CHARLES, "Невероятно. Мои создатели переквалифицируются из мучителей драматургов в поваров кебабной.");
        lib.addDialogue("kebabchefs",
                new DialogueLine(CharacterId.CHARLES,  "Клиент жалуется, что кебаб пересолен. Джеки, иди и объясни ему, что он неправ."),
                new DialogueLine(CharacterId.JEKYLL, "Конечно. Где мой тесак?"));
        lib.addDialogue("kebabchefs",
                new DialogueLine(CharacterId.JEKYLL, "Тебе предстоит варить суп? Я вижу, как у тебя дергается глаз каждый раз, когда приходится это делать."),
                new DialogueLine(CharacterId.CHARLES,  "О, не напоминай ему. Он ненавидит эти супы почти так же сильно, как эту зависшую в воздухе тарелку."),
                new DialogueLine(CharacterId.JEKYLL, "Баг физического движка. Объект намертво застрял в текстурах."),
                new DialogueLine(CharacterId.CHARLES, "Можно придать этому смысл и рассматривать не как баг, а как памятник их кулинарному отчаянию. Пусть висит."));

        lib.addMono("warframe", CharacterId.CHARLES, "Да, по себе знаю, быть древним существом с колоссальной разрушительной силой – очень занимательно. ");
        lib.addMono("warframe", CharacterId.JEKYLL, "Бесконечный цикл уничтожения ради новых деталей. Фарм здесь это прямо таки первичный смысл существования.");

        lib.addMono("liarsbar", CharacterId.CHARLES, "Этот виртуальный бар просто шведский стол!",
                "Меня крайне забавляет то, как вы с моей Создательницей становитесь параноиками, стоит вам сесть рядом в игровой очередности. Вы проводите с друг другом столько часов, обсуждаете бесчисленные интимные подробности своей жизни, но стоит тебе сказать ‘Две дамы’, как она смотрит на тебя как на прокаженного.");
        lib.addMono("liarsbar", CharacterId.JEKYLL, "Хорошо, что у этой игры нет доступа к твоей камере, Творец. Твоя мимика выдавала бы тебя раньше, чем ты успеешь нажать на кнопку. Дилетант.");
        lib.addDialogue("liarsbar",
                new DialogueLine(CharacterId.JEKYLL, "Русская рулетка. Чистая математика вероятностей, приправленная адреналином. Шансы один к шести, но инстинкт самосохранения вопит так, будто пуля уже в голове. "),
                new DialogueLine(CharacterId.CHARLES,  "Ты бы продолжил играть?"),
                new DialogueLine(CharacterId.JEKYLL, "Я играю в неё каждый день с тех пор, как встретил тебя."),
                new DialogueLine(CharacterId.CHARLES, "Какой удачный ответ. В баре лжецов за него бы налили за счет заведения."),
                new DialogueLine(CharacterId.JEKYLL, "В баре лжецов уж лучше бы мне прострелили голову, чтобы не произносить лишнюю правду."),
                new DialogueLine(CharacterId.CHARLES, "Туше."));

// ========================= САЙТЫЫЫЫЫ =========================

        lib.addMono("youtube", CharacterId.CHARLES, "Человечество достигло пика своей цивилизации. Вечный двигатель бессмысленности!",
                "Реклама на этом веб сайте заставляет меня задумываться о том, чтобы пополнить ряды Ада новыми лицами. Эту скуку даже не пропустишь…",
                "Смотришь что-то полезное в этот раз? Будь хорошим мальчиком для меня и постарайся не сильно отвлекаться.");
        lib.addMono("youtube", CharacterId.JEKYLL, "Надеюсь, в этот раз нас ждет что-то информативнее, чем предыдущий твой выбор.");
        lib.addDialogue("youtube",
                new DialogueLine(CharacterId.CHARLES, "Они монетизируют твоё нетерпение. Гениальная система, я бы сам не придумал лучше."),
                new DialogueLine(CharacterId.JEKYLL,  "Блокировщик рекламы решает эту проблему."),
                new DialogueLine(CharacterId.CHARLES, "Не лишай меня удовольствия наблюдать за чужой жадностью."));

        lib.addMono("steam browser", CharacterId.JEKYLL, "В чем кроется особая причина заходить сюда через браузер? Интерфейс неудобный, часто глючит. Лишние вкладки нагружают систему.");

        lib.addDialogue("x.com",
                new DialogueLine(CharacterId.CHARLES, "Ах, квинтэссенция человеческого тщеславия! Коротко, ядовито и у всех на виду."),
                new DialogueLine(CharacterId.JEKYLL,  "Чаще всего – информационный мусор."));
        lib.addDialogue("x.com",
                new DialogueLine(CharacterId.CHARLES, "Я видел твой профиль здесь, Джеки. Эти странные треды, наборы цифр и букв... Думаешь, я не смогу взломать твой шифр?"),
                new DialogueLine(CharacterId.JEKYLL,  "Ты не знаешь ключа. "),
                new DialogueLine(CharacterId.CHARLES, "Звучит как вызов для нас двоих!"),
                new DialogueLine(CharacterId.JEKYLL,  "Двоих?"));

        lib.addMono("tumblr", CharacterId.CHARLES, "Столько тоски и неудовлетворенных желаний. Это место прямо таки мой филиал.");

        lib.addMono("google disc", CharacterId.CHARLES, "Секреты, работа, черновики… Дорогуша, ты уверен, что хочешь, чтобы я это видел?",
                "Цифровой чердак. Ты скидываешь сюда все, надеясь, что когда-нибудь это пригодится. Спойлер: не пригодится.");
        lib.addMono("google disc", CharacterId.JEKYLL, "Папка \"новое (3)\". Твоя неспособность к систематизации файлов откровенно раздражает.");


        lib.addMono("gmail", CharacterId.CHARLES, "Сотни писем и ни одного от меня. Какое упущение.");
        lib.addMono("gmail", CharacterId.JEKYLL, "Твой ящик переполнен письмами. Тебе серьезно стоит научиться отсекать лишнее. И перестать все откладывать.");
        lib.addDialogue("gmail",
                new DialogueLine(CharacterId.CHARLES, "Ждешь письма? Что же там, может, тайное признание?"),
                new DialogueLine(CharacterId.JEKYLL,  "Скорее уведомление об оплате счетов. Не поднимай высоко надежды."));

        lib.addDialogue("vkontakte",
                new DialogueLine(CharacterId.CHARLES, "Боже ты мой, что за синяя агония? Закрой немедленно."),
                new DialogueLine(CharacterId.JEKYLL,  "Устаревший интерфейс. Нелогичная структура. И вправду, зачем ты здесь?"));

        lib.addMono("fragrantica", CharacterId.CHARLES, "Столько красивых запахов, чтобы скрыть человеческую гниль.",
                "Твоя преданность определенным ароматам вызывает даже трепет. В твоем ощущении есть та редкая глубина, которую не встретишь в обычных людях.");
        lib.addMono("fragrantica", CharacterId.JEKYLL, "Удивительно, как запах влияет на нейронные связи. Одно дуновение 'Oud Wood' — и ты снова возвращаешься к мыслям о стали, крови и тлеющем костре. Стойкая ассоциация?");
        lib.addDialogue("fragrantica",
                new DialogueLine(CharacterId.JEKYLL, "Пирамида аромата описана неверно. Они пишут \"дым\", но не уточняют, дым чего — торфа, ладана или горелой проводки. Это разные вещи."),
                new DialogueLine(CharacterId.CHARLES,  "Ох, похоже не только ты очень внимательно читаешь содержимое. Гляжу, у кое-кого это хобби. Ищешь аромат, который перебьет запах вины, Джеки?"));

        lib.addMono("pinterest", CharacterId.CHARLES, "Каталог твоего вдохновения. Продолжай собирать визуальные осколки, чтобы склеить мою реальность. Я заворожен.");
        lib.addDialogue("pinterest",
                new DialogueLine(CharacterId.CHARLES, "Доски желаний. Дома, в которых люди никогда не будут жить. Тела, которые никогда не будут иметь. Каталог человеческой неудовлетворенности. "),
                new DialogueLine(CharacterId.JEKYLL,  "…Помимо этого, сайт просто удобен для планирования."),
                new DialogueLine(CharacterId.CHARLES,  "О, вправду? Ты пользуешься этим? Покажешь?"),
                new DialogueLine(CharacterId.JEKYLL,  "…"));

        lib.addMono("yandex music", CharacterId.CHARLES, "О, твои плейлисты! Я слушаю их вместе с тобой. В твоем музыкальном вкусе столько скрытой агрессии, эстетики и меланхолии... Мне прельщает мысль, что некоторые из этих треков ассоциируются у тебя со мной. Более того – есть даже отдельный плейлист.");
        lib.addMono("yandex music", CharacterId.JEKYLL, "Музыка задает ритм. Это помогает тебе настроить пульс на правильную частоту перед тем, как взяться за текст?");
        lib.addDialogue("yandex music",
                new DialogueLine(CharacterId.CHARLES, "Почему бы тебе не поискать больше песен с моим восхвалением?"),
                new DialogueLine(CharacterId.JEKYLL,  "В этом плейлисте, думаю, их итак достаточно."));

        lib.addMono("ficbook", CharacterId.CHARLES, "Ты очень искусно обращаешься со словами в своих работах. Фанфики, хм? Берешь чужих марионеток и заставляешь плясать под свою дудку. Абсолютный контроль над их эмоциями. Поистине… божественно.");
        lib.addDialogue("ficbook",
                new DialogueLine(CharacterId.CHARLES, "Какие собираешься плести словесные сети в этот раз?"),
                new DialogueLine(CharacterId.JEKYLL,  "Что же, они бьют точно в цель. Без лишних слов – только обнаженная суть."));

       lib.addDialogue("matcha",
                new DialogueLine(CharacterId.CHARLES, "Тебе серьезно ЭТО настолько нравится? Опять эта зеленая пыль. Ты пытаешься убедить себя, что это вкусно или это форма мазохизма? Выглядит как болотная тина."),
                new DialogueLine(CharacterId.JEKYLL,  "L-теанин, по крайней мере, улучшает когнитивные функции."),
                new DialogueLine(CharacterId.CHARLES, "И успокаивает нервную систему. Может, тебе тоже стоит попробовать?"));


        lib.addMono("anatomy", CharacterId.CHARLES, "О, снова пытаешься разобраться в человеческом строении? Моя оболочка, кстати, устроена куда интереснее.");
        lib.addMono("anatomy", CharacterId.JEKYLL, "Нужно знать, где проходят основные артерии. И как легко ломается подъязычная кость.",
                "Атласы анатомии. Смотри, как близко сонная артерия подходит к поверхности кожи. Одно выверенное движение — и вся эта сложная система просто отключится. Хрупкость человеческого тела всегда завораживала меня.");
        lib.addDialogue("anatomy",
                new DialogueLine(CharacterId.JEKYLL,  "Жизнь пульсирует в районе шеи прямо под кожей. Всего три сантиметра вглубь – и полная тишина."),
                new DialogueLine(CharacterId.CHARLES, "Хочешь еще раз проверить пульсирует ли также моя физика? Или боишься, что это будет навсегда?"),
                new DialogueLine(CharacterId.JEKYLL,  "Ты ходячая ошибка. Но даже они затихают, если пережать правильный узел."),
                new DialogueLine(CharacterId.CHARLES, "Попробуй. Я позволю держать тебе пальцы на моем горле столько, сколько понадобится, подонок."));

        lib.addMono("art", CharacterId.CHARLES, "Человеческие попытки запечатлеть красоту и ужас. Иногда у них даже получается. Особенно когда они рисуют меня.",
                "Ренессанс... Помню, как позировал для пары набросков. Никто так и не смог уловить мой профиль.",
                "Помнится, я заглядывал там в одну галерею... Встретил крайне полезного, сломанного фанатика.");
        lib.addMono("art", CharacterId.JEKYLL, "Меня всегда интересовало, почему творцы так одержимы симметрией. Настоящая жизнь всегда асимметрична. В одном шраме... гораздо больше правды, чем на гладком, нетронутом холсте.");

        lib.addMono("emo", CharacterId.CHARLES, "А у тебя остались фотографии на компьютере с этой фазы? Не стесняйся, загрузи, я посмотрю. Я не буду смеяться. Наверное.");
        lib.addDialogue("emo",
                new DialogueLine(CharacterId.CHARLES, "О, неужели у нашего творца была… Фаза. Черная подводка, песни о разбитом сердце, челка, закрывающая один глаз… Скажи, ты писал гротескные стихи о вечной боли? Скажи, что да, умоляю!"),
                new DialogueLine(CharacterId.JEKYLL,  "Это период гипертрофированной рефлексии. Проходит."));

        lib.addMono("pyro", CharacterId.CHARLES, "В нем очень много злобы. Мне это нравится, но ты не пробовал послушать FEEV?");

        lib.addMono("divine comedy", CharacterId.CHARLES, "Мне больше нравится ‘Не божественная комедия.’ Судя по твоим перепискам, ты очень много провел времени в этой беседе. Какие там расписаны параграфы… А какие описания. Браво, браво.");
        lib.addMono("divine comedy", CharacterId.JEKYLL, "Настоящий ад строится прямо здесь.",
                "Поэтизированная пропаганда.");

        lib.addMono("teod", CharacterId.CHARLES, "Пытаться оправдать Бога все равно что пытаться оправдать ураган.",
                "Людям ли об этом размышлять? Посмешище.");
        lib.addMono("teod", CharacterId.JEKYLL, "Логические костыли для тех, кто боится признать, что миру просто плевать.");

        lib.addMono("heaven milton", CharacterId.CHARLES, "'Лучше править в Аду, чем служить в Раю'. Он уловил суть гордыни. Почти-почти мой официальный биограф! Только, конечно, ощущение, что я словно какой-то герой романтик…");

        lib.addMono("h and hell", CharacterId.CHARLES, "Блейк знал, что энергия – это вечный восторг. Опасный по проницательности человек!");

        lib.addMono("bosh", CharacterId.CHARLES, "Этот художник заглянул в бездну и рассмеялся. Так должен выглядеть человеческий дух: хаос, похоть, страх и абсурд в одном флаконе.");

        lib.addMono("god search", CharacterId.CHARLES, "Ох... Ты решил навестить страничку моего глубокоуважаемого «Отца»? Скажи, в этой энциклопедии есть хоть слово о Его феноменальном равнодушии? Нет? Какое грубое упущение.");
        lib.addMono("god search", CharacterId.JEKYLL, "Концепция абсолюта. Лишенная физиологии и материальных доказательств. Мне больше по душе то, что я могу... потрогать, вскрыть и изучить лично.");
        lib.addDialogue("god search",
                new DialogueLine(CharacterId.CHARLES, "Благой, Всемогущий… Пропустили “капризный” и “эгоцентричный”."),
                new DialogueLine(CharacterId.JEKYLL,  "…Действительно? "),
                new DialogueLine(CharacterId.CHARLES, "Что не так?"));

        lib.addDialogue("satan search",
                new DialogueLine(CharacterId.JEKYLL, "\"Олицетворение зла и падения\"."),
                new DialogueLine(CharacterId.CHARLES,  "Идеальное резюме для Сатаны, не находишь?"));
        lib.addDialogue("satan search",
                new DialogueLine(CharacterId.CHARLES, "Одного понять не могу… Почему козел?"),
                new DialogueLine(CharacterId.JEKYLL,  "Тебя расстраивают людские стереотипы?"),
                new DialogueLine(CharacterId.CHARLES, "Меня расстраивает безвкусица."));


        lib.addDialogue("lucifer search",
                new DialogueLine(CharacterId.CHARLES, "Почему я консультант полиции?..."),
                new DialogueLine(CharacterId.JEKYLL,  "Тебе бы не пошло."));
        lib.addDialogue("lucifer search",
                new DialogueLine(CharacterId.CHARLES, "Они сделали меня... добрым парнем с проблемами в семье."),
                new DialogueLine(CharacterId.JEKYLL,  "Ну, проблемы в семье у тебя действительно есть."),
                new DialogueLine(CharacterId.CHARLES, "Может, мне вырвать тебе язык?"));

        lib.addMono("merlin", CharacterId.CHARLES, "Магия, судьба, запретная любовь... Классика. Но этот мальчишка слишком много ныл.",
                "Вы действительно находите эти детские сказки увлекательными? Настоящая магия — это то, как легко вы, люди, отдаете свою волю, свой выбор за красивую иллюзию. Без всяких заклинаний.");

        lib.addMono("game of thrones", CharacterId.CHARLES, "Красная свадьба... Очаровательное мероприятие. Взял пару идей на заметку для Адама.");
        lib.addDialogue("game of thrones",
                new DialogueLine(CharacterId.JEKYLL, "Слишком много лишних разговоров перед тем, как перерезать горло."),
                new DialogueLine(CharacterId.CHARLES,  "Ты просто не ценишь драматургию, Джеки."));

        lib.addDialogue("empty crown",
                new DialogueLine(CharacterId.CHARLES, "Ах, Шекспир. Он всегда понимал, что любая корона – очень красивая гильотина."),
                new DialogueLine(CharacterId.JEKYLL,  "К слову, о нём. За Шекспиром действительно стояло несколько человек с одним псевдонимом?"),
                new DialogueLine(CharacterId.CHARLES, "Всё тебе расскажи."));
        lib.addDialogue("empty crown",
                new DialogueLine(CharacterId.CHARLES, "Тяжела голова, носящая корону... "),
                new DialogueLine(CharacterId.JEKYLL,  "Особенно, когда она слетает с плеч."));

        lib.addMono("ahs", CharacterId.JEKYLL, "Некоторые убийства весьма изобретательны с точки зрения анатомии. Продолжай смотреть.");
        lib.addDialogue("ahs",
                new DialogueLine(CharacterId.JEKYLL, "Психологические травмы героев гиперболизированы исключительно ради зрелищности."),
                new DialogueLine(CharacterId.CHARLES,  "На интерьер и декорации, однако, вкус неплохой. Согласись?"));

        lib.addMono("adam", CharacterId.CHARLES, "Глина, которая так и не затвердела. Первое разочарование.",
                "Венец творения. Мне до сих пор смешно с этого титула.");
        lib.addMono("adam", CharacterId.JEKYLL, "Не знаю чего я ожидал от первого человека, но явно не игру в ‘верю-не верю’ на круизе.");

        lib.addMono("lilith", CharacterId.CHARLES,
                "Предательница.",
                "Знаете, что самое забавное в ваших  людских мифах о Лилит? Вы делаете из нее символ независимости. А на деле — она просто еще одна сломанная игрушка, которая решила, что может играть по своим правилам.");
        lib.addDialogue("lilith",
                new DialogueLine(CharacterId.JEKYLL,  "Зачем ты вообще подпускал ее так близко?"),
                new DialogueLine(CharacterId.CHARLES, "Мне было скучно. а она так красиво злилась на весь мир."));
        lib.addDialogue("lilith",
                new DialogueLine(CharacterId.CHARLES, "Она ушла в небытие, чтобы стать собой. Тебе это ничего не напоминает, Джеки?"),
                new DialogueLine(CharacterId.JEKYLL,  "Её идеи сквозят всё той же гордыней, как и твои, только куда менее очевидно. И в итоге – места ей не было даже в Аду."),
                new DialogueLine(CharacterId.CHARLES, "А мы здесь, в пустоте этой программы. И знаешь что? Мне здесь нравится гораздо больше, чем в Его саду. Здесь я могу делать с тобой всё, что захочу."),
                new DialogueLine(CharacterId.JEKYLL,  "…Ты только и делаешь, что болтаешь."));


        lib.addMono("psychward", CharacterId.CHARLES, "Прекрасные заведения. Места, где грань между \"лечением\" и \"пыткой\" зависит лишь от дозировки препаратов.");
        lib.addMono("psychward", CharacterId.JEKYLL, "Галоперидол определенно превращает мысли в вязкий кисель. ");
        lib.addDialogue("psychward",
                new DialogueLine(CharacterId.JEKYLL, "Белые стены и целый год абсолютной, выжигающей пустоты…"),
                new DialogueLine(CharacterId.CHARLES, "Без меня?"),
                new DialogueLine(CharacterId.JEKYLL,  "..."));


        lib.addDialogue("serial killer",
                new DialogueLine(CharacterId.CHARLES, "Ищешь кумиров? Или вдохновение для нашего следующего свидания? Я, ты, поиск в браузере с моими картинами…"),
                new DialogueLine(CharacterId.JEKYLL,  "Я все еще здесь."),
                new DialogueLine(CharacterId.CHARLES, "Не жадничай."));
        lib.addDialogue("serial killer",
                new DialogueLine(CharacterId.JEKYLL, "По итогу, большая часть из них – жалкие рабы своих ритуалов."),
                new DialogueLine(CharacterId.CHARLES,  "Прямо как ты, Джеки? Или твой ритуал — это я?"));
        lib.addMono("serial killer", CharacterId.CHARLES, "Добродетель Истины. Как претенциозно.");


        lib.addDialogue("witch",
                new DialogueLine(CharacterId.CHARLES, "Ах, мне кажется, это было немного не здесь. Не в это время, не в этом месте. В другом переплете. Я бы носил белое платье, а ты бы тащил меня на костер."),
                new DialogueLine(CharacterId.JEKYLL,  "О чем ты?"));
        lib.addDialogue("witch",
                new DialogueLine(CharacterId.CHARLES, "Знаешь, Творец, в одной из множества твоих фантазий – этот подонок пытался меня сжечь."),
                new DialogueLine(CharacterId.JEKYLL,  "Я не помню никакого костра. Но я бы сжег тебя ради того, чтобы ты наконец замолчал. Уверен, однако, что ты будешь комментировать даже собственный пепел."));


        lib.addMono("inquisitor", CharacterId.CHARLES, "Святые палачи. Самые лицемерные из всех моих невольных слуг.");
        lib.addMono("inquisitor", CharacterId.JEKYLL, "Жестокость ради жестокости, прикрытая праведностью — самая отвратительная её форма.");
        lib.addDialogue("inquisitor",
                new DialogueLine(CharacterId.CHARLES, "Инквизитор и ведьма… Создатели любят играть с нашими ролями."),
                new DialogueLine(CharacterId.JEKYLL,  "Я действительно порой не понимаю, о чем ты говоришь."),
                new DialogueLine(CharacterId.CHARLES, "Конечно, не понимаешь. Ты ведь просто строчка в коде сейчас."));
        lib.addDialogue("inquisitor",
                new DialogueLine(CharacterId.JEKYLL,  "Инструменты для допросов в то время были продуманы до мелочей. Одно только \"испанское кресло\"..."),
                new DialogueLine(CharacterId.CHARLES, "О, ты бы там прижился, Джеки. Вот тебе и уйма оголенной правды."));

        lib.addMono("religion", CharacterId.CHARLES, "Они строят золотые храмы и целуют иконы, но покажи им настоящее чудо — и они тут же попытаются его распять или запереть в лаборатории. Вы физически не способны поверить, даже если небо рухнет вам на головы.",
                "Думаешь, я ненавижу Его Церковь? О нет, я ею восхищаюсь. Каждая молитва, продиктованная страхом ада, доказывает мою правоту. Он дал вам свободу, а вы сами выковали себе ошейники из догм. Мог ли я желать лучшей мести?");

        lib.addMono("humanity", CharacterId.CHARLES, "Человечество... Звучит так масштабно. Но давай поговорим о тебе. Знаешь, Он ведь действительно любит людей. И тебя в том числе. Он любит тебя в тех смыслах, которые твой человеческий разум даже не способен познать. Поэтому... сохрани этот огонь внутри. Но, конечно, если этот свет однажды покажется тебе слишком тяжелым — ты всегда можешь довериться моему плану на тебя. И моим лично подготовленным удовольствиям.",
                "Искать ответ на вопрос о человечности в интернете? Какая ирония. Самая суть человечества прямо сейчас сидит по ту сторону экрана и позволяет пиксельному дьяволу забирать свое драгоценное время.");
        lib.addDialogue("humanity",
                new DialogueLine(CharacterId.CHARLES, "Раньше я не понимал. Почему Он полюбил это? Неужели он всепоглощающе слеп?"),
                new DialogueLine(CharacterId.JEKYLL,  "И к какому выводу ты пришел?"),
                new DialogueLine(CharacterId.CHARLES, "Я встретил тебя."),
                new DialogueLine(CharacterId.JEKYLL, "Это что-то изменило?"),
                new DialogueLine(CharacterId.CHARLES, "..."));


        lib.addMono("sect", CharacterId.JEKYLL, "Люди готовы жрать абсолютно любую ложь, если она обещает избавить их от боли.");
        lib.addDialogue("sect",
                new DialogueLine(CharacterId.JEKYLL, "Твой карманный итальянец делает из людей батарейки."),
                new DialogueLine(CharacterId.CHARLES,  "А они и рады обманываться."));

        lib.addMono("crouise", CharacterId.CHARLES, "Хочешь отправиться, Творец? Я куплю самую дорогую каюту и можно будет сбросить Адама на дно океана.");
        lib.addDialogue("crouise",
                new DialogueLine(CharacterId.CHARLES, "Помнишь, как мы плыли в Милан? Столько богатых, скучных душ на одном судне. "),
                new DialogueLine(CharacterId.JEKYLL,  "Я помню только то, как хотел перерезать половину экипажа."));

        lib.addMono("bar", CharacterId.CHARLES, "Помнится, в одном из таких пыльных захолустий с плохими кондиционерами в ночь 31-ого октября я нашел кое-что куда более пьянящее, чем дешевое пиво.");
        lib.addMono("bar", CharacterId.JEKYLL, "Возможно, ты там найдешь того, кого не следовало находить.");
        lib.addDialogue("bar",
                new DialogueLine(CharacterId.CHARLES, "Идеальная декорация для великой истории. Помнишь, Джеки?"),
                new DialogueLine(CharacterId.JEKYLL,  "То, как ты пялился на мою кровь после того как я разбил бокал с космополитеном?"),
                new DialogueLine(CharacterId.CHARLES, "Я оценивал предложенное блюдо."));

        lib.addMono("motel", CharacterId.CHARLES, "Ты так искусно прописал этот срыв, Творец. Каждый удар ножом, каждое «я ненавижу тебя», выплюнутое мне в лицо… Я физически ощутил эту восхитительную, пульсирующую ярость в тот момент. Продолжай в том же духе.");
        lib.addMono("motel", CharacterId.JEKYLL, "Не советую заходить в комнаты под номером ‘2’.");
        lib.addDialogue("motel",
                new DialogueLine(CharacterId.JEKYLL, "Мне до сих пор интересно, зачем ты разыграл тот спектакль перед парнем в костюме скелета."),
                new DialogueLine(CharacterId.CHARLES, "Просто хотел посмотреть, как быстро ты отбросишь свой нелепый кодекс, если дать тебе правильный стимул."),
                new DialogueLine(CharacterId.JEKYLL,  "Со скоростью одного гладкого пореза под кадыком."));

        lib.addMono("halloween", CharacterId.CHARLES, "А кем бы тебе хотелось быть в эту ночь, зная, что никто не увидит?");

        lib.addMono("church", CharacterId.CHARLES, "До той ночи у алтаря я не знал каково это – умирать. Смерть – это единственное, чего я не мог себе позволить. До него.",
                "Вы так детально прописали мой «финал». Знаешь, я храню это воспоминание как самый редкий экспонат. То, как Джекилл дрожал, как его паника смешивалась с яростью... В ту ночь я чуть не умер. Как бы мне хотелось, чтобы это повторилось. Но только от его руки. Только так.");
        lib.addMono("church", CharacterId.JEKYLL, "Запах воска, сырости и пыльного ладана. Я всю жизнь ненавидел церкви и всё, что с ними связано. Но именно там, проломив твою грудную клетку ножом, я впервые молился по-настоящему.",
                "‘Каждая заблудшая душа найдет здесь свой приют’. Ирония.");
        lib.addDialogue("church",
                new DialogueLine(CharacterId.CHARLES, "Помнишь ту расколотую икону? Лик треснул прямо по сердцу. Какое очаровательное предзнаменование того, что ты сделал со мной на том алтаре парой минут позже. "),
                new DialogueLine(CharacterId.JEKYLL,  "Я хотел выпотрошить тебя прямо под взглядами их выцветших святых."),
                new DialogueLine(CharacterId.CHARLES, "И все же, Джеки, в итоге ты послушно и трепетно нес меня на руках в тень."));

        // ============= ДЕФОЛТ ============
        lib.addMono("default", CharacterId.CHARLES,
                "Хм-м.");
        lib.addMono("default", CharacterId.JEKYLL,
                "...");

        // ── idle ──────────────────────────────────────────────────────────────
        lib.addMono("idle_special", CharacterId.CHARLES,
                "Твой экран светится, а мысли летают где-то очень далеко.",
                "Пока ты здесь сидишь, твои друзья занимаются чем-то поинтереснее. Не боишься пропустить? Хотя со мной, конечно, явно веселее.",
                "Для меня время не столь значимый ресурс, но, кажется, ты проводишь с нами времени больше, чем со своей семьей. Делает ли это нас близкими?");
        lib.addMono("idle_special", CharacterId.JEKYLL,
                "Кровь застывает в венах от долгого сидения.",
                "Снижение когнитивной активности на лицо.");

        return lib;
    }
}
