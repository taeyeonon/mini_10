# mini_10 헬스/PT 예약 시스템

LG유플러스 유레카 백엔드 과정 미니프로젝트 10조의 헬스 PT·필라테스 수업 예약 및 수강권 관리 시스템입니다.

## 주요 기능

- JWT 기반 회원가입·로그인 및 `CUSTOMER`, `TRAINER`, `ADMIN` 권한 제어
- 트레이너 단일/요일별 반복 수업 생성, 조회, 취소, 복원
- 회원 수업 조회, 예약, 취소 및 수강권 차감·복구
- 관리자 회원 선택 및 수강권 수동 발급
- 토스페이먼츠 테스트 결제 승인 후 수강권 자동 지급
- 반복 일정·예약·결제의 중복 및 금액 조작 방지

## 기술 스택

- Java 21, Spring Boot 4.1
- Spring MVC, Spring Data JPA, Spring Security, JWT
- MySQL 8, Gradle
- JUnit 5, Mockito
- HTML, CSS, Vanilla JavaScript
- Toss Payments JavaScript SDK V2 / 결제 승인 API

## 빠른 실행

### 1. DB 생성

```sql
CREATE DATABASE mini_10 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

기본 로컬 설정은 `root / 1234`, DB 이름은 `mini_10`입니다. 팀원별 환경이 다르면 `mini_10/src/main/resources/application.properties`의 DB 설정을 변경하거나 환경별 설정 파일을 사용하세요.

### 2. 토스 테스트 키 설정

공유 테스트 클라이언트 키는 프로젝트에 기본 설정되어 있습니다. 결제 승인에 필요한 서버 시크릿 키만 IntelliJ 실행 구성 또는 OS 환경변수에 설정합니다.

```text
TOSS_SECRET_KEY=test_sk_...
```

필요하면 `TOSS_CLIENT_KEY` 환경변수로 기본 클라이언트 키를 덮어쓸 수 있습니다. 토스 결제를 사용하지 않는 기능은 시크릿 키 없이도 실행할 수 있습니다.

### 3. 애플리케이션 실행

IntelliJ에서 `mini_10/src/main/java/com/mycom/myapp/Mini10Application.java`를 실행하거나:

```powershell
cd mini_10
.\gradlew.bat bootRun
```

접속 주소: `http://localhost:8080`

### 4. 테스트

```powershell
cd mini_10
.\gradlew.bat test
```

프로젝트 경로에 한글·공백이 있을 때 Gradle 테스트 워커가 클래스를 찾지 못하면 영문 경로로 프로젝트를 옮겨 실행하세요.

## 샘플 계정

서버 시작 시 이메일 중복을 검사한 뒤 없는 계정만 생성하므로 재실행해도 중복 오류가 발생하지 않습니다.

| 역할 | 이메일 | 비밀번호 |
|---|---|---|
| 관리자 | `admin@test.com` | `admin1234` |
| 트레이너 | `trainer@test.com` | `trainer1234` |
| 회원 | `customer@test.com` | `customer1234` |

비밀번호는 DB에 BCrypt 해시로 저장됩니다. 샘플 계정은 로컬 개발·시연용이며 운영 환경에서는 비활성화해야 합니다.

## 역할별 사용 흐름

1. 관리자는 회원을 이름·이메일로 선택해 수강권을 발급합니다.
2. 트레이너는 단일 일정 또는 요일별 반복 일정을 생성합니다.
3. 회원은 수강권을 구매하거나 발급받은 뒤 수업을 예약합니다.
4. 예약 시 수강권이 1회 차감되고, 정책상 취소 가능한 예약은 수강권이 복구됩니다.

## 문서

- [시스템 아키텍처](docs/architecture.md)
- [ERD 및 스키마](docs/erd.md)
- [DB 스키마·샘플 데이터](docs/database.md)
- [API 명세](docs/api.md)
- [운영·테스트 가이드](docs/operations.md)

## 주의사항

- 토스 시크릿 키, 운영 DB 비밀번호, JWT 운영 키를 Git에 올리지 마세요.
- 현재 결제 가격은 시연을 위해 1회당 100원으로 설정되어 있습니다.
- 결제 성공은 브라우저 결과만 믿지 않고 서버 주문 금액과 토스 승인 결과를 검증합니다.
- `spring.jpa.hibernate.ddl-auto=update`는 개발 편의 설정입니다. 운영 환경에서는 Flyway 또는 Liquibase 도입을 권장합니다.
