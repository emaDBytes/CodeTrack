// src\main\resources\static\js\csrf.js

// Get CSRF token from meta tag
function getCsrfToken() {
  return document.querySelector("meta[name='_csrf']").getAttribute("content");
}

// Get CSRF header name from meta tag
function getCsrfHeaderName() {
  return document
    .querySelector("meta[name='_csrf_header']")
    .getAttribute("content");
}

// Add CSRF token to fetch requests
async function fetchWithCsrf(url, options = {}) {
  const csrfToken = getCsrfToken();
  const csrfHeaderName = getCsrfHeaderName();

  // Ensure headers object exists
  options.headers = options.headers || {};

  // Add CSRF header
  options.headers[csrfHeaderName] = csrfToken;

  // Make the fetch request
  return await fetch(url, options);
}
