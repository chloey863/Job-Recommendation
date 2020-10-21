package com.chloeproject.job.external;

import com.chloeproject.job.entity.Item;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

public class GitHubClient {
    // define two static strings:
    // the url template to fetch result from GitHub Job API
    private static final String URL_TEMPLATE = "https://jobs.github.com/positions.json?description=%s&lat=%s&long=%s";

    // a default keyword for job search
    private static final String DEFAULT_KEYWORD = "developer";


    /**
     * Search the job related to "keyword" and based on user's geolocation (lat, lon)
     *
     * @param lat     latitude of user's geolocation
     * @param lon     longitude of user's geolocation
     * @param keyword keywords for job search
     * @return a list of items of the search results
     */
    public List<Item> search(double lat, double lon, String keyword) {
        if (keyword == null) {
            keyword = DEFAULT_KEYWORD;
        }

        try {
            // encode for handling the edge case: such as “hello word” is encoded
            // to “hello%20word” to be a valid request
            keyword = URLEncoder.encode(keyword, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String url = String.format(URL_TEMPLATE, keyword, lat, lon);

        CloseableHttpClient httpClient = HttpClients.createDefault();

        // Create a custom response handler to handle response
        // responseHandler will be called by httpClient, (check the "execute" source code)
        ResponseHandler<List<Item>> responseHandler = response -> {
            // edge case1: error status code
            if (response.getStatusLine().getStatusCode() != 200) {
                return Collections.emptyList();
            }
            // edge case2: entity is null
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                return Collections.emptyList();
            }

            // general case: entity is not null and status code is ok/200
            ObjectMapper mapper = new ObjectMapper();

            // use mapper to read value of entity's content, by following the format of Item array (Item[])
            Item[] itemArr = mapper.readValue(entity.getContent(), Item[].class);
            List<Item> items = Arrays.asList(itemArr);

            // to avoid used up MonkeyLearn quota:
            if (items.size() > 6) {
                items = items.subList(0, 6);
            }

            // before the search returned results, extract the keywords for the job description
            extractKeywords(items);
            return items;

        };

        // execute the request with the customized responseHandler
        try {
            return httpClient.execute(new HttpGet(url), responseHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    /**
     * Perform the keyword extraction using a monkeyLearnClient
     * @param items list of items
     */
    private void extractKeywords(List<Item> items) {
        MonkeyLearnClient monkeyLearnClient = new MonkeyLearnClient();

        // step 1: convert the descriptions(articles) to a list of string
        List<String> descriptions = new ArrayList<>();
        for (Item item : items) {
            descriptions.add(item.getDescription());
        }
        // also collect titles
        List<String> titles = new ArrayList<>();
        for(Item item : items) {
            titles.add(item.getTitle());
        }

        // step2: get the keyword extraction result from MonkeyLearnClient
        List<Set<String>> keywordList = monkeyLearnClient.extract(descriptions);
        // in case keywordList from descriptions is empty, also try extract keyword from title
        if (keywordList.isEmpty()) {
            keywordList = monkeyLearnClient.extract(titles);
        }

        // step3: add the "keywords" field as key to the response JSON body of each item
        for (int i = 0; i < items.size(); i++) {
            items.get(i).setKeywords(keywordList.get(i));
        }
    }
}