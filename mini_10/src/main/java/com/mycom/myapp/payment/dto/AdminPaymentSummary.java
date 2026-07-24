package com.mycom.myapp.payment.dto;

/** 상단 요약 카드용 전체 집계 (검색 조건과 무관한 전체 기준). */
public record AdminPaymentSummary(long total, long paid, long ready, long failed) {}
