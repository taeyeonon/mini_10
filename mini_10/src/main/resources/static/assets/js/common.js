function getJwtToken() {
  return sessionStorage.getItem('accessToken');
}

function getJwtPayload() {
  const token = getJwtToken();
  if (!token) return null;
  try {
    const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
    const padded = base64.padEnd(Math.ceil(base64.length / 4) * 4, '=');
    return JSON.parse(decodeURIComponent(escape(atob(padded))));
  } catch (error) {
    return null;
  }
}

function getCurrentUserInfo() {
  const payload = getJwtPayload() || {};
  let profile = {};
  try {
    profile = JSON.parse(sessionStorage.getItem('userProfile') || '{}');
  } catch (error) {
    profile = {};
  }
  const roles = (profile.roles || payload.roles || payload.auth || [])
      .map(role => role.replace(/^ROLE_/, ''));
  return {
    name: profile.name || '',
    email: profile.email || payload.sub || '',
    roles
  };
}

function getCurrentUserLabel() {
  const user = getCurrentUserInfo();
  let title = '로그인 사용자';
  if (user.roles.includes('ADMIN')) title = '관리자';
  else if (user.roles.includes('TRAINER')) title = '트레이너';
  else if (user.roles.includes('CUSTOMER')) title = `${user.name || '회원'} 회원`;
  return user.email ? `${title} (${user.email})` : title;
}

function renderCurrentUser(selector = '.current-user') {
  document.querySelectorAll(selector).forEach(element => {
    element.textContent = getCurrentUserLabel();
  });
}

function getCurrentRoles() {
  return getCurrentUserInfo().roles;
}

function hasRole(...roles) {
  const mine = getCurrentRoles();
  return roles.some(role => mine.includes(role));
}

/** 역할별 기본 진입 페이지. ADMIN > TRAINER > CUSTOMER 순으로 우선한다. */
function getHomePathByRole() {
  if (hasRole('ADMIN')) return '/admin-dashboard.html';
  if (hasRole('TRAINER')) return '/trainer-dashboard.html';
  if (hasRole('CUSTOMER')) return '/customer.html';
  return '/board.html';
}

/** 토큰 만료 여부. exp 가 없으면 만료되지 않은 것으로 본다. */
function isTokenExpired() {
  const payload = getJwtPayload();
  if (!payload || !payload.exp) return false;
  return payload.exp * 1000 <= Date.now();
}

/**
 * 페이지 진입 가드. 토큰이 없거나 만료됐으면 login.html 로, 역할이 모자라면 자기 역할의 홈으로 보낸다.
 * 서버(SecurityConfig)의 권한 검사가 실제 방어선이고 이 함수는 화면 흐름용이다.
 * @param {...string} roles 허용 역할. 비우면 로그인 여부만 확인한다.
 * @returns {boolean} 통과하면 true, 리다이렉트했으면 false
 */
function requireAuth(...roles) {
  if (!getJwtToken() || isTokenExpired()) {
    sessionStorage.removeItem('accessToken');
    sessionStorage.removeItem('userProfile');
    location.replace('/login.html');
    return false;
  }
  if (roles.length && !hasRole(...roles)) {
    alert('접근 권한이 없습니다.');
    location.replace(getHomePathByRole());
    return false;
  }
  return true;
}

async function jwtFetch(url, options = {}) {
  const token = getJwtToken();
  const headers = options.headers ? { ...options.headers } : {};
  if (token) headers['X-AUTH-TOKEN'] = token;

  const fetchOptions = {
    method: options.method || 'GET',
    headers
  };
  if (options.body !== undefined) fetchOptions.body = options.body;
  return fetch(url, fetchOptions);
}

function jwtLogout() {
  sessionStorage.removeItem('accessToken');
  sessionStorage.removeItem('userProfile');
  alert('로그아웃 되었습니다.');
  window.location.replace('/login.html');
}
