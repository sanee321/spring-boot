package com.example.reviewservice.service;

import com.example.reviewservice.dto.ReviewRequest;
import com.example.reviewservice.dto.ReviewResponse;
import com.example.reviewservice.entity.Review;
import com.example.reviewservice.repository.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceTest {
    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    public void testCreateReview() {
        ReviewRequest request = new ReviewRequest(1L, 1L, 5, "Excellent product!");
        
        Review review = new Review();
        review.setId(1L);
        review.setProductId(1L);
        review.setUserId(1L);
        review.setRating(5);
        review.setComment("Excellent product!");
        review.setCreatedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());

        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        ReviewResponse response = reviewService.createReview(request);

        assertNotNull(response);
        assertEquals(5, response.getRating());
        assertEquals("Excellent product!", response.getComment());
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    public void testGetReviewsByProductId() {
        Review review = new Review();
        review.setId(1L);
        review.setProductId(1L);
        review.setRating(5);

        when(reviewRepository.findByProductId(1L)).thenReturn(Arrays.asList(review));

        List<ReviewResponse> responses = reviewService.getReviewsByProductId(1L);

        assertEquals(1, responses.size());
        verify(reviewRepository, times(1)).findByProductId(1L);
    }

    @Test
    public void testGetAverageRating() {
        when(reviewRepository.getAverageRatingByProductId(1L)).thenReturn(4.5);

        Double rating = reviewService.getAverageRating(1L);

        assertEquals(4.5, rating);
        verify(reviewRepository, times(1)).getAverageRatingByProductId(1L);
    }

    @Test
    public void testGetReviewCount() {
        when(reviewRepository.getReviewCountByProductId(1L)).thenReturn(10);

        Integer count = reviewService.getReviewCount(1L);

        assertEquals(10, count);
        verify(reviewRepository, times(1)).getReviewCountByProductId(1L);
    }

    @Test
    public void testUpdateReview() {
        Review review = new Review();
        review.setId(1L);
        review.setRating(5);
        review.setCreatedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());

        ReviewRequest request = new ReviewRequest(1L, 1L, 4, "Good product");

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        ReviewResponse response = reviewService.updateReview(1L, request);

        assertNotNull(response);
        verify(reviewRepository, times(1)).findById(1L);
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    public void testDeleteReview() {
        when(reviewRepository.existsById(1L)).thenReturn(true);

        boolean result = reviewService.deleteReview(1L);

        assertTrue(result);
        verify(reviewRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testGetReviewById() {
        Review review = new Review();
        review.setId(1L);
        review.setProductId(1L);
        review.setRating(5);

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        Optional<ReviewResponse> response = reviewService.getReviewById(1L);

        assertTrue(response.isPresent());
        assertEquals(5, response.get().getRating());
        verify(reviewRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetReviewByIdNotFound() {
        when(reviewRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<ReviewResponse> response = reviewService.getReviewById(999L);

        assertFalse(response.isPresent());
        verify(reviewRepository, times(1)).findById(999L);
    }

    @Test
    public void testGetReviewsByUserId() {
        Review review = new Review();
        review.setId(1L);
        review.setUserId(1L);
        review.setRating(4);

        when(reviewRepository.findByUserId(1L)).thenReturn(Arrays.asList(review));

        List<ReviewResponse> responses = reviewService.getReviewsByUserId(1L);

        assertEquals(1, responses.size());
        verify(reviewRepository, times(1)).findByUserId(1L);
    }

    @Test
    public void testGetAllReviews() {
        Review review1 = new Review();
        review1.setId(1L);
        review1.setRating(5);

        Review review2 = new Review();
        review2.setId(2L);
        review2.setRating(4);

        when(reviewRepository.findAll()).thenReturn(Arrays.asList(review1, review2));

        List<ReviewResponse> responses = reviewService.getAllReviews();

        assertEquals(2, responses.size());
        verify(reviewRepository, times(1)).findAll();
    }

    @Test
    public void testGetReviewsByProductIdEmpty() {
        when(reviewRepository.findByProductId(999L)).thenReturn(Arrays.asList());

        List<ReviewResponse> responses = reviewService.getReviewsByProductId(999L);

        assertTrue(responses.isEmpty());
        verify(reviewRepository, times(1)).findByProductId(999L);
    }

    @Test
    public void testAverageRatingWithMultipleReviews() {
        when(reviewRepository.getAverageRatingByProductId(1L)).thenReturn(3.8);

        Double rating = reviewService.getAverageRating(1L);

        assertEquals(3.8, rating);
        verify(reviewRepository, times(1)).getAverageRatingByProductId(1L);
    }

    @Test
    public void testReviewCountZero() {
        when(reviewRepository.getReviewCountByProductId(999L)).thenReturn(0);

        Integer count = reviewService.getReviewCount(999L);

        assertEquals(0, count);
        verify(reviewRepository, times(1)).getReviewCountByProductId(999L);
    }

    @Test
    public void testCreateReviewWithMinimalRating() {
        ReviewRequest request = new ReviewRequest(1L, 1L, 1, "Poor product");
        
        Review review = new Review();
        review.setId(1L);
        review.setRating(1);
        review.setComment("Poor product");

        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        ReviewResponse response = reviewService.createReview(request);

        assertNotNull(response);
        assertEquals(1, response.getRating());
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    public void testUpdateReviewNotFound() {
        ReviewRequest request = new ReviewRequest(1L, 1L, 4, "Good");

        when(reviewRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<ReviewResponse> response = reviewService.updateReview(999L, request);

        assertFalse(response.isPresent());
        verify(reviewRepository, times(1)).findById(999L);
        verify(reviewRepository, never()).save(any());
    }

    @Test
    public void testDeleteReviewNotFound() {
        when(reviewRepository.existsById(999L)).thenReturn(false);

        boolean result = reviewService.deleteReview(999L);

        assertFalse(result);
        verify(reviewRepository, never()).deleteById(any());
    }

    @Test
    public void testCreateReviewWithMaximalRating() {
        ReviewRequest request = new ReviewRequest(1L, 1L, 5, "Excellent product");
        
        Review review = new Review();
        review.setId(1L);
        review.setRating(5);
        review.setComment("Excellent product");

        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        ReviewResponse response = reviewService.createReview(request);

        assertNotNull(response);
        assertEquals(5, response.getRating());
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    public void testGetAverageRatingMultipleProducts() {
        when(reviewRepository.getAverageRatingByProductId(1L)).thenReturn(4.5);
        when(reviewRepository.getAverageRatingByProductId(2L)).thenReturn(3.8);

        Double rating1 = reviewService.getAverageRating(1L);
        Double rating2 = reviewService.getAverageRating(2L);

        assertEquals(4.5, rating1);
        assertEquals(3.8, rating2);
        verify(reviewRepository, times(2)).getAverageRatingByProductId(anyLong());
    }

    @Test
    public void testGetReviewCountMultipleProducts() {
        when(reviewRepository.getReviewCountByProductId(1L)).thenReturn(10);
        when(reviewRepository.getReviewCountByProductId(2L)).thenReturn(5);

        Integer count1 = reviewService.getReviewCount(1L);
        Integer count2 = reviewService.getReviewCount(2L);

        assertEquals(10, count1);
        assertEquals(5, count2);
        verify(reviewRepository, times(2)).getReviewCountByProductId(anyLong());
    }

    @Test
    public void testGetReviewByIdSuccess() {
        Review review = new Review();
        review.setId(1L);
        review.setRating(4);
        review.setComment("Good");

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        Optional<ReviewResponse> response = reviewService.getReviewById(1L);

        assertTrue(response.isPresent());
        assertEquals(4, response.get().getRating());
        verify(reviewRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetReviewsByProductIdMultiple() {
        Review review1 = new Review();
        review1.setId(1L);
        review1.setProductId(1L);

        Review review2 = new Review();
        review2.setId(2L);
        review2.setProductId(1L);

        when(reviewRepository.findByProductId(1L)).thenReturn(Arrays.asList(review1, review2));

        List<ReviewResponse> responses = reviewService.getReviewsByProductId(1L);

        assertEquals(2, responses.size());
        verify(reviewRepository, times(1)).findByProductId(1L);
    }

    @Test
    public void testGetReviewsByUserIdMultiple() {
        Review review1 = new Review();
        review1.setId(1L);
        review1.setUserId(1L);

        Review review2 = new Review();
        review2.setId(2L);
        review2.setUserId(1L);

        when(reviewRepository.findByUserId(1L)).thenReturn(Arrays.asList(review1, review2));

        List<ReviewResponse> responses = reviewService.getReviewsByUserId(1L);

        assertEquals(2, responses.size());
        verify(reviewRepository, times(1)).findByUserId(1L);
    }

    @Test
    public void testUpdateReviewSuccess() {
        ReviewRequest request = new ReviewRequest(1L, 1L, 5, "Excellent");

        Review existingReview = new Review();
        existingReview.setId(1L);
        existingReview.setRating(3);
        existingReview.setComment("Good");

        Review updatedReview = new Review();
        updatedReview.setId(1L);
        updatedReview.setRating(5);
        updatedReview.setComment("Excellent");

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(existingReview));
        when(reviewRepository.save(any(Review.class))).thenReturn(updatedReview);

        Optional<ReviewResponse> response = reviewService.updateReview(1L, request);

        assertTrue(response.isPresent());
        assertEquals(5, response.get().getRating());
        verify(reviewRepository, times(1)).findById(1L);
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    public void testDeleteReviewSuccess() {
        when(reviewRepository.existsById(1L)).thenReturn(true);

        boolean result = reviewService.deleteReview(1L);

        assertTrue(result);
        verify(reviewRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testReviewServiceIntegrationFlow() {
        ReviewRequest request = new ReviewRequest(1L, 1L, 4, "Great product");

        Review savedReview = new Review();
        savedReview.setId(1L);
        savedReview.setProductId(1L);
        savedReview.setUserId(1L);
        savedReview.setRating(4);
        savedReview.setComment("Great product");

        when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(savedReview));
        when(reviewRepository.findByProductId(1L)).thenReturn(Arrays.asList(savedReview));
        when(reviewRepository.getAverageRatingByProductId(1L)).thenReturn(4.0);

        ReviewResponse createResponse = reviewService.createReview(request);
        assertNotNull(createResponse);

        Optional<ReviewResponse> retrieveResponse = reviewService.getReviewById(1L);
        assertTrue(retrieveResponse.isPresent());

        List<ReviewResponse> productReviews = reviewService.getReviewsByProductId(1L);
        assertFalse(productReviews.isEmpty());

        Double avgRating = reviewService.getAverageRating(1L);
        assertEquals(4.0, avgRating);
    }
}
}
