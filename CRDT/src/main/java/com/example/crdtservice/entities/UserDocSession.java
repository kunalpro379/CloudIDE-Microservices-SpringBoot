package com.example.codeeditorservice.entities;

import lombok.Data;
import com.example.codeeditorservice.enums.Permission;

@Data
public class UserDocSession {
    String docId;
    Permission permission;
}