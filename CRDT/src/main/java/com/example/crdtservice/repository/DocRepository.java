package com.example.codeeditorservice.repository;

import com.example.codeeditorservice.entities.Doc;
import com.example.codeeditorservice.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocRepository extends JpaRepository<Doc, Long> {
    List<Doc> findByOwner(User owner);

    Optional<Doc> findByIdAndOwner(Long id, User owner);

    List<Doc> findByOwner_Username(String username);

    Optional<Doc> findByIdAndOwner_Username(Long id, String username);

    @Query(value = """
                SELECT d.*
                FROM doc d
                WHERE d.owner_id = (SELECT u.id FROM user u WHERE u.username = :username)
                OR d.id IN (
                    SELECT ud.doc_id
                    FROM user_doc ud
                    JOIN user u ON ud.user_id = u.id
                    WHERE u.username = :username
                )
            """, nativeQuery = true)
    List<Doc> findAccessibleDocuments(@Param("username") String username);

    Optional<Doc> getDocById(Long id);
}