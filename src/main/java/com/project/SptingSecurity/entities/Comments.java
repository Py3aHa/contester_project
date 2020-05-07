package com.project.SptingSecurity.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "comments")
public class Comments {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER )
    private Users author;

    @ManyToOne(fetch = FetchType.EAGER)
    private NewPosts newsPost;

    @ManyToOne(fetch = FetchType.EAGER)
    private Lessons lesson;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "comment")
    private String comment;

    @Column(name = "postDate")
    private Date postDate;

}
