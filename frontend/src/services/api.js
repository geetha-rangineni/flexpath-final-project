import axios from 'axios';

// Create an Axios instance
const API = axios.create({
  baseURL: 'http://localhost:8080/api', // Update this if different
});

// Attach the token (if exists) to every request
API.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default API;

// Optional: named exports for convenience
export const login = (credentials) => API.post('/auth/login', credentials);
export const fetchPublicItems = () => API.get('/items/public');
export const fetchPrivateItems = () => API.get('/items/private');
