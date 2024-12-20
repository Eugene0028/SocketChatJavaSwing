package ru.shift;

import lombok.*;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message {
    private String sender;
    private String message;
    private Timestamp timestamp;
}

