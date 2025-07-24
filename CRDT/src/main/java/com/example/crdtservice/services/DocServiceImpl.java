package com.example.codeeditorservice.services;

import com.example.codeeditorservice.Dto.DocTitleDTO;
import com.example.codeeditorservice.Dto.DocumentChangeDTO;
import com.example.codeeditorservice.Dto.UserDocDTO;
import com.example.codeeditorservice.Dto.DocumentDTO;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DocServiceImpl implements DocService {
     @Override
     public DocumentDTO createDoc(DocTitleDTO title) {
          // TODO: Implement
          return null;
     }

     @Override
     public Long deleteDoc(Long id) {
          // TODO: Implement
          return null;
     }

     @Override
     public String updateDocTitle(Long id, DocTitleDTO documentDTO) {
          // TODO: Implement
          return null;
     }

     @Override
     public UserDocDTO addUser(Long id, UserDocDTO userDoc) {
          // TODO: Implement
          return null;
     }

     @Override
     public List<UserDocDTO> getSharedUsers(Long id) {
          // TODO: Implement
          return null;
     }

     @Override
     public String removeUser(Long id, UserDocDTO userDoc) {
          // TODO: Implement
          return null;
     }

     @Override
     public String updatePermission(Long id, UserDocDTO userDoc) {
          // TODO: Implement
          return null;
     }

     @Override
     public List<DocumentDTO> getAllDocs() {
          // TODO: Implement
          return null;
     }

     @Override
     public List<DocumentChangeDTO> getDocChanges(Long id) {
          // TODO: Implement
          return null;
     }

     @Override
     public DocumentDTO getDoc(Long id) {
          // TODO: Implement
          return null;
     }
}
