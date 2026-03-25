package com.utc.ec.service;

import com.utc.ec.dto.CreateReviewRequest;
import com.utc.ec.dto.ReviewResponseDTO;
import com.utc.ec.dto.ReviewSummaryDTO;

import java.util.List;

public interface ReviewService {

    ReviewResponseDTO createReview(String username, CreateReviewRequest request);

    List<ReviewResponseDTO> getReviewsByProductId(Integer productId);

    ReviewSummaryDTO getReviewSummary(Integer productId);

    List<ReviewResponseDTO> getMyReviews(String username);

    void deleteReview(String username, Integer reviewId);
}

