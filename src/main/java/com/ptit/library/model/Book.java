package com.ptit.library.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Books")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Book implements Comparable<Book> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "book_code", length = 50)
    private String bookCode;

    @Column(name = "title", length = 200)
    private String title;

    @Column(name = "author", length = 100)
    private String author;

    @Column(name = "published_year")
    private Integer publishedYear;

    @Column(name = "total_copies")
    private Integer totalCopies;

    @Column(name = "copies_available")
    private Integer copiesAvailable;

    @Column(name = "publisher", length = 100)
    private String publisher;

    @Column(name = "isbn", length = 20)
    private String isbn;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "cover_image")
    private String coverImage;

    @Override
    public int compareTo(Book o) {
        return this.author.compareTo(o.author);
    }
}
