package com.mycom.myapp.payment.dto;

import java.util.List;

/** Page 직렬화 형식이 바뀌는 것을 막기 위해 필요한 값만 담아 내려준다. */
public record AdminPaymentPageResponse(
        List<AdminPaymentResponse> content,
        int page, int size, long totalElements, int totalPages,
        AdminPaymentSummary summary) {}
