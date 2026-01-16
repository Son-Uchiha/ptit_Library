/**
 * PTIT Library - API Service
 * Handles all API calls to the backend
 */

const API_BASE_URL = "http://localhost:8080/api";

// Token management
const TokenService = {
  getToken: () => localStorage.getItem("accessToken"),
  setToken: (token) => localStorage.setItem("accessToken", token),
  removeToken: () => localStorage.removeItem("accessToken"),
  getUser: () => {
    const user = localStorage.getItem("user");
    return user ? JSON.parse(user) : null;
  },
  setUser: (user) => localStorage.setItem("user", JSON.stringify(user)),
  removeUser: () => localStorage.removeItem("user"),
  isLoggedIn: () => !!localStorage.getItem("accessToken"),
};

// API Client
const api = {
  /**
   * Make API request
   */
  async request(endpoint, options = {}) {
    const url = `${API_BASE_URL}${endpoint}`;
    const token = TokenService.getToken();

    const config = {
      headers: {
        "Content-Type": "application/json",
        ...(token && { Authorization: `Bearer ${token}` }),
        ...options.headers,
      },
      ...options,
    };

    try {
      const response = await fetch(url, config);
      const data = await response.json();

      if (!response.ok) {
        // Nếu có lỗi validation chi tiết
        if (data.data && typeof data.data === "object") {
          const errors = Object.values(data.data).join(", ");
          throw new Error(errors || data.message || "Có lỗi xảy ra");
        }
        throw new Error(data.message || "Có lỗi xảy ra");
      }

      return data;
    } catch (error) {
      console.error("API Error:", error);
      throw error;
    }
  },

  get(endpoint) {
    return this.request(endpoint, { method: "GET" });
  },

  post(endpoint, body) {
    return this.request(endpoint, {
      method: "POST",
      body: JSON.stringify(body),
    });
  },

  put(endpoint, body) {
    return this.request(endpoint, {
      method: "PUT",
      body: JSON.stringify(body),
    });
  },

  delete(endpoint) {
    return this.request(endpoint, { method: "DELETE" });
  },
};

// Auth API
const AuthAPI = {
  login: (username, password) => api.post("/auth/login", { username, password }),
  register: (data) => api.post("/auth/register", data),
  forgotPassword: (email) => api.post(`/auth/forgot-password?email=${encodeURIComponent(email)}`),
  refreshToken: () => api.post("/auth/refresh-token"),
  logout: () => {
    TokenService.removeToken();
    TokenService.removeUser();
  },
};

// User API
const UserAPI = {
  getMe: () => api.get("/users/me"),
  getByUsername: (username) => api.get(`/users/${username}`),
  updateProfile: (data) => api.put("/users/me", data),
  changePassword: (data) => api.put("/users/me/password", data),
};

// Book API
const BookAPI = {
  getAll: () => api.get("/books"),
  getById: (id) => api.get(`/books/${id}`),
  search: (keyword, filters = {}) => {
    const params = new URLSearchParams({ keyword, ...filters });
    return api.get(`/books/search?${params}`);
  },
};

// Borrow API
const BorrowAPI = {
  borrowBooks: (bookIds) => api.post("/borrows", { bookIds }),
  getRecords: (username = "") => api.get(`/borrows/records${username ? `?username=${username}` : ""}`),
  getAllRecords: (status = "") => api.get(`/borrows/all${status ? `?status=${status}` : ""}`),
  issueRecord: (recordId) => api.post(`/borrows/${recordId}/issue`),
  returnRecord: (recordId) => api.post(`/borrows/${recordId}/return`),
};

// Notification API
const NotificationAPI = {
  getAll: () => api.get("/notifications"),
  getUnread: () => api.get("/notifications/unread"),
  markAsRead: (id) => api.put(`/notifications/${id}/read`),
  markAllAsRead: () => api.put("/notifications/read-all"),
};

// Message API
const MessageAPI = {
  send: (receiverId, content) => api.post("/messages", { receiverId, content }),
  getConversation: (userId) => api.get(`/messages/conversation/${userId}`),
  getUnread: () => api.get("/messages/unread"),
  getConversations: () => api.get("/messages/conversations"),
  markAsRead: (id) => api.put(`/messages/${id}/read`),
};

// Friend API
const FriendAPI = {
  getAll: () => api.get("/friends"),
  sendRequest: (to) => api.post(`/friends/request?to=${encodeURIComponent(to)}`),
  accept: (of) => api.post(`/friends/accept?of=${encodeURIComponent(of)}`),
  decline: (of) => api.post(`/friends/decline?of=${encodeURIComponent(of)}`),
  remove: (username) => api.delete(`/friends/${username}`),
  getPendingReceived: () => api.get("/friends/pending/received"),
  getPendingSent: () => api.get("/friends/pending/sent"),
  isFriends: (username) => api.get(`/friends/is-friends/${username}`),
};

// Ranking API
const RankingAPI = {
  get: (page = 1, size = 20) => api.get(`/ranking?page=${page}&size=${size}`),
};

// Export
window.TokenService = TokenService;
window.AuthAPI = AuthAPI;
window.UserAPI = UserAPI;
window.BookAPI = BookAPI;
window.BorrowAPI = BorrowAPI;
window.NotificationAPI = NotificationAPI;
window.MessageAPI = MessageAPI;
window.FriendAPI = FriendAPI;
window.RankingAPI = RankingAPI;
