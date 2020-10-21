package com.chloeproject.job.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

// Since we do not need all the properties in the response, so mark @JsonIgnoreProperties.
@JsonIgnoreProperties(ignoreUnknown = true)
public class Extraction {

    @JsonProperty("tag_name")
    public String tagName;

    @JsonProperty("parsed_value")
    public String parsedValue; // parsed_value is the keywords returned from Monkey Learn API

    public int count;

    public String relevance;
}
