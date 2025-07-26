package com.donorbox.backend.dto;

import com.donorbox.backend.entity.Blog;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlogRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String subtitle;

    @NotBlank(message = "Slug is required")
    private String slug;

    @NotBlank(message = "Content is required")
    private String content;

    private String excerpt;

    private String featuredImage;

    @NotBlank(message = "Author is required")
    private String author;

    private String authorEmail;

    @NotNull(message = "Status is required")
    private Blog.BlogStatus status;

    private Integer readingTime;

    private String tags;

    private String metaTitle;

    private String metaDescription;

    @Builder.Default
    private Boolean isFeatured = false;

    @Builder.Default
    private Boolean allowComments = true;
}
