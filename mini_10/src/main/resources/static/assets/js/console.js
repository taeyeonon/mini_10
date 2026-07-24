/* 관리자·트레이너 콘솔 공통 스크립트 (common.js 다음에 로드) */

const CONSOLE_SETTINGS_KEY = 'consoleSettings';
const DEFAULT_CONSOLE_SETTINGS = { pageSize: 10 };

function loadConsoleSettings() {
  try {
    return { ...DEFAULT_CONSOLE_SETTINGS, ...JSON.parse(localStorage.getItem(CONSOLE_SETTINGS_KEY) || '{}') };
  } catch (error) {
    return { ...DEFAULT_CONSOLE_SETTINGS };
  }
}

function saveConsoleSettings(settings) {
  localStorage.setItem(CONSOLE_SETTINGS_KEY, JSON.stringify({ ...loadConsoleSettings(), ...settings }));
}

/**
 * 콘솔 화면 공통 초기화: 권한 가드 → 사이드바/헤더 렌더링.
 * @param {{brand: string, roleLabel: string, roles: string[], menus: object[], activeKey: string}} config
 * @returns {boolean} 가드를 통과하면 true
 */
function initConsolePage(config) {
  if (!requireAuth(...config.roles)) return false;

  const sidebar = document.querySelector('.sidebar');
  if (sidebar) {
    sidebar.innerHTML = `<div class="brand">${config.brand}</div>`
        + '<div class="nav-section">메뉴</div>'
        + config.menus.map(menu => `<a class="nav-item ${menu.key === config.activeKey ? 'active' : ''}"
            href="${menu.href}">${menu.icon} ${menu.label}</a>`).join('');
  }

  const user = getCurrentUserInfo();
  const roleBox = document.querySelector('.profile-text .role');
  if (roleBox) roleBox.textContent = user.name || config.roleLabel;
  const emailBox = document.querySelector('.profile-text .email');
  if (emailBox) emailBox.textContent = user.email || '-';
  const avatar = document.querySelector('.avatar');
  if (avatar) avatar.textContent = (user.name || user.email || '?').charAt(0).toUpperCase();
  const today = document.querySelector('.today');
  if (today) {
    today.textContent = new Date().toLocaleDateString('ko-KR',
        { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' });
  }
  return true;
}

/** JWT 를 실어 호출하고 에러 응답을 예외로 바꾼다. 401 이면 로그인으로 보낸다. */
async function consoleRequest(url, options = {}) {
  const response = await jwtFetch(url, options);
  if (response.status === 401) {
    location.replace('/login.html');
    throw new Error('로그인이 필요합니다.');
  }
  const text = await response.text();
  let data = null;
  if (text) {
    try { data = JSON.parse(text); } catch (_) { data = text; }
  }
  if (response.status === 403) {
    throw new Error(data?.message || '접근 권한이 없습니다.');
  }
  if (!response.ok) {
    throw new Error(data?.message || data || `요청 실패 (${response.status})`);
  }
  return data;
}

function formatDateTime(value) {
  return value ? value.replace('T', ' ').slice(0, 16) : '-';
}

function formatDate(value) {
  return value ? value.slice(0, 10) : '-';
}

function formatTime(value) {
  if (!value) return '-';
  return value.includes('T') ? value.split('T')[1].slice(0, 5) : value.slice(0, 5);
}

function escapeHtml(value) {
  if (value === null || value === undefined) return '';
  return String(value).replace(/[&<>"']/g,
      char => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[char]));
}

const PAYMENT_STATUS_BADGE = {
  READY: { className: 'badge-ready', label: '결제 대기' },
  PAID: { className: 'badge-paid', label: '결제 완료' },
  FAILED: { className: 'badge-canceled', label: '결제 실패' },
  CANCELED: { className: 'badge-canceled', label: '결제 취소' }
};

const SCHEDULE_STATUS_BADGE = {
  OPEN: { className: 'badge-paid', label: '오픈' },
  COMPLETED: { className: 'badge-info', label: '완료' },
  CANCELLED: { className: 'badge-canceled', label: '취소' }
};

const RESERVATION_STATUS_BADGE = {
  CONFIRMED: { className: 'badge-paid', label: '예약 확정' },
  CANCELLED: { className: 'badge-canceled', label: '예약 취소' }
};

const ROLE_BADGE = {
  ADMIN: { className: 'badge-canceled', label: '관리자' },
  TRAINER: { className: 'badge-ready', label: '트레이너' },
  CUSTOMER: { className: 'badge-info', label: '회원' }
};

const DAY_OF_WEEK_LABEL = {
  MONDAY: '월요일', TUESDAY: '화요일', WEDNESDAY: '수요일', THURSDAY: '목요일',
  FRIDAY: '금요일', SATURDAY: '토요일', SUNDAY: '일요일'
};

function renderBadge(map, key) {
  const badge = map[key] || { className: 'badge-etc', label: key ?? '-' };
  return `<span class="badge ${badge.className}">${escapeHtml(badge.label)}</span>`;
}

/** 정원 대비 예약 게이지. */
function renderGauge(reservedCount, capacity) {
  const percent = capacity > 0 ? Math.min(Math.round((reservedCount / capacity) * 100), 100) : 0;
  return `<div class="gauge ${reservedCount >= capacity ? 'is-full' : ''}"><span style="width:${percent}%"></span></div>`;
}

/**
 * 페이지 번호 버튼을 그린다. 컨테이너의 click 이벤트에서 data-page 를 읽어 쓰면 된다.
 * @param {HTMLElement} container
 * @param {{page:number, totalPages:number}} pageInfo
 */
function renderPagination(container, pageInfo) {
  const { page, totalPages } = pageInfo;
  if (!totalPages) {
    container.innerHTML = '';
    return;
  }
  // 현재 페이지 기준 최대 5개 번호만 노출
  const start = Math.max(0, Math.min(page - 2, totalPages - 5));
  const end = Math.min(totalPages, start + 5);

  let html = `<button ${page === 0 ? 'disabled' : ''} data-page="${page - 1}">이전</button>`;
  for (let index = start; index < end; index++) {
    html += `<button class="${index === page ? 'current' : ''}" data-page="${index}">${index + 1}</button>`;
  }
  html += `<button ${page >= totalPages - 1 ? 'disabled' : ''} data-page="${page + 1}">다음</button>`;
  container.innerHTML = html;
}

/** 배열을 클라이언트에서 잘라 쓰는 페이지네이션 (전체 목록 API용). */
function slicePage(items, page, size) {
  const totalPages = Math.ceil(items.length / size);
  const current = Math.min(Math.max(page, 0), Math.max(totalPages - 1, 0));
  return {
    content: items.slice(current * size, current * size + size),
    page: current, totalPages, totalElements: items.length
  };
}

/** 목록 하단의 페이지네이션 + 건수 안내를 한 번에 갱신한다. */
function renderPageFooter(pagination, pageInfoBox, result, unit = '건') {
  renderPagination(pagination, result);
  pageInfoBox.textContent = result.totalPages
      ? `전체 ${result.totalElements.toLocaleString()}${unit} · ${result.page + 1} / ${result.totalPages} 페이지`
      : '';
}

/** toISOString 은 UTC 기준이라 하루 밀릴 수 있어 로컬 날짜로 만든다. */
function toDateInput(date) {
  const month = String(date.getMonth() + 1).padStart(2, '0');
  return `${date.getFullYear()}-${month}-${String(date.getDate()).padStart(2, '0')}`;
}

function toDateTimeInput(date) {
  const hour = String(date.getHours()).padStart(2, '0');
  return `${toDateInput(date)}T${hour}:${String(date.getMinutes()).padStart(2, '0')}`;
}
