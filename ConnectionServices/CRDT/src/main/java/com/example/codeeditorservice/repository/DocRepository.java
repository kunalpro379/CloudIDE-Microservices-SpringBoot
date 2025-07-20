package org.example.codeeditorservice.repository;
import org.example.codeeditorservice.entities.Doc;

import org.swing.text.html.Option;
@Repository
public interface DocRepository extends JpaRepository<Doc, Long>{
    List<Doc>findByOwner(User owner);
    Optional<Doc>findByIdAndOwner(Long id, User owner);
    List<Doc>findByOwner_Username(String username);
    Optional<Doc>findByIdAndOwner_Username(Long id, String username);
//    @Query("select d from Doc d where d.owner.username = ?1 or d in (select ud.doc from UserDoc ud where ud.user.username = ?1)")
//    List<Doc>findByUsername(String username);
    @Query(
            value = """
        SELECT d.* 
        FROM doc d
        WHERE d.owner_id = (SELECT u.id FROM user u WHERE u.username = :username)
        OR d.id IN (
            SELECT ud.doc_id 
            FROM user_doc ud
            JOIN user u ON ud.user_id = u.id
            WHERE u.username = :username
        )
    """,
            nativeQuery = true
    )
    List<Doc> findAccessibleDocuments(@Param("username") String username);
    Optional<Doc>getDocById(Long id);
}