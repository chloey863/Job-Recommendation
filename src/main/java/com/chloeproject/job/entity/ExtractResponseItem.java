package com.chloeproject.job.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Extract keywords from Response JSON Body
 */
// Since we do not need all the properties in the response, so mark @JsonIgnoreProperties.
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExtractResponseItem {

    public List<Extraction> extractions;
}
