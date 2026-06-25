package com.zhousheng.llcb.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class BizDtos {

    private BizDtos() {
    }

    public record CertSubmitRequest(
            @NotNull Long catalogId,
            @NotBlank String certifyType,
            @NotBlank String outcomeName,
            String certificateNo,
            String issuingAuthority,
            LocalDate obtainedAt,
            @DecimalMin(value = "0.01") BigDecimal requestedCredit,
            List<Long> materialFileIds) {
    }

    public record AuditRequest(BigDecimal recognizedCredit, String reason) {
    }

    public record ConversionSubmitRequest(@NotNull Long ruleId,
                                          @NotNull Long sourceOutcomeId,
                                          @NotNull @DecimalMin(value = "0.01") BigDecimal sourceCredit) {
    }

    public record ConversionPreviewResponse(Long ruleId, BigDecimal conversionRatio, BigDecimal targetCredit) {
    }

    public record ReviewAssignRequest(@NotNull Long expertId) {
    }

    public record ExpertReviewRequest(@NotBlank String opinion, String reviewComment) {
    }

    public record FeedbackReplyRequest(@NotBlank String replyContent) {
    }
}
