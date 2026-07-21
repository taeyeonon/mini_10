// SessionStorage에서 JWT 토큰 읽기
function getJwtToken() {
  return sessionStorage.getItem('accessToken');
}

// JWT 토큰을 헤더(X-AUTH-TOKEN)에 자동으로 붙여서 fetch 호출하는 공통 함수
async function jwtFetch(url, options = {}) {
  const token = getJwtToken();
  const headers = options.headers ? { ...options.headers } : {};

  // JWT 토큰이 있으면 헤더에 추가
  if (token) {
    headers['X-AUTH-TOKEN'] = token;
  }

  const fetchOptions = {
    method: options.method || 'GET',
    headers: headers
  };

  if (options.body !== undefined) {
    fetchOptions.body = options.body;
  }

  return fetch(url, fetchOptions);
}

// JWT 로그아웃 (Stateless이므로 클라이언트 토큰 삭제)
function jwtLogout() {
  sessionStorage.removeItem('accessToken');
  alert('로그아웃 되었습니다.');
  window.location.href = '/login.html';
}