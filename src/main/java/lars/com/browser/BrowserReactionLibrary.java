package lars.com.browser;

import java.util.*;

public class BrowserReactionLibrary {
    private final Map<String, String> keywordToCategory = new HashMap<>();

    public BrowserReactionLibrary() {
        initializeKeywords();
    }

    private void initializeKeywords() {
        keywordToCategory.put("ведьма", "witch search");
        keywordToCategory.put("witch", "witch search");
        keywordToCategory.put("бог", "God search");
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

    // Категория сайта из background.js → категория для DualResponseLibrary
    // Просто пробрасываем как есть (youtube → youtube, discord → discord)
    public String getCategoryBySite(String siteCategory) {
        if (siteCategory == null || siteCategory.equals("unknown")) return null;
        return siteCategory;
    }
}