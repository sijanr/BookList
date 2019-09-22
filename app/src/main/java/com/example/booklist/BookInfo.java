package com.example.booklist;

public final class BookInfo {

    private String bookName;
    private String author;
    private String imageURL;
    private float rating;
    private String publisher;
    private String publishedDate;
    private int pageCount;

    public BookInfo(String bookName, String author, String imageURL, float rating, String publisher, String publishedDate, int pageCount) {
        this.bookName = bookName;
        this.author = author;
        this.imageURL = imageURL;
        this.rating = rating;
        this.publisher = publisher;
        this.publishedDate = publishedDate;
        this.pageCount = pageCount;
    }

    public String getBookName() {
        return bookName;
    }

    public String getAuthor() {
        return author;
    }

    public String getImageURL() {
        return imageURL;
    }

    public float getRating() {
        return rating;
    }

    public String getPublisher() {
        return publisher;
    }

    public String getPublishedDate() {
        return publishedDate;
    }

    public int getPageCount() {
        return pageCount;
    }
}
