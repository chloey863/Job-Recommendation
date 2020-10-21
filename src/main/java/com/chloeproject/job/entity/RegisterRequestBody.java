package com.chloeproject.job.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RegisterRequestBody {

    @JsonProperty("user_id")
    public String userId;

    public String password;

    @JsonProperty("first_name")
    public String firstName;

    @JsonProperty("last_name")
    public String lastName;
}
