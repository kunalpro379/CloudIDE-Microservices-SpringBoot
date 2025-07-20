package org.example.codeeditorservice.entities;
import lombok.AllArgsConstructor;
import lombok.Data;
@Data
@AllArgsConstructor
public class WebSocketSession{
    private String username;
    private String docId;
}