package com.example.codeeditorservice.Dto;

import lombok.Data;
import java.util.List;

@Data
public class ActiveUser {
    private List<String> usernames;
}