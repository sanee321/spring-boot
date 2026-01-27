package com.example.reviewservice.repository;

import com.example.reviewservice.entity.Review;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class ReviewRepositoryTest {

    @Autowired
    private ReviewRepository reviewRepository;

    @Test
    void testSaveReview() {
        Review review = new Review();
        review.setProductId(1L);
        review.setUserId(1L);
        review.setRating(5);
        review.setComment("Excellent product!");

        Review saved = reviewRepository.save(review);

        assertNotNull(saved.getId());
        assertEquals(1L, saved.getProductId());
        assertEquals(1L, saved.getUserId());
        assertEquals(5, saved.getRating());
    }

    @Test
    void testFindByProductId() {
        Review review = new Review();
        review.setProductId(2L);
        review.setUserId(1L);
        review.setRating(4);
        review.setComment("Good product");

        reviewRepository.save(review);

        List<Review> found = reviewRepository.findByProductId(2L);

        assertFalse(found.isEmpty());
        assertEquals(2L, found.get(0).getProductId());
    }

    @Test
    void testFindByUserId() {
        Review review = new Review();
        review.setProductId(3L);
        review.setUserId(2L);
        review.setRating(3);
        review.setComment("Average product");

        reviewRepository.save(review);

        List<Review> found = reviewRepository.findByUserId(2L);

        assertFalse(found.isEmpty());
        assertEquals(2L, found.get(0).getUserId());
    }

    @Test
    void testUpdateReview() {
        Review review = new Review();
        review.setProductId(4L);
        review.setUserId(3L);
        review.setRating(5);
        review.setComment("Original comment");

        Review saved = reviewRepository.save(review);
        Long reviewId = saved.getId();

        saved.setRating(4);
        saved.setComment("Updated comment");
        reviewRepository.save(saved);

        Optional<Review> updated = reviewRepository.findById(reviewId);

        assertTrue(updated.isPresent());
        assertEquals(4, updated.get().getRating());
        assertEquals("Updated comment", updated.get().getComment());
    }

    @Test
    void testDeleteReview() {
        Review review = new Review();
        review.setProductId(5L);
        review.setUserId(4L);
        review.setRating(3);
        review.setComment("Delete test");

        Review saved = reviewRepository.save(review);
        Long reviewId = saved.getId();

        reviewRepository.deleteById(reviewId);

        Optional<Review> deleted = reviewRepository.findById(reviewId);
        assertFalse(deleted.isPresent());
    }

    @Test
    void testFindAll() {
        Review review1 = new Review();
        review1.setProductId(6L);
        review1.setUserId(5L);
        review1.setRating(5);

        Review review2 = new Review();
        review2.setProductId(7L);
        review2.setUserId(6L);
        review2.setRating(4);

        reviewRepository.save(review1);
        reviewRepository.save(review2);

        var all = reviewRepository.findAll();
        assertTrue(all.size() >= 2);
    }

    @Test
    void testMultipleReviewsForSameProduct() {
        Review review1 = new Review();
        review1.setProductId(8L);
        review1.setUserId(7L);
        review1.setRating(5);

        Review review2 = new Review();
        review2.setProductId(8L);
        review2.setUserId(8L);
        review2.setRating(4);

        reviewRepository.save(review1);
        reviewRepository.save(review2);

        List<Review> reviews = reviewRepository.findByProductId(8L);
        assertEquals(2, reviews.size());
        assertEquals(8L, reviews.get(0).getProductId());
    }

    @Test
    void testReviewRatingRange() {
        Review lowRating = new Review();
        lowRating.setProductId(9L);
        lowRating.setUserId(9L);
        lowRating.setRating(1);

        Review highRating = new Review();
        highRating.setProductId(9L);
        highRating.setUserId(10L);
        highRating.setRating(5);

        reviewRepository.save(lowRating);
        reviewRepository.save(highRating);

        List<Review> reviews = reviewRepository.findByProductId(9L);
        assertTrue(reviews.stream().allMatch(r -> r.getRating() >= 1 && r.getRating() <= 5));
    }
}
