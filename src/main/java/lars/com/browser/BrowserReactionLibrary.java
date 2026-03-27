package lars.com.browser;

import java.util.*;

public class BrowserReactionLibrary {
    private final Map<String, String> keywordToCategory = new HashMap<>();

    public BrowserReactionLibrary() {
        initializeKeywords();
    }

    private void initializeKeywords() {
        keywordToCategory.put("матча", "matcha");
        keywordToCategory.put("anatomy", "anatomy");
        keywordToCategory.put("анатомия", "anatomy");
        keywordToCategory.put("искусство", "art");
        keywordToCategory.put("эмо", "emo");
        keywordToCategory.put("emo", "emo");
        keywordToCategory.put("pyrokinesis", "pyro");
        keywordToCategory.put("пирокинезис", "pyro");
        keywordToCategory.put("divine comedy", "divine comedy");
        keywordToCategory.put("божественная комедия", "divine comedy");
        keywordToCategory.put("теодицея", "teod");
        keywordToCategory.put("мильтон потерянный рай", "heaven milton");
        keywordToCategory.put("бракосочетание рая и ада", "h and hell");
        keywordToCategory.put("иероним босх сад земных наслаждений", "bosh");
        keywordToCategory.put("бог", "god search");
        keywordToCategory.put("god", "god search");
        keywordToCategory.put("сериал люцифер", "lucifer search");
        keywordToCategory.put("люцифер", "satan search");
        keywordToCategory.put("lucifer", "satan search");
        keywordToCategory.put("сатана", "satan search");
        keywordToCategory.put("satan", "satan search");
        keywordToCategory.put("мерлин", "merlin");
        keywordToCategory.put("game of thrones", "game of thrones");
        keywordToCategory.put("игра престолов", "game of thrones");
        keywordToCategory.put("пустая корона", "empty crown");
        keywordToCategory.put("американская история ужасов", "ahs");
        keywordToCategory.put("american horror story", "ahs");
        keywordToCategory.put("adam", "adam");
        keywordToCategory.put("адам", "adam");
        keywordToCategory.put("лилит",  "lilith");
        keywordToCategory.put("lilith", "lilith");
        keywordToCategory.put("психиатрическая больница", "psychward");
        keywordToCategory.put("психбольница", "psychward");
        keywordToCategory.put("serial killer", "serial killer");
        keywordToCategory.put("серийный убийца", "serial killer");
        keywordToCategory.put("ведьма", "witch");
        keywordToCategory.put("witch", "witch");
        keywordToCategory.put("инквизитор", "inquisitor");
        keywordToCategory.put("inquisitor", "inquisitor");
        keywordToCategory.put("religion", "religion");
        keywordToCategory.put("религия", "religion");
        keywordToCategory.put("humanity", "humanity");
        keywordToCategory.put("человечество", "humanity");
        keywordToCategory.put("sect", "sect");
        keywordToCategory.put("секта", "sect");
        keywordToCategory.put("круиз", "crouise");
        keywordToCategory.put("bar", "bar");
        keywordToCategory.put("бар", "bar");
        keywordToCategory.put("motel", "motel");
        keywordToCategory.put("мотель", "motel");
        keywordToCategory.put("halloween", "halloween");
        keywordToCategory.put("хеллоуин", "halloween");
        keywordToCategory.put("хэллоуин", "halloween");
        keywordToCategory.put("заброшенная церковь", "church");

    }

    // Поиск по запросу → категория
    public String getCategoryBySearchQuery(String query) {
        if (query == null || query.isEmpty()) return null;
        String lower = query.toLowerCase();

        for (Map.Entry<String, String> entry : keywordToCategory.entrySet()) {
            if (lower.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    public String getCategoryBySite(String siteCategory) {
        if (siteCategory == null || siteCategory.equals("unknown")) return null;
        return siteCategory;
    }
}