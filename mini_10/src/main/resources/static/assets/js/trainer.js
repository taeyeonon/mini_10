/* 트레이너 콘솔 메뉴 정의 (console.js 다음에 로드) */

const TRAINER_MENUS = [
  { key: 'dashboard', label: '대시보드', icon: '📊', href: '/trainer-dashboard.html' },
  { key: 'schedule', label: '수업 일정', icon: '🗓️', href: '/trainer.html' },
  { key: 'template', label: '반복 일정', icon: '🔁', href: '/trainer-template.html' },
  { key: 'reservation', label: '예약 현황', icon: '🙋', href: '/trainer-reservation.html' },
  { key: 'settings', label: '설정', icon: '⚙️', href: '/trainer-settings.html' }
];

/** TRAINER 가드 + 트레이너 사이드바 렌더링. ADMIN 도 열람할 수 있다. */
function initTrainerPage(activeKey) {
  return initConsolePage({
    brand: '🏋️ FitPass Trainer',
    roleLabel: '트레이너',
    roles: ['TRAINER', 'ADMIN'],
    menus: TRAINER_MENUS,
    activeKey
  });
}

const trainerRequest = consoleRequest;
