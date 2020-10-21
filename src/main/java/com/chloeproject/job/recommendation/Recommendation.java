package com.chloeproject.job.recommendation;


import com.chloeproject.job.db.MySQLConnection;
import com.chloeproject.job.entity.Item;
import com.chloeproject.job.external.GitHubClient;

import java.util.*;

public class Recommendation {
    /**
     * Implementation of content-based recommendation algorithm.
     *
     */
    public List<Item> recommendItems(String userId, double lat, double lon) {
        List<Item> recommendedItems = new ArrayList<>();

        // Step1: get all already favorited itemIds from MySQLConnection
        MySQLConnection connection = new MySQLConnection();
        Set<String> favoritedItemIds = connection.getFavoriteItemIds(userId);

        // Step2: get all keywords, sort by count from MySQLConnection
        // {"software engineer": 6, "backend": 4, "san francisco": 3, "remote": 1}
        Map<String, Integer> allKeywords = new HashMap<>();
        for (String itemId : favoritedItemIds) {
            Set<String> keywords = connection.getKeywords(itemId);
            for (String keyword : keywords) {
                allKeywords.put(keyword, allKeywords.getOrDefault(keyword, 0) + 1);
            }
        }
        connection.close();

        // store the keywords in list
        List<Map.Entry<String, Integer>> keywordList = new ArrayList<>(allKeywords.entrySet());
        // then sorted based on their count (lambda)
        keywordList.sort((Map.Entry<String, Integer> e1, Map.Entry<String, Integer> e2) ->
                Integer.compare(e2.getValue(), e1.getValue()));

        // pre-processing: cut down search list only top 3
        if (keywordList.size() > 3) {
            keywordList = keywordList.subList(0, 3); // exclusive end index
        }

        // Step3: search based on keywords, filter out favorite items
        Set<String> visitedItemIds = new HashSet<>();
        GitHubClient client = new GitHubClient();

        for (Map.Entry<String, Integer> keyword : keywordList) {
            List<Item> items = client.search(lat, lon, keyword.getKey());

            for (Item item : items) {
                // check if a to-be-recommended item id is already added to set, to avoid duplication
                if (!favoritedItemIds.contains(item.getId()) && !visitedItemIds.contains(item.getId())) {
                    recommendedItems.add(item);
                    visitedItemIds.add(item.getId());
                }
            }
        }
        return recommendedItems;
    }
}