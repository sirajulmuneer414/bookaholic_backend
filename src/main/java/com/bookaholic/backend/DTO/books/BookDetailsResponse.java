package com.bookaholic.backend.DTO.books;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookDetailsResponse {
    private Long id;

    private String title;

    private String author;

    private String isbn;

    private Integer totalCopies;

    private Integer availableCopies;

    private String imageUrl; // Cloudinary URL
}
