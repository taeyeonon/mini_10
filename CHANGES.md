# Changes

- Refactored ticket consumption logic to remove duplication between the reservation service and ticket service.
  - `ReservationService` now delegates ticket deduction to `TicketService.useTicketAndGet(Long userId)`.
  - `TicketService` interface now exposes `useTicketAndGet(Long userId)` in addition to `useTicket(Long userId)`.
  - `TicketServiceImpl` now centralizes ticket lookup and `use()` logic.

- Clarified the role of `/customer/tickets/use` in `CustomerTicketController`.
  - This endpoint is intended for direct/manual ticket consumption.
  - Reservation flow (`ReservationController.reserve`) consumes tickets internally and does not require a frontend call to this endpoint.

- Added JUnit test coverage for signup and login functionality:
  - `UserServiceImplTest` verifies signup user creation with encoded password and default CUSTOMER role.
  - `LoginServiceImplTest` verifies login success and JWT/token creation flow.
  - `UserControllerTest` verifies `/users` registration endpoint mapping and response.
  - `LoginControllerTest` verifies `/auth/login` endpoint mapping and response.
