package com.chloeproject.job.external;

import com.chloeproject.job.entity.ExtractRequestBody;
import com.chloeproject.job.entity.ExtractResponseItem;
import com.chloeproject.job.entity.Extraction;
import com.chloeproject.job.env.MonkeyLearnClientUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * MonkeyLearnClient will do the keyword extraction by using Monkey Learn API
 */
public class MonkeyLearnClient {
    /**
     * Extract the keywords
     * @param articles a list of strings (one string is one article)
     * @return list of keywords (Set<String> is a set of string/keywords), a list corresponds to the
     *         keywords list of one article
     */
    public List<Set<String>> extract(List<String> articles) {
        ObjectMapper mapper = new ObjectMapper();
        CloseableHttpClient httpClient = HttpClients.createDefault();

        HttpPost request = new HttpPost(MonkeyLearnClientUtil.EXTRACT_URL);
        request.setHeader("Content-type", "application/json");
        request.setHeader("Authorization", "Token " + MonkeyLearnClientUtil.AUTH_TOKEN);
        ExtractRequestBody body = new ExtractRequestBody(articles, 3);

        // map the ExtractRequestBody body to JSON string
        String stringJsonBody;
        try {
            stringJsonBody = mapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }

        // add the request jsonBody into entity object (from example)
        try {
            request.setEntity(new StringEntity(stringJsonBody));
        } catch (UnsupportedEncodingException e) {
            return Collections.emptyList();
        }

        // Create a custom response handler to handle response
        // responseHandler will be called by httpClient
        ResponseHandler<List<Set<String>>> responseHandler = response -> {
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
            // results = an array of items (each item is an article responded by the request)
            ExtractResponseItem[] results = mapper.readValue(entity.getContent(), ExtractResponseItem[].class);

            List<Set<String>> keywordList = new ArrayList<>();
            for (ExtractResponseItem result : results) {
                Set<String> keywords = new HashSet<>();
                for (Extraction extraction : result.extractions) {
                    keywords.add(extraction.parsedValue);
                }
                keywordList.add(keywords);
            }
            return keywordList;
        };

        // execute the request with the customized responseHandler
        try {
            return httpClient.execute(request, responseHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    // a main function to test if the extract() function works as expected
    public static void main(String[] args) {
        List<String> articles = Arrays.asList(
                "Elon Musk has shared a photo of the spacesuit designed by SpaceX. This is the second image shared of the new design and the first to feature the spacesuit’s full-body look.",
                "Former Auburn University football coach Tommy Tuberville defeated ex-US Attorney General Jeff Sessions in Tuesday nights runoff for the Republican nomination for the U.S. Senate. ",
                "The NEOWISE comet has been delighting skygazers around the world this month – with photographers turning their lenses upward and capturing it above landmarks across the Northern Hemisphere."
        );

        MonkeyLearnClient client = new MonkeyLearnClient();

        List<Set<String>> keywordList = client.extract(articles);
        System.out.println(keywordList);
    }

}
