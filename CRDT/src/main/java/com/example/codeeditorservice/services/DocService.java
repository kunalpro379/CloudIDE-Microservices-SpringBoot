package com.example.codeeditorservice.services;

import com.example.codeeditorservice.Dto.DocTitleDTO;
import com.example.codeeditorservice.Dto.DocumentChangeDTO;
import com.example.codeeditorservice.Dto.UserDocDTO;
import com.example.codeeditorservice.Dto.DocumentDTO;

import java.util.List;

public interface DocService {
    DocumentDTO createDoc(DocTitleDTO title);

    Long deleteDoc(Long id);

    String updateDocTitle(Long id, DocTitleDTO documentDTO);

    UserDocDTO addUser(Long id, UserDocDTO userDoc);

    List<UserDocDTO> getSharedUsers(Long id);

    String removeUser(Long id, UserDocDTO userDoc);

    String updatePermission(Long id, UserDocDTO userDoc);

    List<DocumentDTO> getAllDocs();

    List<DocumentChangeDTO> getDocChanges(Long id);

    DocumentDTO getDoc(Long id);
}