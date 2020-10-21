package com.chloeproject.job.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LoginResponseBody {

    public String status;

    @JsonProperty("user_id")
    public String userId;

    public String name;

    /**
     * Default constructor (if not write out specifically,
     * will be override by defined one). Because Jackson need to use
     * default constructor, so need to write out both.
     */
    public LoginResponseBody() {
    }

    /**
     * Defined constructor.
     */
    public LoginResponseBody(String status, String userId, String name) {
        this.status = status;
        this.userId = userId;
        this.name = name;
    }
}
