package com.example.reviewservice.service;

import com.example.reviewservice.dto.ReviewRequest;
import com.example.reviewservice.dto.ReviewResponse;
import com.example.reviewservice.entity.Review;
import com.example.reviewservice.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReviewService {
    @Autowired
    private ReviewRepository reviewRepository;

    public ReviewResponse createReview(ReviewRequest request) {
        Review review = new Review();
        review.setProductId(request.getProductId());
        review.setUserId(request.getUserId());
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        Review savedReview = reviewRepository.save(review);
        return mapToResponse(savedReview);
    }

    public Optional<ReviewResponse> getReviewById(Long id) {
        return reviewRepository.findById(id).map(this::mapToResponse);
    }

    public List<ReviewResponse> getReviewsByProductId(Long productId) {
        return reviewRepository.findByProductId(productId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ReviewResponse> getReviewsByUserId(Long userId) {
        return reviewRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public Double getAverageRating(Long productId) {
        return reviewRepository.getAverageRatingByProductId(productId);
    }

    public Integer getReviewCount(Long productId) {
        return reviewRepository.getReviewCountByProductId(productId);
    }

    public ReviewResponse updateReview(Long id, ReviewRequest request) {
        Optional<Review> optional = reviewRepository.findById(id);
        if (optional.isPresent()) {
            Review review = optional.get();
            review.setRating(request.getRating());
            review.setComment(request.getComment());

            Review updatedReview = reviewRepository.save(review);
            return mapToResponse(updatedReview);
        }
        return null;
    }

    public boolean deleteReview(Long id) {
        if (reviewRepository.existsById(id)) {
            reviewRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private ReviewResponse mapToResponse(Review review) {
        ReviewResponse response = new ReviewResponse();
        response.setId(review.getId());
        response.setProductId(review.getProductId());
        response.setUserId(review.getUserId());
        response.setRating(review.getRating());
        response.setComment(review.getComment());
        response.setCreatedAt(review.getCreatedAt());
        response.setUpdatedAt(review.getUpdatedAt());
        return response;
    }
}
