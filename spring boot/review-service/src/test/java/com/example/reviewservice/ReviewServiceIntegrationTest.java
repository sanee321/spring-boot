package com.example.reviewservice;

import com.example.reviewservice.dto.ReviewRequest;
import com.example.reviewservice.dto.ReviewResponse;
import com.example.reviewservice.entity.Review;
import com.example.reviewservice.repository.ReviewRepository;
import com.example.reviewservice.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class ReviewServiceIntegrationTest {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ReviewRepository reviewRepository;

    @BeforeEach
    void setUp() {
        reviewRepository.deleteAll();
    }

    @Test
    void testCreateAndRetrieveReview() {
        ReviewRequest request = new ReviewRequest(1L, 1L, 5, "Excellent product!");
        ReviewResponse response = reviewService.createReview(request);

        assertNotNull(response);
        assertEquals(5, response.getRating());

        Optional<ReviewResponse> retrieved = reviewService.getReviewById(response.getId());
        assertTrue(retrieved.isPresent());
        assertEquals(5, retrieved.get().getRating());
    }

    @Test
    void testCreateAndUpdateReview() {
        ReviewRequest request = new ReviewRequest(1L, 1L, 5, "Excellent");
        ReviewResponse created = reviewService.createReview(request);

        ReviewRequest updateRequest = new ReviewRequest(1L, 1L, 4, "Good");
        Optional<ReviewResponse> updated = reviewService.updateReview(created.getId(), updateRequest);

        assertTrue(updated.isPresent());
        assertEquals(4, updated.get().getRating());
    }

    @Test
    void testGetReviewsByProductIdFlow() {
        ReviewRequest request1 = new ReviewRequest(1L, 1L, 5, "Great!");
        ReviewRequest request2 = new ReviewRequest(1L, 2L, 4, "Good");

        reviewService.createReview(request1);
        reviewService.createReview(request2);

        List<ReviewResponse> reviews = reviewService.getReviewsByProductId(1L);

        assertEquals(2, reviews.size());
    }

    @Test
    void testGetAverageRatingFlow() {
        reviewService.createReview(new ReviewRequest(1L, 1L, 5, "Great"));
        reviewService.createReview(new ReviewRequest(1L, 2L, 3, "Okay"));

        Double avgRating = reviewService.getAverageRating(1L);

        assertNotNull(avgRating);
        assertTrue(avgRating > 0);
    }

    @Test
    void testDeleteReviewFlow() {
        ReviewRequest request = new ReviewRequest(1L, 1L, 5, "Great");
        ReviewResponse created = reviewService.createReview(request);

        boolean deleted = reviewService.deleteReview(created.getId());

        assertTrue(deleted);
        Optional<ReviewResponse> notFound = reviewService.getReviewById(created.getId());
        assertFalse(notFound.isPresent());
    }

    @Test
    void testGetReviewsByUserIdFlow() {
        ReviewRequest request1 = new ReviewRequest(1L, 1L, 5, "Great");
        ReviewRequest request2 = new ReviewRequest(2L, 1L, 4, "Good");

        reviewService.createReview(request1);
        reviewService.createReview(request2);

        List<ReviewResponse> reviews = reviewService.getReviewsByUserId(1L);

        assertEquals(2, reviews.size());
    }

    @Test
    void testReviewCount() {
        reviewService.createReview(new ReviewRequest(1L, 1L, 5, "Great"));
        reviewService.createReview(new ReviewRequest(1L, 2L, 4, "Good"));

        Integer count = reviewService.getReviewCount(1L);

        assertEquals(2, count);
    }

    @Test
    void testCreateMultipleReviewsForDifferentProducts() {
        reviewService.createReview(new ReviewRequest(1L, 1L, 5, "Great"));
        reviewService.createReview(new ReviewRequest(2L, 2L, 4, "Good"));
        reviewService.createReview(new ReviewRequest(3L, 3L, 3, "Okay"));

        List<ReviewResponse> allReviews = reviewService.getAllReviews();

        assertTrue(allReviews.size() >= 3);
    }
}
