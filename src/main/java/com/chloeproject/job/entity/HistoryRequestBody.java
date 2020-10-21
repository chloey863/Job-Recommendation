package com.chloeproject.job.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * HistoryRequestBody is used for making POST requests.
 */
public class HistoryRequestBody {

    @JsonProperty("user_id")
    public String userId;

    public Item favorite;
}

