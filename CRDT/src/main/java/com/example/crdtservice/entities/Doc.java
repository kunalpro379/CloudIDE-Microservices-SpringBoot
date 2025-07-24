package com.example.codeeditorservice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Doc {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "username", referencedColumnName = "username")
    private User owner;
    private String name;
    private String title;
    @Lob
    @Column(name = "content")
    private byte[] content;
    @OneToMany(mappedBy = "doc")
    private List<UserDocs> sharedWith;
}