package com.example.codeeditorservice.services;

import com.example.codeeditorservice.entities.Doc;
import com.example.codeeditorservice.entities.User;
import com.example.codeeditorservice.entities.UserDocs;
import com.example.codeeditorservice.enums.Permission;
import com.example.codeeditorservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DocAuthorizationServiceImpl implements DocAuthorizationService {
    private final UserRepository userRepository;

    private User getCurrentUser() {
        // Implement logic to get current user from security context
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean canAccess(String docId) {
        User user = getCurrentUser();
        return user.getAccessDocs().stream().anyMatch(userDoc -> userDoc.getDoc().getId().toString().equals(docId));
    }

    @Override
    public boolean canEdit(String username, Doc doc) {
        return doc.getOwner().getUsername().equals(username) || doc.getSharedWith().stream()
                .anyMatch(userDoc -> userDoc.getUser().getUsername().equals(username)
                        && userDoc.getPermission().equals(Permission.EDIT));
    }

    @Override
    public boolean fullAccess(String username, Doc doc) {
        return doc.getOwner().getUsername().equals(username);
    }
}