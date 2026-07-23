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
  window.location.href = '/login.html';
}
