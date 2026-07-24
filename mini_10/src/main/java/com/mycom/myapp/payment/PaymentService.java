package com.mycom.myapp.payment;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.mycom.myapp.common.ForbiddenOperationException;
import com.mycom.myapp.payment.dto.AdminPaymentPageResponse;
import com.mycom.myapp.payment.dto.AdminPaymentResponse;
import com.mycom.myapp.payment.dto.AdminPaymentSummary;
import com.mycom.myapp.payment.dto.PaymentConfirmRequest;
import com.mycom.myapp.payment.dto.PaymentConfirmResponse;
import com.mycom.myapp.payment.dto.PaymentOrderRequest;
import com.mycom.myapp.payment.dto.PaymentOrderResponse;
import com.mycom.myapp.ticket.service.TicketService;
import com.mycom.myapp.user.entity.User;
import com.mycom.myapp.user.repository.UserRepository;

@Service
public class PaymentService {
    public static final long PRICE_PER_TICKET = 100L;

    private final PaymentOrderRepository orderRepository;
    private final UserRepository userRepository;
    private final TicketService ticketService;
    private final RestClient restClient;
    private final String clientKey;
    private final String secretKey;

    public PaymentService(
            PaymentOrderRepository orderRepository, UserRepository userRepository,
            TicketService ticketService,
            @Value("${toss.payments.client-key:}") String clientKey,
            @Value("${toss.payments.secret-key:}") String secretKey) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.ticketService = ticketService;
        this.clientKey = clientKey;
        this.secretKey = secretKey;
        this.restClient = RestClient.create("https://api.tosspayments.com");
    }

    /** 관리자 결제 관리 화면용 목록 조회. 검색·상태 필터는 선택값이며 요약은 항상 전체 기준이다. */
    @Transactional(readOnly = true)
    public AdminPaymentPageResponse findPayments(PaymentStatus status, String keyword, int page, int size) {
        String searchKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        Pageable pageable = PageRequest.of(
                Math.max(page, 0), Math.min(Math.max(size, 1), 100), Sort.by(Sort.Direction.DESC, "id"));
        Page<PaymentOrder> orders = orderRepository.search(status, searchKeyword, pageable);

        AdminPaymentSummary summary = new AdminPaymentSummary(
                orderRepository.count(),
                orderRepository.countByStatus(PaymentStatus.PAID),
                orderRepository.countByStatus(PaymentStatus.READY),
                orderRepository.countByStatus(PaymentStatus.FAILED));
        return new AdminPaymentPageResponse(
                orders.getContent().stream().map(AdminPaymentResponse::from).toList(),
                orders.getNumber(), orders.getSize(),
                orders.getTotalElements(), orders.getTotalPages(), summary);
    }

    @Transactional
    public PaymentOrderResponse createOrder(Long userId, PaymentOrderRequest request) {
        if (clientKey.isBlank()) throw new IllegalStateException("토스 테스트 클라이언트 키가 설정되지 않았습니다.");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
        long amount = Math.multiplyExact(PRICE_PER_TICKET, request.ticketCount());
        String orderId = "TICKET_" + UUID.randomUUID().toString().replace("-", "");
        orderRepository.save(new PaymentOrder(user, orderId, request.ticketCount(), amount));
        return new PaymentOrderResponse(
                orderId, "PT 수강권 " + request.ticketCount() + "회권", amount, clientKey,
                "CUSTOMER_" + UUID.randomUUID(), user.getName(), user.getEmail());
    }

    @Transactional
    public PaymentConfirmResponse confirm(Long userId, PaymentConfirmRequest request) {
        if (secretKey.isBlank()) throw new IllegalStateException("토스 테스트 시크릿 키가 설정되지 않았습니다.");
        PaymentOrder order = orderRepository.findByOrderId(request.orderId())
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
        if (!order.getUser().getId().equals(userId)) {
            throw new ForbiddenOperationException("본인의 주문만 승인할 수 있습니다.");
        }
        if (order.getStatus() != PaymentStatus.READY) throw new IllegalStateException("이미 처리된 주문입니다.");
        if (order.getAmount() != request.amount()) throw new IllegalArgumentException("결제 금액이 주문 금액과 일치하지 않습니다.");

        String credential = Base64.getEncoder().encodeToString(
                (secretKey + ":").getBytes(StandardCharsets.UTF_8));
        try {
            Map<?, ?> approvedPayment = restClient.post().uri("/v1/payments/confirm")
                    .header(HttpHeaders.AUTHORIZATION, "Basic " + credential)
                    .body(Map.of("paymentKey", request.paymentKey(),
                            "orderId", request.orderId(), "amount", request.amount()))
                    .retrieve().body(Map.class);
            if (approvedPayment == null
                    || !"DONE".equals(approvedPayment.get("status"))
                    || !(approvedPayment.get("totalAmount") instanceof Number approvedAmount)
                    || approvedAmount.longValue() != order.getAmount()) {
                throw new IllegalStateException("토스 승인 결과의 상태 또는 금액이 올바르지 않습니다.");
            }
        } catch (RestClientResponseException exception) {
            throw new IllegalStateException("토스 결제 승인에 실패했습니다: " + exception.getResponseBodyAsString());
        }

        order.approve(request.paymentKey());
        LocalDate startDate = LocalDate.now();
        Long ticketId = ticketService.issueTicket(
                userId, order.getTicketCount(), startDate, startDate.plusMonths(1));
        return new PaymentConfirmResponse(
                order.getOrderId(), order.getAmount(), order.getTicketCount(), ticketId);
    }
}
