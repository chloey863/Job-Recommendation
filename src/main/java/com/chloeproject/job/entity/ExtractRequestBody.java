package com.chloeproject.job.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Extract keywords from Request JSON Body
 * */
public class ExtractRequestBody {

    @JsonProperty("data")
    public List<String> data;

    @JsonProperty("max_keywords")
    public int maxKeywords; // only using the max_keywords API from Monkey Learn

    public ExtractRequestBody(List<String> data, int maxKeywords) {
        this.data = data;
        this.maxKeywords = maxKeywords;
    }

}
