/* 관리자 콘솔 메뉴 정의 (console.js 다음에 로드) */

const ADMIN_MENUS = [
  { key: 'dashboard', label: '대시보드', icon: '📊', href: '/admin-dashboard.html' },
  { key: 'payment', label: '수강권/결제 관리', icon: '💳', href: '/admin-payment.html' },
  { key: 'schedule', label: '수업 스케줄', icon: '🗓️', href: '/admin-schedule.html' },
  { key: 'user', label: '회원 관리', icon: '👥', href: '/admin-user.html' },
  { key: 'settings', label: '설정', icon: '⚙️', href: '/admin-settings.html' }
];

/** ADMIN 가드 + 관리자 사이드바 렌더링. */
function initAdminPage(activeKey) {
  return initConsolePage({
    brand: '🏋️ FitPass Admin',
    roleLabel: '관리자',
    roles: ['ADMIN'],
    menus: ADMIN_MENUS,
    activeKey
  });
}

// 관리자 화면에서 쓰던 이름 유지
const adminRequest = consoleRequest;
const loadAdminSettings = loadConsoleSettings;
const saveAdminSettings = saveConsoleSettings;
