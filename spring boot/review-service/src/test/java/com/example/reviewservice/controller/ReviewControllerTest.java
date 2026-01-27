package com.example.reviewservice.controller;

import com.example.reviewservice.dto.ReviewRequest;
import com.example.reviewservice.dto.ReviewResponse;
import com.example.reviewservice.service.ReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReviewService reviewService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateReview() throws Exception {
        ReviewRequest request = new ReviewRequest(1L, 1L, 5, "Excellent product!");
        ReviewResponse response = new ReviewResponse(1L, 1L, 1L, 5, "Excellent product!", LocalDateTime.now());

        when(reviewService.createReview(any(ReviewRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productId").value(1L))
                .andExpect(jsonPath("$.rating").value(5));

        verify(reviewService, times(1)).createReview(any(ReviewRequest.class));
    }

    @Test
    void testGetReviewById() throws Exception {
        ReviewResponse response = new ReviewResponse(1L, 1L, 1L, 5, "Excellent product!", LocalDateTime.now());

        when(reviewService.getReviewById(1L)).thenReturn(Optional.of(response));

        mockMvc.perform(get("/api/reviews/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.productId").value(1L));

        verify(reviewService, times(1)).getReviewById(1L);
    }

    @Test
    void testGetReviewByIdNotFound() throws Exception {
        when(reviewService.getReviewById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/reviews/999"))
                .andExpect(status().isNotFound());

        verify(reviewService, times(1)).getReviewById(999L);
    }

    @Test
    void testGetReviewsByProductId() throws Exception {
        List<ReviewResponse> responses = Arrays.asList(
                new ReviewResponse(1L, 1L, 1L, 5, "Excellent!", LocalDateTime.now()),
                new ReviewResponse(2L, 1L, 2L, 4, "Good!", LocalDateTime.now())
        );

        when(reviewService.getReviewsByProductId(1L)).thenReturn(responses);

        mockMvc.perform(get("/api/reviews/product/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].productId").value(1L));

        verify(reviewService, times(1)).getReviewsByProductId(1L);
    }

    @Test
    void testGetReviewsByUserId() throws Exception {
        List<ReviewResponse> responses = Arrays.asList(
                new ReviewResponse(1L, 1L, 1L, 5, "Great!", LocalDateTime.now()),
                new ReviewResponse(2L, 2L, 1L, 4, "Good!", LocalDateTime.now())
        );

        when(reviewService.getReviewsByUserId(1L)).thenReturn(responses);

        mockMvc.perform(get("/api/reviews/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(reviewService, times(1)).getReviewsByUserId(1L);
    }

    @Test
    void testUpdateReview() throws Exception {
        ReviewRequest request = new ReviewRequest(1L, 1L, 4, "Updated review");
        ReviewResponse response = new ReviewResponse(1L, 1L, 1L, 4, "Updated review", LocalDateTime.now());

        when(reviewService.updateReview(anyLong(), any(ReviewRequest.class))).thenReturn(Optional.of(response));

        mockMvc.perform(put("/api/reviews/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(4));

        verify(reviewService, times(1)).updateReview(anyLong(), any(ReviewRequest.class));
    }

    @Test
    void testDeleteReview() throws Exception {
        mockMvc.perform(delete("/api/reviews/1"))
                .andExpect(status().isNoContent());

        verify(reviewService, times(1)).deleteReview(1L);
    }

    @Test
    void testGetAllReviews() throws Exception {
        List<ReviewResponse> responses = Arrays.asList(
                new ReviewResponse(1L, 1L, 1L, 5, "Great!", LocalDateTime.now()),
                new ReviewResponse(2L, 2L, 2L, 4, "Good!", LocalDateTime.now())
        );

        when(reviewService.getAllReviews()).thenReturn(responses);

        mockMvc.perform(get("/api/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(reviewService, times(1)).getAllReviews();
    }

    @Test
    void testGetAverageRating() throws Exception {
        when(reviewService.getAverageRating(1L)).thenReturn(4.5);

        mockMvc.perform(get("/api/reviews/product/1/average-rating"))
                .andExpect(status().isOk());

        verify(reviewService, times(1)).getAverageRating(1L);
    }

    @Test
    void testGetReviewsByProductIdEmpty() throws Exception {
        when(reviewService.getReviewsByProductId(999L)).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/reviews/product/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(reviewService, times(1)).getReviewsByProductId(999L);
    }

    @Test
    void testGetReviewsByUserIdEmpty() throws Exception {
        when(reviewService.getReviewsByUserId(999L)).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/reviews/user/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(reviewService, times(1)).getReviewsByUserId(999L);
    }

    @Test
    void testUpdateReviewNotFound() throws Exception {
        ReviewRequest request = new ReviewRequest(1L, 1L, 4, "Updated");

        when(reviewService.updateReview(anyLong(), any(ReviewRequest.class))).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/reviews/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(reviewService, times(1)).updateReview(anyLong(), any(ReviewRequest.class));
    }

    @Test
    void testCreateMultipleReviews() throws Exception {
        ReviewRequest request1 = new ReviewRequest(1L, 1L, 5, "Excellent");
        ReviewRequest request2 = new ReviewRequest(2L, 2L, 4, "Good");

        ReviewResponse response1 = new ReviewResponse(1L, 1L, 1L, 5, "Excellent", LocalDateTime.now());
        ReviewResponse response2 = new ReviewResponse(2L, 2L, 2L, 4, "Good", LocalDateTime.now());

        when(reviewService.createReview(any(ReviewRequest.class)))
                .thenReturn(response1)
                .thenReturn(response2);

        mockMvc.perform(post("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isCreated());

        verify(reviewService, times(2)).createReview(any(ReviewRequest.class));
    }

    @Test
    void testGetAllReviewsEmpty() throws Exception {
        when(reviewService.getAllReviews()).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(reviewService, times(1)).getAllReviews();
    }

    @Test
    void testDeleteReviewNotFound() throws Exception {
        mockMvc.perform(delete("/api/reviews/999"))
                .andExpect(status().isNoContent());

        verify(reviewService, times(1)).deleteReview(999L);
    }

    @Test
    void testGetReviewsWithDifferentRatings() throws Exception {
        List<ReviewResponse> responses = Arrays.asList(
                new ReviewResponse(1L, 1L, 1L, 5, "Excellent", LocalDateTime.now()),
                new ReviewResponse(2L, 1L, 2L, 3, "Average", LocalDateTime.now()),
                new ReviewResponse(3L, 1L, 3L, 1, "Poor", LocalDateTime.now())
        );

        when(reviewService.getReviewsByProductId(1L)).thenReturn(responses);

        mockMvc.perform(get("/api/reviews/product/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].rating").value(5))
                .andExpect(jsonPath("$[1].rating").value(3))
                .andExpect(jsonPath("$[2].rating").value(1));

        verify(reviewService, times(1)).getReviewsByProductId(1L);
    }

    @Test
    void testGetAverageRatingMultiple() throws Exception {
        when(reviewService.getAverageRating(1L)).thenReturn(3.5);
        when(reviewService.getAverageRating(2L)).thenReturn(4.2);

        mockMvc.perform(get("/api/reviews/product/1/average-rating"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/reviews/product/2/average-rating"))
                .andExpect(status().isOk());

        verify(reviewService, times(1)).getAverageRating(1L);
        verify(reviewService, times(1)).getAverageRating(2L);
    }
}
