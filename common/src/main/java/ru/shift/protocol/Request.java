package ru.shift.protocol;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = LoginRequest.class, name = "LOGIN"),
        @JsonSubTypes.Type(value = MessageRequest.class, name = "MESSAGE")
})
public abstract class Request {
}



