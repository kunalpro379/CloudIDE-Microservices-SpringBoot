package com.example.codeeditorservice.repository;

import com.example.codeeditorservice.entities.Doc;
import com.example.codeeditorservice.entities.UserDocId;
import com.example.codeeditorservice.entities.UserDocs;
import com.example.codeeditorservice.enums.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;
import java.util.List;

@Repository
public interface UserDocRepository extends JpaRepository<UserDocs, UserDocId> {
        @Query("SELECT ud.doc FROM UserDocs ud WHERE ud.user.username = ?1")
        List<Doc> getDocsByUser_Username(String username);

        @Modifying
        @Transactional
        @Query("""
            DELETE FROM UserDocs ud
            WHERE ud.userDocId.username = ?1
              AND ud.userDocId.documentId = ?2
              AND (SELECT d.owner.username FROM Doc d WHERE d.id = ud.userDocId.documentId) = ?3
        """)
        int deleteUserDocBy(String username, Long documentId, String owner);

        @Modifying
        @Transactional
        @Query("""
            UPDATE UserDocs ud
            SET ud.permission = ?4
            WHERE ud.userDocId.username = ?1
              AND ud.userDocId.documentId = ?2
              AND (SELECT d.owner.username FROM Doc d WHERE d.id = ud.userDocId.documentId) = ?3
        """)
        int updateUserDocBy(String username, Long documentId, String owner, Permission permission);
}