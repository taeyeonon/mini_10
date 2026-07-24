# API 명세

인증이 필요한 요청은 헤더에 JWT를 전달합니다.

```http
X-AUTH-TOKEN: {accessToken}
```

## 인증·사용자

| Method | Path | 권한 | 설명 |
|---|---|---|---|
| POST | `/users` | 공개 | 회원가입 |
| POST | `/auth/login` | 공개 | 로그인 및 JWT 발급 |
| GET | `/admin/users` | ADMIN | 전체 사용자 목록 (역할 포함) |
| GET | `/admin/users/customers` | ADMIN | 수강권 발급용 회원 목록 |

사용자 응답에는 역할이 함께 담깁니다: `{"id":1,"name":"min","email":"min@m.com","roles":["CUSTOMER"]}`

## 수강권

| Method | Path | 권한 | 설명 |
|---|---|---|---|
| GET | `/customer/tickets/me` | CUSTOMER, ADMIN | 내 수강권 조회 |
| POST | `/admin/tickets/issue` | ADMIN | 회원에게 수강권 수동 발급 |

## 결제

| Method | Path | 권한 | 설명 |
|---|---|---|---|
| POST | `/customer/payments/orders` | CUSTOMER, ADMIN | 서버 금액으로 결제 주문 생성 |
| POST | `/customer/payments/confirm` | CUSTOMER, ADMIN | 토스 승인·수강권 자동 지급 |
| GET | `/api/admin/payments` | ADMIN | 결제 내역 목록 (검색·상태 필터·페이징) |

주문 생성 요청:

```json
{"ticketCount": 10}
```

결제 승인 요청:

```json
{"paymentKey":"...","orderId":"TICKET_...","amount":1000}
```

결제 내역 조회 (`/admin-payment.html` 화면에서 사용):

- 쿼리 파라미터: `keyword`(주문번호·회원ID·이름·이메일 부분 일치), `status`(`READY|PAID|FAILED`), `page`(0부터), `size`(최대 100, 기본 10)
- `summary`는 검색 조건과 무관한 전체 기준 집계입니다.

```json
{
  "content": [{
    "id": 10, "userId": 7, "userName": "홍길동", "userEmail": "user1@test.com",
    "orderId": "TICKET_...", "ticketCount": 1, "amount": 100, "status": "READY",
    "paymentKey": null, "createdAt": "2026-07-24T09:40:47", "approvedAt": null
  }],
  "page": 0, "size": 10, "totalElements": 10, "totalPages": 1,
  "summary": {"total": 10, "paid": 0, "ready": 10, "failed": 0}
}
```

## 수업 일정

| Method | Path | 권한 | 설명 |
|---|---|---|---|
| GET | `/api/schedules` | CUSTOMER, TRAINER, ADMIN | 예약 가능한 수업 목록 |
| GET | `/api/schedules/{id}` | CUSTOMER, TRAINER, ADMIN | 수업 상세 |
| GET | `/api/admin/schedules` | ADMIN | 전체 수업 목록 (트레이너·상태·기간 무관, 보관 제외) |
| POST | `/api/trainer/schedules` | TRAINER | 단일 일정 생성 |
| GET | `/api/trainer/schedules` | TRAINER | 내 일정 조회 |
| PUT | `/api/trainer/schedules/{id}` | TRAINER | 내 일정 수정 |
| PATCH | `/api/trainer/schedules/{id}/cancel` | TRAINER | 일정 취소 |
| PATCH | `/api/trainer/schedules/{id}/restore` | TRAINER | 취소 일정 복원 |
| POST | `/api/trainer/schedules/refresh` | TRAINER | 취소 일정 삭제·보관 및 목록 갱신 |
| GET | `/api/trainer/schedules/{id}/reservations` | TRAINER | 내 수업의 예약자 명단 (본인 수업만, 아니면 403) |

## 반복 일정

| Method | Path | 권한 | 설명 |
|---|---|---|---|
| POST | `/api/trainer/schedule-templates` | TRAINER | 반복 규칙과 실제 수업 생성 |
| GET | `/api/trainer/schedule-templates` | TRAINER | 내 반복 규칙 조회 |
| PATCH | `/api/trainer/schedule-templates/{id}/deactivate` | TRAINER | 반복 규칙 비활성화 |

## 예약

| Method | Path | 권한 | 설명 |
|---|---|---|---|
| POST | `/customer/reservations/schedules/{scheduleId}` | CUSTOMER | 수업 예약·수강권 차감 |
| GET | `/customer/reservations/me` | CUSTOMER | 내 예약 내역 |
| PATCH | `/customer/reservations/{reservationId}/cancel` | CUSTOMER | 예약 취소·수강권 복구 |

## 대표 오류

- `400 Bad Request`: 입력값, 상태, 금액, 일정 충돌 오류
- `401 Unauthorized`: 로그인 토큰 누락·만료·검증 실패
- `403 Forbidden`: 역할 또는 소유권 부족
- `404 Not Found`: 회원·수업·예약·주문을 찾지 못함
