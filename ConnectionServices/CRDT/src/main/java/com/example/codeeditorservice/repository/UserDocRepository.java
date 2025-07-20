package org.example.codeeditorservice.repository;
import jakarta.transaction.Transactional;
import org.cce.backend.entity.Doc;
import org.cce.backend.entity.UserDoc;
import org.cce.backend.entity.UserDocId;
import org.cce.backend.enums.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface UserDocRepository extends JpaRepository<UserDoc, UserDocId>{
    @Query(
            'SELECT ud FROM UserDoc ud WHERE ud.user.username=?1'
    )
    List<Doc>getDocsByUser_Username(String username);
    @Modifying
    @Transactional
    @Query(
            """DELETE FROM UserDoc ud WHERE ud.userDocId.username=?1
                    AND ud.userDocId.docId=?2 
                    AND (SELECT d.owner.username FROM Doc d WHERE d.id = ud.userDocId.docId) = ?1
            """
    )
    int deleteUserDocBy(String username, Long docId, String owner);

    @Modifying
    @transactional
    @Query(
            """
                    UPDATE UserDoc ud SET ud.permission=?3
                    WHERE ud.userDocId.username=?1 
                    AND ud.userDocId.docId=?2
                    AND (SELECT d.owner.username FROM Doc d WHERE d.id =?2) = ?3
                    """
    )
    int updateUserDocBy(String username, Long docId, String owner,Permission permission);
    
}