@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class UserDocs{
    @EmbeddedId  //--> composite pk (user+document)
    private UserDocId userDocId;
    @MapsId("username")
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="username", referencedColumnName="username")
    @OnDelete(action=OnDeleteAction.CASCADE) //--> if user is deleted, all his documents will be deleted
    private User user;


    @MapsId("documentId")
    @ManyToOne(fetch=FetchType.LAZY)
    //when you fetch UserDocs, User isn’t immediately fetched; it's loaded on-demand.
    @JoinColumn(name="docId", referencedColumnName="id")
    @OnDelete(action=OnDeleteAction.CASCADE) //
    private Doc doc;
    private Permission permission;



}