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
| GET | `/admin/users/customers` | ADMIN | 수강권 발급용 회원 목록 |

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

주문 생성 요청:

```json
{"ticketCount": 10}
```

결제 승인 요청:

```json
{"paymentKey":"...","orderId":"TICKET_...","amount":1000}
```

## 수업 일정

| Method | Path | 권한 | 설명 |
|---|---|---|---|
| GET | `/api/schedules` | CUSTOMER, TRAINER, ADMIN | 예약 가능한 수업 목록 |
| GET | `/api/schedules/{id}` | CUSTOMER, TRAINER, ADMIN | 수업 상세 |
| POST | `/api/trainer/schedules` | TRAINER | 단일 일정 생성 |
| GET | `/api/trainer/schedules` | TRAINER | 내 일정 조회 |
| PUT | `/api/trainer/schedules/{id}` | TRAINER | 내 일정 수정 |
| PATCH | `/api/trainer/schedules/{id}/cancel` | TRAINER | 일정 취소 |
| PATCH | `/api/trainer/schedules/{id}/restore` | TRAINER | 취소 일정 복원 |
| POST | `/api/trainer/schedules/refresh` | TRAINER | 취소 일정 삭제·보관 및 목록 갱신 |

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
