package com.utc.ec.controller;

import com.utc.ec.dto.*;
import com.utc.ec.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Review", description = "API danh gia san pham")
public class ReviewController {

    private final ReviewService reviewService;

    // ─────────────────────────────────────────────────────────────
    //  Public endpoints
    // ─────────────────────────────────────────────────────────────

    @Operation(
            summary = "Xem danh gia theo san pham",
            description = "Lay tat ca danh gia cua 1 san pham, sap xep moi nhat truoc. " +
                          "Khong can dang nhap."
    )
    @GetMapping("/product/{productId}")
    public ApiResponse<List<ReviewResponseDTO>> getReviewsByProduct(
            @Parameter(description = "ID san pham") @PathVariable Integer productId) {
        return ApiResponse.success(null, reviewService.getReviewsByProductId(productId));
    }

    @Operation(
            summary = "Thong ke danh gia san pham",
            description = "Lay diem trung binh va tong so danh gia cua 1 san pham. " +
                          "Khong can dang nhap."
    )
    @GetMapping("/product/{productId}/summary")
    public ApiResponse<ReviewSummaryDTO> getReviewSummary(
            @Parameter(description = "ID san pham") @PathVariable Integer productId) {
        return ApiResponse.success(null, reviewService.getReviewSummary(productId));
    }

    // ─────────────────────────────────────────────────────────────
    //  Authenticated endpoints
    // ─────────────────────────────────────────────────────────────

    @Operation(
            summary = "Tao danh gia",
            description = "Danh gia san pham da mua. Yeu cau: " +
                          "1) Don hang phai o trang thai DELIVERED. " +
                          "2) Chi danh gia 1 lan cho moi order_line. " +
                          "3) Diem tu 1-5 sao."
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ReviewResponseDTO> createReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateReviewRequest request) {
        return ApiResponse.success("review.create.success",
                reviewService.createReview(userDetails.getUsername(), request));
    }

    @Operation(
            summary = "Xem danh gia cua toi",
            description = "Lay tat ca danh gia ma ban da viet, sap xep moi nhat truoc."
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/my")
    public ApiResponse<List<ReviewResponseDTO>> getMyReviews(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ApiResponse.success(null, reviewService.getMyReviews(userDetails.getUsername()));
    }

    @Operation(
            summary = "Xoa danh gia",
            description = "Chi xoa duoc danh gia cua chinh minh."
    )
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{reviewId}")
    public ApiResponse<Void> deleteReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "ID danh gia") @PathVariable Integer reviewId) {
        reviewService.deleteReview(userDetails.getUsername(), reviewId);
        return ApiResponse.success("review.delete.success", null);
    }
}

