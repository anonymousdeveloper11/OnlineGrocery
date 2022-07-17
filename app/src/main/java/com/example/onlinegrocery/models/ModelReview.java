package com.example.onlinegrocery.models;

public class ModelReview {
    String uid, rating, review, timestamp;

    public ModelReview() {
    }

    public ModelReview(String uid, String rating, String review, String timestamp) {
        this.uid = uid;
        this.rating = rating;
        this.review = review;
        this.timestamp = timestamp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
