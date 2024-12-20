package ru.shift.protocol;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "status")
@JsonSubTypes({
        @JsonSubTypes.Type(value = LoginResponse.class, name = "LOGIN"),
        @JsonSubTypes.Type(value = MessageResponse.class, name = "MESSAGE"),
        @JsonSubTypes.Type(value = OnlineUsersUpdate.class, name = "USERS_STATUS")
})
public abstract class Response {
}


