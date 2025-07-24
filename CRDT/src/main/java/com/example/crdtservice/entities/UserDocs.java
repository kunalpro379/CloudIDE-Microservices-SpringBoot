package com.example.codeeditorservice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import com.example.codeeditorservice.enums.Permission;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class UserDocs {
    @EmbeddedId
    private UserDocId userDocId;
    @MapsId("username")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "username", referencedColumnName = "username")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @MapsId("documentId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "docId", referencedColumnName = "id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Doc doc;
    private Permission permission;
}