package com.example.codeeditorservice.services;
import com.example.codeeditorservice.entities.Doc;

public interface DocAuthorizationService  {
    boolean canAccess(String docId);
    boolean canEdit(String username, Doc doc);
    boolean fullAccess(String username, Doc doc);
}