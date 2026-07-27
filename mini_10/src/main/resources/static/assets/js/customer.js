/* 회원 콘솔 메뉴 정의 (console.js 다음에 로드) */

const CUSTOMER_MENUS = [
  { key: 'dashboard', label: '대시보드', icon: '🏠', href: '/customer-dashboard.html' },
  { key: 'ticket', label: '수강권', icon: '🎟️', href: '/customer-ticket.html' },
  { key: 'reservation', label: '예약', icon: '📅', href: '/customer-reservation.html' }
];

/** CUSTOMER 가드 + 회원 사이드바 렌더링. ADMIN 도 열람할 수 있다. */
function initCustomerPage(activeKey) {
  return initConsolePage({
    brand: '🏋️ Fit Manager Member',
    roleLabel: '회원',
    roles: ['CUSTOMER', 'ADMIN'],
    menus: CUSTOMER_MENUS,
    activeKey
  });
}

const customerRequest = consoleRequest;
