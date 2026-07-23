package com.mycom.myapp.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mycom.myapp.payment.dto.PaymentConfirmRequest;
import com.mycom.myapp.payment.dto.PaymentOrderRequest;
import com.mycom.myapp.payment.dto.PaymentOrderResponse;
import com.mycom.myapp.ticket.service.TicketService;
import com.mycom.myapp.user.entity.User;
import com.mycom.myapp.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {
    @Mock PaymentOrderRepository orderRepository;
    @Mock UserRepository userRepository;
    @Mock TicketService ticketService;
    private PaymentService service;

    @BeforeEach
    void setUp() {
        service = new PaymentService(
                orderRepository, userRepository, ticketService, "test_client_key", "test_secret_key");
    }

    @Test
    void serverCalculatesTestPriceAsOneHundredWonPerClass() {
        User user = user();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentOrderResponse response = service.createOrder(1L, new PaymentOrderRequest(10));

        assertThat(response.amount()).isEqualTo(1_000L);
        assertThat(response.orderName()).isEqualTo("PT 수강권 10회권");
        verify(orderRepository).save(any(PaymentOrder.class));
    }

    @Test
    void manipulatedAmountIsRejectedBeforeTossApproval() {
        User user = user();
        PaymentOrder order = new PaymentOrder(user, "TICKET_ORDER_123", 10, 1_000L);
        when(orderRepository.findByOrderId(order.getOrderId())).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> service.confirm(1L,
                new PaymentConfirmRequest("payment-key", order.getOrderId(), 100L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("결제 금액");
    }

    @Test
    void alreadyPaidOrderCannotIssueAnotherTicket() {
        User user = user();
        PaymentOrder order = new PaymentOrder(user, "TICKET_ORDER_123", 1, 100L);
        order.approve("already-paid-key");
        when(orderRepository.findByOrderId(order.getOrderId())).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> service.confirm(1L,
                new PaymentConfirmRequest("another-key", order.getOrderId(), 100L)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 처리된");
    }

    private User user() {
        return User.builder().id(1L).name("회원").email("customer@test.com")
                .password("encoded").build();
    }
}
