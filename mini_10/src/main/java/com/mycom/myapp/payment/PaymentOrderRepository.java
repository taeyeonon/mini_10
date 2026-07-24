package com.mycom.myapp.payment;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, Long> {
    Optional<PaymentOrder> findByOrderId(String orderId);

    long countByStatus(PaymentStatus status);

    /** 특정 상태 결제의 금액 합계 (없으면 0). 요약 카드의 매출 집계에 사용. */
    @Query("select coalesce(sum(p.amount), 0) from PaymentOrder p where p.status = :status")
    long sumAmountByStatus(@Param("status") PaymentStatus status);

    /** 특정 상태 결제의 수강권 횟수 합계 (없으면 0). */
    @Query("select coalesce(sum(p.ticketCount), 0) from PaymentOrder p where p.status = :status")
    long sumTicketCountByStatus(@Param("status") PaymentStatus status);

    /** 관리자 결제 내역 검색: 주문번호·회원ID·이름·이메일 부분 일치 + 상태 필터 (둘 다 null이면 전체). */
    @Query(value = """
            select p from PaymentOrder p join fetch p.user u
            where (:status is null or p.status = :status)
              and (:keyword is null
                   or lower(p.orderId) like lower(concat('%', :keyword, '%'))
                   or lower(u.name) like lower(concat('%', :keyword, '%'))
                   or lower(u.email) like lower(concat('%', :keyword, '%'))
                   or cast(u.id as String) = :keyword)
            """,
            countQuery = """
            select count(p) from PaymentOrder p join p.user u
            where (:status is null or p.status = :status)
              and (:keyword is null
                   or lower(p.orderId) like lower(concat('%', :keyword, '%'))
                   or lower(u.name) like lower(concat('%', :keyword, '%'))
                   or lower(u.email) like lower(concat('%', :keyword, '%'))
                   or cast(u.id as String) = :keyword)
            """)
    Page<PaymentOrder> search(
            @Param("status") PaymentStatus status,
            @Param("keyword") String keyword,
            Pageable pageable);
}
