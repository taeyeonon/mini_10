# 시스템 아키텍처

## 전체 구조

```mermaid
flowchart LR
    U["브라우저<br/>HTML/JavaScript"] -->|"JWT · REST API"| S["Spring Security"]
    S --> C["Controller"]
    C --> V["Service / Transaction"]
    V --> R["JPA Repository"]
    R --> DB[("MySQL mini_10")]
    U -->|"결제 인증"| TUI["Toss Payments SDK"]
    V -->|"결제 승인 · 금액 검증"| TAPI["Toss Payments API"]
```

## 백엔드 계층

```mermaid
flowchart TB
    AUTH["auth / jwt / config<br/>로그인·인증·권한"]
    USER["user<br/>회원·역할"]
    SCHEDULE["schedule<br/>단일·반복 일정"]
    TICKET["ticket<br/>발급·차감·복구"]
    RESERVATION["reservation<br/>예약·취소"]
    PAYMENT["payment<br/>주문·승인·자동 지급"]

    AUTH --> USER
    SCHEDULE --> USER
    TICKET --> USER
    RESERVATION --> USER
    RESERVATION --> SCHEDULE
    RESERVATION --> TICKET
    PAYMENT --> USER
    PAYMENT --> TICKET
```

## 권한 모델

| 역할 | 주요 권한 |
|---|---|
| CUSTOMER | 수업 조회, 예약·취소, 내 수강권 조회, 수강권 결제 |
| TRAINER | 본인 일정 및 반복 일정 생성·조회·취소·복원 |
| ADMIN | 회원 목록 조회, 수강권 수동 발급 |

JWT는 `X-AUTH-TOKEN` 헤더로 전달됩니다. 화면의 권한 확인은 편의 기능이며 최종 권한 검사는 Spring Security와 서비스의 소유권 검사에서 수행합니다.

## 핵심 트랜잭션

### 반복 일정 생성

```mermaid
sequenceDiagram
    participant T as 트레이너
    participant S as ScheduleTemplateService
    participant DB as MySQL
    T->>S: 반복 일정 등록
    S->>DB: 기간·요일·시간 중복 검사
    S->>DB: 반복 규칙 저장
    S->>DB: 해당 요일의 실제 수업 일괄 저장
    alt 하나라도 실패
        S-->>DB: 전체 롤백
    else 성공
        S-->>T: 생성 결과
    end
```

### 예약

```mermaid
sequenceDiagram
    participant C as 회원
    participant R as ReservationService
    participant DB as MySQL
    C->>R: 수업 예약
    R->>DB: 일정 상태·정원·중복 확인
    R->>DB: 사용 가능한 수강권 1회 차감
    R->>DB: 예약 저장·예약 인원 증가
    R-->>C: 예약 결과
```

### 결제

```mermaid
sequenceDiagram
    participant C as 회원
    participant P as PaymentService
    participant T as Toss Payments
    participant DB as MySQL
    C->>P: 수강권 주문 생성
    P->>DB: 주문번호·서버 계산 금액 저장
    C->>T: 결제 인증
    C->>P: paymentKey·orderId·amount
    P->>DB: 주문자·상태·금액 검증
    P->>T: 결제 승인
    T-->>P: DONE·최종 금액
    P->>DB: PAID 기록·수강권 지급
    P-->>C: 구매 완료
```
