# DB 스키마와 샘플 데이터

## 생성 방식

- 로컬 개발에서는 JPA 엔티티와 `spring.jpa.hibernate.ddl-auto=update` 설정으로 테이블을 생성·갱신합니다.
- 최초 실행 전에 MySQL에 `mini_10` 데이터베이스만 생성하면 됩니다.
- 역할과 시연용 사용자는 `DataInitializer`가 서버 시작 시 자동 생성합니다.
- 운영 환경에서는 `ddl-auto=update` 대신 Flyway 또는 Liquibase로 버전 관리하는 것을 권장합니다.

```sql
CREATE DATABASE mini_10
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

## 사용자 관련 스키마

```text
user
├─ id              BIGINT PK AUTO_INCREMENT
├─ name            VARCHAR(255)
├─ email           VARCHAR(255)
└─ password        VARCHAR(255)  -- BCrypt 해시

user_role
├─ id              INT PK AUTO_INCREMENT
└─ name            VARCHAR(255)  -- CUSTOMER, TRAINER, ADMIN

user_user_role
├─ user_id         BIGINT FK → user.id
└─ user_role_id    INT FK → user_role.id
```

회원가입 시 기본 역할은 `CUSTOMER`입니다. Spring Security에서는 DB의 역할명에 `ROLE_` 접두사를 붙여 권한으로 사용합니다.

## 주요 업무 테이블

| 테이블 | 용도 | 주요 관계·제약조건 |
|---|---|---|
| `ticket` | 회원 수강권과 잔여 횟수 | `user_id → user.id` |
| `trainer_schedule_template` | 요일별 반복 일정 규칙 | `trainer_id → user.id`, 활성 여부와 적용 기간 보관 |
| `trainer_schedule` | 예약 가능한 실제 수업 | `trainer_id → user.id`, 선택적으로 `template_id` 연결 |
| `reservation` | 회원 예약 내역 | 회원·수업 조합 유니크, 수강권 연결 |
| `payment_order` | 토스 결제 주문과 승인 상태 | `order_id`, `payment_key` 유니크 |

전체 컬럼과 관계는 [ERD 및 스키마](erd.md)를 참고합니다.

## 자동 생성 샘플 데이터

`DataInitializer`는 역할과 이메일을 먼저 조회하고 없는 데이터만 저장합니다. 따라서 서버를 여러 번 실행해도 동일한 샘플 계정이 중복 생성되지 않습니다.

| 역할 | 이름 | 이메일 | 초기 비밀번호 |
|---|---|---|---|
| ADMIN | Admin User | `admin@test.com` | `admin1234` |
| TRAINER | Trainer User | `trainer@test.com` | `trainer1234` |
| CUSTOMER | Customer User | `customer@test.com` | `customer1234` |

비밀번호는 평문으로 저장되지 않고 실행 시 BCrypt로 암호화됩니다. 이미 같은 이메일의 사용자가 있다면 기존 이름·비밀번호·역할을 덮어쓰지 않습니다.

## 확인 SQL

```sql
SELECT u.id, u.name, u.email, ur.name AS role_name
FROM user u
JOIN user_user_role uur ON uur.user_id = u.id
JOIN user_role ur ON ur.id = uur.user_role_id
ORDER BY u.id;

SELECT id, user_id, total_count, remaining_count, start_date, end_date
FROM ticket
ORDER BY id DESC;
```

MySQL 버전이나 설정에 따라 `user`가 예약어로 인식되면 테이블명을 백틱으로 감싸서 ``SELECT * FROM `user`;``처럼 실행합니다.
