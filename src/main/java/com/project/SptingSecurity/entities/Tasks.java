package com.project.SptingSecurity.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.springframework.boot.autoconfigure.web.ConditionalOnEnabledResourceChain;

import javax.persistence.*;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tasks")
public class Tasks {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title")
    private String title;

    @ManyToOne(fetch = FetchType.EAGER)
    private TaskCategories categories;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "description")
    private String description;

    @Column(name = "img")
    private String file;

    @Column(name = "input")
    private String input;

    @Column(name = "output")
    private String output;

    @Column(name = "inputTest")
    private String inputFile;

    @Column(name = "outputTest")
    private String outputFile;

    @Column(name = "level")
    private int level;

    @ManyToOne(fetch = FetchType.EAGER)
    private Users author;
}
