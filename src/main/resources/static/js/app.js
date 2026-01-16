/**
 * PTIT Library - Main Application
 * Single Page Application with Client-side Routing
 */

class App {
  constructor() {
    this.currentPage = "home";
    this.init();
  }

  init() {
    this.bindEvents();
    this.checkAuth();

    // Khôi phục trang từ localStorage hoặc hash URL
    const savedPage = localStorage.getItem("currentPage") || window.location.hash.replace("#", "") || "home";
    this.navigate(savedPage);
  }

  bindEvents() {
    // Navigation clicks
    document.querySelectorAll("[data-page]").forEach((el) => {
      el.addEventListener("click", (e) => {
        e.preventDefault();
        this.navigate(el.dataset.page);
      });
    });

    // Notification dropdown toggle
    const notificationBtn = document.getElementById("notificationBtn");
    const notificationDropdown = document.getElementById("notificationDropdown");
    if (notificationBtn && notificationDropdown) {
      notificationBtn.addEventListener("click", (e) => {
        e.preventDefault();
        e.stopPropagation();
        notificationDropdown.classList.toggle("active");
        if (notificationDropdown.classList.contains("active")) {
          this.loadNotifications();
        }
      });
    }

    // Mark all as read
    document.getElementById("markAllReadBtn")?.addEventListener("click", () => {
      this.markAllNotificationsRead();
    });

    // Close notification dropdown when clicking outside
    document.addEventListener("click", (e) => {
      const notificationDropdown = document.getElementById("notificationDropdown");
      if (notificationDropdown && !notificationDropdown.contains(e.target)) {
        notificationDropdown.classList.remove("active");
      }
    });

    // Logout
    document.getElementById("logoutBtn")?.addEventListener("click", (e) => {
      e.preventDefault();
      this.logout();
    });

    // Modal close
    document.getElementById("modalClose")?.addEventListener("click", () => this.closeModal());
    document.getElementById("modalOverlay")?.addEventListener("click", (e) => {
      if (e.target === e.currentTarget) this.closeModal();
    });
  }

  checkAuth() {
    const isLoggedIn = TokenService.isLoggedIn();
    const user = TokenService.getUser();

    document.getElementById("userDropdown").style.display = isLoggedIn ? "block" : "none";
    document.getElementById("loginNavBtn").style.display = isLoggedIn ? "none" : "inline-flex";

    // Show/hide admin nav based on role (DB lưu là ADMIN viết hoa)
    const adminNavItem = document.getElementById("adminNavItem");
    if (adminNavItem) {
      adminNavItem.style.display = isLoggedIn && user && user.role?.toUpperCase() === "ADMIN" ? "block" : "none";
    }

    if (isLoggedIn && user) {
      document.getElementById("userName").textContent = user.fullName || user.username;
      document.getElementById("userAvatar").src = `https://ui-avatars.com/api/?name=${encodeURIComponent(
        user.fullName || user.username
      )}&background=ed1c24&color=fff`;

      // Connect WebSocket for real-time notifications
      if (window.WebSocketService) {
        WebSocketService.connect();
      }
    } else {
      // Disconnect WebSocket if not logged in
      if (window.WebSocketService) {
        WebSocketService.disconnect();
      }
    }
  }

  navigate(page) {
    this.currentPage = page;

    // Lưu trang hiện tại vào localStorage và URL hash
    localStorage.setItem("currentPage", page);
    window.location.hash = page;

    // Update nav active state
    document.querySelectorAll(".nav-link").forEach((el) => {
      el.classList.toggle("active", el.dataset.page === page);
    });

    // Render page
    this.renderPage(page);
  }

  async renderPage(page) {
    const content = document.getElementById("mainContent");
    content.innerHTML = '<div class="loading-container"><div class="spinner"></div></div>';

    try {
      switch (page) {
        case "home":
          content.innerHTML = await this.renderHomePage();
          break;
        case "books":
          content.innerHTML = await this.renderBooksPage();
          break;
        case "borrow":
          content.innerHTML = await this.renderBorrowPage();
          break;
        case "ranking":
          content.innerHTML = await this.renderRankingPage();
          break;
        case "login":
          content.innerHTML = this.renderLoginPage();
          this.bindLoginEvents();
          break;
        case "register":
          content.innerHTML = this.renderRegisterPage();
          this.bindRegisterEvents();
          break;
        case "profile":
          content.innerHTML = await this.renderProfilePage();
          break;
        case "messages":
          content.innerHTML = await this.renderMessagesPage();
          this.bindMessageEvents();
          // Reset message badge when viewing messages
          if (typeof WebSocketService !== "undefined") {
            WebSocketService.resetMessageBadge();
          }
          break;
        case "notifications":
          content.innerHTML = await this.renderNotificationsPage();
          break;
        case "friends":
          content.innerHTML = await this.renderFriendsPage();
          this.bindFriendEvents();
          break;
        case "admin":
          content.innerHTML = await this.renderAdminPage();
          this.bindAdminEvents();
          break;
        default:
          content.innerHTML = this.render404Page();
      }
    } catch (error) {
      content.innerHTML = this.renderErrorPage(error.message);
    }

    // Bind search if exists
    this.bindSearchEvents();

    // Rebind data-page links trong content mới
    this.bindPageLinks();
  }

  bindPageLinks() {
    document.querySelectorAll("[data-page]").forEach((el) => {
      el.addEventListener("click", (e) => {
        e.preventDefault();
        this.navigate(el.dataset.page);
      });
    });
  }

  // ===============================
  // PAGE RENDERERS
  // ===============================

  async renderHomePage() {
    let booksHtml = "";

    try {
      const response = await BookAPI.getAll();
      const books = response.data?.slice(0, 8) || [];

      if (books.length > 0) {
        booksHtml = books.map((book) => this.renderBookCard(book)).join("");
      } else {
        booksHtml = `
                    <div class="empty-state" style="grid-column: 1/-1">
                        <div class="empty-icon">📚</div>
                        <h3 class="empty-title">Chưa có sách</h3>
                        <p class="empty-text">Thư viện đang cập nhật sách mới</p>
                    </div>
                `;
      }
    } catch (e) {
      booksHtml = `<p class="text-muted text-center" style="grid-column: 1/-1">Không thể tải danh sách sách</p>`;
    }

    return `
            <div class="container">
                <section class="hero-section">
                    <div class="hero-content">
                        <h1 class="hero-title">Chào mừng đến với Thư viện PTIT</h1>
                        <p class="hero-subtitle">Khám phá hàng ngàn đầu sách, tài liệu học tập và nghiên cứu dành cho sinh viên và giảng viên</p>
                        <div class="search-box">
                            <input type="text" class="form-input" id="searchInput" placeholder="Tìm kiếm sách theo tên, tác giả...">
                            <button class="btn btn-primary" id="searchBtn">
                                <span class="material-symbols-outlined">search</span>
                                Tìm kiếm
                            </button>
                        </div>
                    </div>
                </section>

                <div class="stats-grid">
                    <div class="stat-card">
                        <div class="stat-icon primary">
                            <span class="material-symbols-outlined">menu_book</span>
                        </div>
                        <div class="stat-content">
                            <div class="stat-value">5,000+</div>
                            <div class="stat-label">Đầu sách</div>
                        </div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-icon success">
                            <span class="material-symbols-outlined">group</span>
                        </div>
                        <div class="stat-content">
                            <div class="stat-value">10,000+</div>
                            <div class="stat-label">Độc giả</div>
                        </div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-icon warning">
                            <span class="material-symbols-outlined">bookmark_added</span>
                        </div>
                        <div class="stat-content">
                            <div class="stat-value">50,000+</div>
                            <div class="stat-label">Lượt mượn</div>
                        </div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-icon info">
                            <span class="material-symbols-outlined">schedule</span>
                        </div>
                        <div class="stat-content">
                            <div class="stat-value">24/7</div>
                            <div class="stat-label">Hỗ trợ Online</div>
                        </div>
                    </div>
                </div>

                <section>
                    <div class="section-header">
                        <h2 class="section-title">📖 Sách mới nhất</h2>
                        <a href="#" class="btn btn-ghost" data-page="books">
                            Xem tất cả
                            <span class="material-symbols-outlined">arrow_forward</span>
                        </a>
                    </div>
                    <div class="grid grid-4">
                        ${booksHtml}
                    </div>
                </section>
            </div>
        `;
  }

  async renderBooksPage() {
    let booksHtml = "";

    try {
      const response = await BookAPI.getAll();
      const books = response.data || [];

      if (books.length > 0) {
        booksHtml = books.map((book) => this.renderBookCard(book)).join("");
      } else {
        booksHtml = `
                    <div class="empty-state" style="grid-column: 1/-1">
                        <div class="empty-icon">📚</div>
                        <h3 class="empty-title">Chưa có sách</h3>
                    </div>
                `;
      }
    } catch (e) {
      booksHtml = `<p class="text-muted text-center" style="grid-column: 1/-1">Không thể tải danh sách sách</p>`;
    }

    return `
            <div class="container">
                <div class="section-header mb-6">
                    <h1 class="section-title">📚 Danh sách sách</h1>
                    <div class="search-box" style="max-width: 40rem">
                        <input type="text" class="form-input" id="searchInput" placeholder="Tìm kiếm...">
                        <button class="btn btn-primary" id="searchBtn">
                            <span class="material-symbols-outlined">search</span>
                        </button>
                    </div>
                </div>
                <div class="grid grid-4" id="booksGrid">
                    ${booksHtml}
                </div>
            </div>
        `;
  }

  async renderBorrowPage() {
    if (!TokenService.isLoggedIn()) {
      return `
                <div class="container">
                    <div class="empty-state">
                        <div class="empty-icon">🔐</div>
                        <h3 class="empty-title">Vui lòng đăng nhập</h3>
                        <p class="empty-text">Bạn cần đăng nhập để xem lịch sử mượn sách</p>
                        <a href="#" class="btn btn-primary" data-page="login">Đăng nhập</a>
                    </div>
                </div>
            `;
    }

    let recordsHtml = "";

    try {
      const response = await BorrowAPI.getRecords();
      const records = response.data || [];

      if (records.length > 0) {
        recordsHtml = `
                    <div class="table-container">
                        <table class="table">
                            <thead>
                                <tr>
                                    <th>Mã phiếu</th>
                                    <th>Tên sách</th>
                                    <th>Tác giả</th>
                                    <th>Ngày đăng ký</th>
                                    <th>Hạn trả</th>
                                    <th>Trạng thái</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${records
                                  .map(
                                    (r) => `
                                    <tr>
                                        <td><strong>#${r.recordId || r.id}</strong></td>
                                        <td>${r.title}</td>
                                        <td>${r.author}</td>
                                        <td>${r.registerDate || "-"}</td>
                                        <td>${r.dueDate || "-"}</td>
                                        <td><span class="book-status ${
                                          r.status === "Đã trả" ? "available" : "unavailable"
                                        }">${r.status}</span></td>
                                    </tr>
                                `
                                  )
                                  .join("")}
                            </tbody>
                        </table>
                    </div>
                `;
      } else {
        recordsHtml = `
                    <div class="empty-state">
                        <div class="empty-icon">📋</div>
                        <h3 class="empty-title">Chưa có phiếu mượn</h3>
                        <p class="empty-text">Bạn chưa mượn cuốn sách nào</p>
                        <a href="#" class="btn btn-primary" data-page="books">Khám phá sách</a>
                    </div>
                `;
      }
    } catch (e) {
      recordsHtml = `<p class="text-muted text-center">Không thể tải danh sách phiếu mượn</p>`;
    }

    return `
            <div class="container">
                <div class="section-header mb-6">
                    <h1 class="section-title">📋 Lịch sử mượn sách</h1>
                </div>
                <div class="card">
                    <div class="card-body">
                        ${recordsHtml}
                    </div>
                </div>
            </div>
        `;
  }

  async renderRankingPage() {
    let rankingHtml = "";

    try {
      const response = await RankingAPI.get(1, 20);
      const items = response.data?.items || [];

      if (items.length > 0) {
        rankingHtml = `
                    <div class="table-container">
                        <table class="table">
                            <thead>
                                <tr>
                                    <th style="width: 80px">Hạng</th>
                                    <th>Sinh viên</th>
                                    <th>Mã SV</th>
                                    <th>Ngành</th>
                                    <th style="text-align: right">Số sách đã mượn</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${items
                                  .map(
                                    (item, index) => `
                                    <tr>
                                        <td>
                                            <span class="rank-badge ${
                                              index === 0
                                                ? "gold"
                                                : index === 1
                                                ? "silver"
                                                : index === 2
                                                ? "bronze"
                                                : ""
                                            }">
                                                ${index + 1}
                                            </span>
                                        </td>
                                        <td><strong>${item.fullName || "-"}</strong></td>
                                        <td>${item.studentCode}</td>
                                        <td>${item.major || "-"}</td>
                                        <td style="text-align: right"><strong>${item.borrowCount}</strong></td>
                                    </tr>
                                `
                                  )
                                  .join("")}
                            </tbody>
                        </table>
                    </div>
                `;
      } else {
        rankingHtml = `
                    <div class="empty-state">
                        <div class="empty-icon">🏆</div>
                        <h3 class="empty-title">Chưa có dữ liệu</h3>
                    </div>
                `;
      }
    } catch (e) {
      rankingHtml = `<p class="text-muted text-center">Không thể tải bảng xếp hạng</p>`;
    }

    return `
            <div class="container">
                <div class="section-header mb-6">
                    <h1 class="section-title">🏆 Bảng xếp hạng</h1>
                </div>
                <div class="card">
                    <div class="card-body">
                        ${rankingHtml}
                    </div>
                </div>
            </div>
        `;
  }

  renderLoginPage() {
    return `
            <div class="auth-page">
                <div class="auth-card card">
                    <div class="card-body">
                        <div class="auth-header">
                            <div class="auth-logo">📚</div>
                            <h1 class="auth-title">Đăng nhập</h1>
                            <p class="auth-subtitle">Chào mừng bạn đến với thư viện PTIT</p>
                        </div>
                        <form class="auth-form" id="loginForm">
                            <div class="form-group">
                                <label class="form-label">Mã sinh viên</label>
                                <input type="text" class="form-input" id="loginUsername" placeholder="VD: B21DCCN001" required autocomplete="username">
                            </div>
                            <div class="form-group">
                                <label class="form-label">Mật khẩu</label>
                                <input type="password" class="form-input" id="loginPassword" placeholder="Nhập mật khẩu" required autocomplete="current-password">
                            </div>
                            <button type="submit" class="btn btn-primary btn-lg">Đăng nhập</button>
                        </form>
                        <div class="auth-footer">
                            Chưa có tài khoản? <a href="#" data-page="register">Đăng ký ngay</a>
                        </div>
                    </div>
                </div>
            </div>
        `;
  }

  renderRegisterPage() {
    return `
            <div class="auth-page">
                <div class="auth-card card">
                    <div class="card-body">
                        <div class="auth-header">
                            <div class="auth-logo">📚</div>
                            <h1 class="auth-title">Đăng ký</h1>
                            <p class="auth-subtitle">Tạo tài khoản thư viện PTIT</p>
                        </div>
                        <form class="auth-form" id="registerForm">
                            <div class="form-group">
                                <label class="form-label">Mã sinh viên</label>
                                <input type="text" class="form-input" id="regStudentCode" placeholder="VD: B21DCCN001" required>
                            </div>
                            <div class="form-group">
                                <label class="form-label">Họ và tên</label>
                                <input type="text" class="form-input" id="regFullName" placeholder="Nguyễn Văn A" required>
                            </div>
                            <div class="form-group">
                                <label class="form-label">Email</label>
                                <input type="email" class="form-input" id="regEmail" placeholder="email@ptit.edu.vn" required>
                            </div>
                            <div class="form-group">
                                <label class="form-label">Số điện thoại</label>
                                <input type="tel" class="form-input" id="regPhone" placeholder="0123456789">
                            </div>
                            <div class="form-group">
                                <label class="form-label">Mật khẩu</label>
                                <input type="password" class="form-input" id="regPassword" placeholder="Ít nhất 6 ký tự" required autocomplete="new-password">
                            </div>
                            <div class="form-group">
                                <label class="form-label">Xác nhận mật khẩu</label>
                                <input type="password" class="form-input" id="regRetypePassword" placeholder="Nhập lại mật khẩu" required autocomplete="new-password">
                            </div>
                            <button type="submit" class="btn btn-primary btn-lg">Đăng ký</button>
                        </form>
                        <div class="auth-footer">
                            Đã có tài khoản? <a href="#" data-page="login">Đăng nhập</a>
                        </div>
                    </div>
                </div>
            </div>
        `;
  }

  async renderProfilePage() {
    if (!TokenService.isLoggedIn()) {
      this.navigate("login");
      return "";
    }

    try {
      const response = await UserAPI.getMe();
      const user = response.data;

      // Lấy thống kê mượn sách
      let borrowingCount = 0;
      let returnedCount = 0;
      try {
        const recordsRes = await BorrowAPI.getRecords();
        const records = recordsRes.data || [];
        borrowingCount = records.filter((r) => r.status === "Đang mượn" || r.status === "Đang chờ").length;
        returnedCount = records.filter((r) => r.status === "Đã trả").length;
      } catch (e) {
        console.error("Failed to load borrow records:", e);
      }

      return `
                <div class="container">
                    <div class="section-header mb-6">
                        <h1 class="section-title">👤 Tài khoản</h1>
                    </div>
                    <div class="grid grid-2">
                        <div class="card">
                            <div class="card-header">
                                <h3 class="card-title">Thông tin cá nhân</h3>
                            </div>
                            <div class="card-body">
                                <div class="flex items-center gap-4 mb-6">
                                    <img src="https://ui-avatars.com/api/?name=${encodeURIComponent(
                                      user.fullName || user.username
                                    )}&background=ed1c24&color=fff&size=80" 
                                         alt="Avatar" style="width: 8rem; height: 8rem; border-radius: 50%;">
                                    <div>
                                        <h3 style="font-size: 2rem; font-weight: 700; color: var(--gray-800)">${
                                          user.fullName || "-"
                                        }</h3>
                                        <p class="text-muted">@${user.username}</p>
                                    </div>
                                </div>
                                <div class="flex flex-col gap-4">
                                    <div><strong>Email:</strong> ${user.email || "-"}</div>
                                    <div><strong>SĐT:</strong> ${user.phone || "-"}</div>
                                    <div><strong>Ngành:</strong> ${user.major || "-"}</div>
                                    <div><strong>Khóa:</strong> ${user.enrollmentYear || "-"}</div>
                                </div>
                            </div>
                        </div>
                        <div class="card">
                            <div class="card-header">
                                <h3 class="card-title">Hoạt động</h3>
                            </div>
                            <div class="card-body">
                                <div class="stats-grid" style="grid-template-columns: repeat(2, 1fr); gap: 1.6rem;">
                                    <div class="stat-card">
                                        <div class="stat-icon primary">
                                            <span class="material-symbols-outlined">bookmark</span>
                                        </div>
                                        <div class="stat-content">
                                            <div class="stat-value">${borrowingCount}</div>
                                            <div class="stat-label">Sách đang mượn</div>
                                        </div>
                                    </div>
                                    <div class="stat-card">
                                        <div class="stat-icon success">
                                            <span class="material-symbols-outlined">done_all</span>
                                        </div>
                                        <div class="stat-content">
                                            <div class="stat-value">${returnedCount}</div>
                                            <div class="stat-label">Đã hoàn trả</div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            `;
    } catch (e) {
      return this.renderErrorPage("Không thể tải thông tin người dùng");
    }
  }

  async renderMessagesPage() {
    if (!TokenService.isLoggedIn()) {
      this.navigate("login");
      return "";
    }

    try {
      const friendsRes = await FriendAPI.getAll();
      const friends = friendsRes.data || [];

      const friendsList =
        friends.length > 0
          ? friends
              .map(
                (f) => `
          <div class="friend-chat-item" data-user-id="${f.username}">
            <img src="https://ui-avatars.com/api/?name=${encodeURIComponent(
              f.fullName || f.username
            )}&background=ed1c24&color=fff" 
                 alt="Avatar" class="friend-avatar">
            <div class="friend-info">
              <div class="friend-name">${f.fullName || f.username}</div>
              <div class="friend-status text-muted">@${f.username}</div>
            </div>
          </div>
        `
              )
              .join("")
          : '<p class="text-muted text-center p-4">Chưa có bạn bè</p>';

      return `
        <div class="container">
          <div class="section-header mb-6">
            <h1 class="section-title">💬 Tin nhắn</h1>
          </div>
          <div class="message-layout">
            <div class="message-sidebar card">
              <div class="card-header">
                <h3 class="card-title">Bạn bè</h3>
              </div>
              <div class="friend-chat-list" id="friendChatList">
                ${friendsList}
              </div>
            </div>
            <div class="message-main card">
              <div class="card-header" id="chatHeader">
                <h3 class="card-title">Chọn người để chat</h3>
              </div>
              <div class="message-body" id="messageBody">
                <div class="empty-state">
                  <div class="empty-icon">💬</div>
                  <p class="empty-text">Chọn một người bạn để bắt đầu trò chuyện</p>
                </div>
              </div>
              <div class="message-input-area" id="messageInputArea" style="display: none;">
                <input type="text" class="form-input" id="messageInput" placeholder="Nhập tin nhắn...">
                <button class="btn btn-primary" id="sendMessageBtn">
                  <span class="material-symbols-outlined">send</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      `;
    } catch (e) {
      console.error("Error loading messages:", e);
      return this.renderErrorPage("Không thể tải tin nhắn: " + e.message);
    }
  }

  async renderNotificationsPage() {
    if (!TokenService.isLoggedIn()) {
      this.navigate("login");
      return "";
    }

    try {
      const response = await NotificationAPI.getAll();
      const notifications = response.data || [];

      const notificationsHtml =
        notifications.length > 0
          ? notifications
              .map(
                (n) => `
          <div class="notification-item card ${n.read ? "" : "unread"}" data-id="${n.id}">
            <div class="card-body flex items-center gap-4">
              <div class="notification-icon ${n.read ? "" : "text-primary"}">
                <span class="material-symbols-outlined">${this.getNotificationIcon(n.type)}</span>
              </div>
              <div class="flex-1">
                <p class="notification-message">${n.message}</p>
                <small class="text-muted">${this.formatDate(n.createdAt)}</small>
              </div>
              ${
                !n.read
                  ? `<button class="btn btn-sm btn-secondary mark-read-btn" data-id="${n.id}">
                <span class="material-symbols-outlined">done</span>
              </button>`
                  : ""
              }
            </div>
          </div>
        `
              )
              .join("")
          : '<div class="empty-state"><p>Chưa có thông báo nào</p></div>';

      setTimeout(() => this.bindNotificationEvents(), 100);

      return `
        <div class="container">
          <div class="section-header mb-6 flex justify-between items-center">
            <h1 class="section-title">🔔 Thông báo</h1>
            <button class="btn btn-secondary" id="markAllReadBtn">
              <span class="material-symbols-outlined">done_all</span>
              Đánh dấu tất cả đã đọc
            </button>
          </div>
          <div class="notifications-list">
            ${notificationsHtml}
          </div>
        </div>
      `;
    } catch (e) {
      console.error("Error loading notifications:", e);
      return this.renderErrorPage("Không thể tải thông báo: " + e.message);
    }
  }

  getNotificationIcon(type) {
    const icons = {
      BORROW_APPROVED: "check_circle",
      BORROW_REJECTED: "cancel",
      BORROW_RETURNED: "assignment_return",
      MESSAGE: "chat",
      FRIEND_REQUEST: "person_add",
      SYSTEM: "info",
    };
    return icons[type] || "notifications";
  }

  bindNotificationEvents() {
    // Mark single as read
    document.querySelectorAll(".mark-read-btn").forEach((btn) => {
      btn.addEventListener("click", async (e) => {
        e.stopPropagation();
        const id = btn.dataset.id;
        try {
          await NotificationAPI.markAsRead(id);
          const item = document.querySelector(`.notification-item[data-id="${id}"]`);
          if (item) {
            item.classList.remove("unread");
            btn.remove();
          }
          this.updateNotificationBadge();
        } catch (err) {
          console.error("Error marking notification as read:", err);
        }
      });
    });

    // Mark all as read
    const markAllBtn = document.getElementById("markAllReadBtn");
    if (markAllBtn) {
      markAllBtn.addEventListener("click", async () => {
        try {
          await NotificationAPI.markAllAsRead();
          document.querySelectorAll(".notification-item.unread").forEach((item) => {
            item.classList.remove("unread");
            const btn = item.querySelector(".mark-read-btn");
            if (btn) btn.remove();
          });
          this.updateNotificationBadge();
        } catch (err) {
          console.error("Error marking all notifications as read:", err);
        }
      });
    }
  }

  async updateNotificationBadge() {
    try {
      const response = await NotificationAPI.getUnread();
      const count = (response.data || []).length;
      const badge = document.querySelector(".notification-badge");
      if (badge) {
        badge.textContent = count;
        badge.style.display = count > 0 ? "flex" : "none";
      }
    } catch (e) {
      console.error("Error updating notification badge:", e);
    }
  }

  async renderFriendsPage() {
    if (!TokenService.isLoggedIn()) {
      this.navigate("login");
      return "";
    }

    try {
      const [friendsRes, pendingRes] = await Promise.all([FriendAPI.getAll(), FriendAPI.getPendingReceived()]);
      const friends = friendsRes.data || [];
      const pending = pendingRes.data || [];

      const friendsHtml =
        friends.length > 0
          ? friends
              .map(
                (f) => `
          <div class="friend-card card">
            <div class="card-body flex items-center gap-4">
              <img src="https://ui-avatars.com/api/?name=${encodeURIComponent(
                f.fullName || f.username
              )}&background=ed1c24&color=fff" 
                   alt="Avatar" style="width: 5rem; height: 5rem; border-radius: 50%;">
              <div class="flex-1">
                <h4>${f.fullName || f.username}</h4>
                <p class="text-muted">@${f.username}</p>
              </div>
              <div class="flex gap-2">
                <button class="btn btn-secondary btn-sm" onclick="app.openChat('${f.username}')">
                  <span class="material-symbols-outlined">chat</span>
                </button>
                <button class="btn btn-danger btn-sm" onclick="app.removeFriend('${f.username}')">
                  <span class="material-symbols-outlined">person_remove</span>
                </button>
              </div>
            </div>
          </div>
        `
              )
              .join("")
          : '<p class="text-muted text-center">Chưa có bạn bè</p>';

      const pendingHtml =
        pending.length > 0
          ? pending
              .map(
                (p) => `
          <div class="friend-card card">
            <div class="card-body flex items-center gap-4">
              <img src="https://ui-avatars.com/api/?name=${encodeURIComponent(
                p.fullName || p.username
              )}&background=ffc107&color=000" 
                   alt="Avatar" style="width: 5rem; height: 5rem; border-radius: 50%;">
              <div class="flex-1">
                <h4>${p.fullName || p.username}</h4>
                <p class="text-muted">@${p.username}</p>
              </div>
              <div class="flex gap-2">
                <button class="btn btn-primary btn-sm" onclick="app.acceptFriend('${p.username}')">Chấp nhận</button>
                <button class="btn btn-secondary btn-sm" onclick="app.rejectFriend('${p.username}')">Từ chối</button>
              </div>
            </div>
          </div>
        `
              )
              .join("")
          : '<p class="text-muted text-center">Không có lời mời</p>';

      return `
        <div class="container">
          <div class="section-header mb-6">
            <h1 class="section-title">👥 Bạn bè</h1>
          </div>
          
          <div class="card mb-6">
            <div class="card-header">
              <h3 class="card-title">Thêm bạn bè</h3>
            </div>
            <div class="card-body">
              <div class="flex gap-2">
                <input type="text" class="form-input flex-1" id="addFriendInput" placeholder="Nhập mã sinh viên...">
                <button class="btn btn-primary" id="addFriendBtn">
                  <span class="material-symbols-outlined">person_add</span>
                  Kết bạn
                </button>
              </div>
            </div>
          </div>

          ${
            pending.length > 0
              ? `
          <div class="mb-6">
            <h2 class="section-title mb-4">📩 Lời mời kết bạn (${pending.length})</h2>
            <div class="flex flex-col gap-3">
              ${pendingHtml}
            </div>
          </div>
          `
              : ""
          }

          <div>
            <h2 class="section-title mb-4">👥 Danh sách bạn bè (${friends.length})</h2>
            <div class="flex flex-col gap-3">
              ${friendsHtml}
            </div>
          </div>
        </div>
      `;
    } catch (e) {
      console.error("Error loading friends:", e);
      return this.renderErrorPage("Không thể tải danh sách bạn bè: " + e.message);
    }
  }

  render404Page() {
    return `
            <div class="container">
                <div class="empty-state">
                    <div class="empty-icon">🔍</div>
                    <h3 class="empty-title">Không tìm thấy trang</h3>
                    <p class="empty-text">Trang bạn đang tìm kiếm không tồn tại</p>
                    <a href="#" class="btn btn-primary" data-page="home">Về trang chủ</a>
                </div>
            </div>
        `;
  }

  // ===============================
  // ADMIN PAGE
  // ===============================

  async renderAdminPage() {
    const user = TokenService.getUser();
    if (!TokenService.isLoggedIn() || !user || user.role?.toUpperCase() !== "ADMIN") {
      return `
        <div class="container">
          <div class="empty-state">
            <div class="empty-icon">🔐</div>
            <h3 class="empty-title">Không có quyền truy cập</h3>
            <p class="empty-text">Trang này chỉ dành cho quản trị viên</p>
            <a href="#" class="btn btn-primary" data-page="home">Về trang chủ</a>
          </div>
        </div>
      `;
    }

    let recordsHtml = "";

    try {
      const response = await BorrowAPI.getAllRecords();
      const records = response.data || [];

      if (records.length > 0) {
        recordsHtml = `
          <div class="table-container">
            <table class="table">
              <thead>
                <tr>
                  <th>Mã phiếu</th>
                  <th>Sinh viên</th>
                  <th>Tên sách</th>
                  <th>Tác giả</th>
                  <th>Ngày đăng ký</th>
                  <th>Trạng thái</th>
                  <th>Thao tác</th>
                </tr>
              </thead>
              <tbody>
                ${records
                  .map(
                    (r) => `
                  <tr data-record-id="${r.recordId || r.id}">
                    <td><strong>#${r.recordId || r.id}</strong></td>
                    <td>${r.username || "-"}</td>
                    <td>${r.title}</td>
                    <td>${r.author}</td>
                    <td>${r.registerDate || "-"}</td>
                    <td>
                      <span class="book-status ${this.getStatusClass(r.status)}">${r.status}</span>
                    </td>
                    <td>
                      ${this.renderAdminActions(r)}
                    </td>
                  </tr>
                `
                  )
                  .join("")}
              </tbody>
            </table>
          </div>
        `;
      } else {
        recordsHtml = `
          <div class="empty-state">
            <div class="empty-icon">📋</div>
            <h3 class="empty-title">Chưa có phiếu mượn</h3>
            <p class="empty-text">Chưa có sinh viên nào đăng ký mượn sách</p>
          </div>
        `;
      }
    } catch (e) {
      console.error("Error loading admin records:", e);
      recordsHtml = `<p class="text-muted text-center">Không thể tải danh sách phiếu mượn: ${e.message}</p>`;
    }

    return `
      <div class="container">
        <div class="section-header mb-6">
          <h1 class="section-title">⚙️ Quản lý mượn sách</h1>
          <div class="flex gap-2">
            <button class="btn btn-secondary" id="filterAllBtn">Tất cả</button>
            <button class="btn btn-warning" id="filterPendingBtn">Đang chờ</button>
            <button class="btn btn-primary" id="filterBorrowingBtn">Đang mượn</button>
            <button class="btn btn-success" id="filterReturnedBtn">Đã trả</button>
          </div>
        </div>
        <div class="card">
          <div class="card-body" id="adminRecordsContainer">
            ${recordsHtml}
          </div>
        </div>
      </div>
    `;
  }

  getStatusClass(status) {
    switch (status) {
      case "Đang chờ":
        return "pending";
      case "Đang mượn":
        return "unavailable";
      case "Đã trả":
        return "available";
      default:
        return "";
    }
  }

  renderAdminActions(record) {
    const status = record.status;
    const recordId = record.recordId || record.id;

    if (status === "Đang chờ") {
      return `
        <button class="btn btn-primary btn-sm" onclick="app.approveRecord(${recordId})">
          <span class="material-symbols-outlined">check</span> Duyệt
        </button>
      `;
    } else if (status === "Đang mượn") {
      return `
        <button class="btn btn-success btn-sm" onclick="app.confirmReturn(${recordId})">
          <span class="material-symbols-outlined">assignment_return</span> Xác nhận trả
        </button>
      `;
    } else {
      return `<span class="text-muted">Hoàn thành</span>`;
    }
  }

  bindAdminEvents() {
    document.getElementById("filterAllBtn")?.addEventListener("click", () => this.filterAdminRecords(""));
    document.getElementById("filterPendingBtn")?.addEventListener("click", () => this.filterAdminRecords("Đang chờ"));
    document
      .getElementById("filterBorrowingBtn")
      ?.addEventListener("click", () => this.filterAdminRecords("Đang mượn"));
    document.getElementById("filterReturnedBtn")?.addEventListener("click", () => this.filterAdminRecords("Đã trả"));
  }

  async filterAdminRecords(status) {
    const container = document.getElementById("adminRecordsContainer");
    if (!container) return;

    container.innerHTML = '<div class="loading-container"><div class="spinner"></div></div>';

    try {
      const response = await BorrowAPI.getAllRecords(status);
      const records = response.data || [];

      if (records.length > 0) {
        container.innerHTML = `
          <div class="table-container">
            <table class="table">
              <thead>
                <tr>
                  <th>Mã phiếu</th>
                  <th>Sinh viên</th>
                  <th>Tên sách</th>
                  <th>Tác giả</th>
                  <th>Ngày đăng ký</th>
                  <th>Trạng thái</th>
                  <th>Thao tác</th>
                </tr>
              </thead>
              <tbody>
                ${records
                  .map(
                    (r) => `
                  <tr data-record-id="${r.recordId || r.id}">
                    <td><strong>#${r.recordId || r.id}</strong></td>
                    <td>${r.username || "-"}</td>
                    <td>${r.title}</td>
                    <td>${r.author}</td>
                    <td>${r.registerDate || "-"}</td>
                    <td>
                      <span class="book-status ${this.getStatusClass(r.status)}">${r.status}</span>
                    </td>
                    <td>
                      ${this.renderAdminActions(r)}
                    </td>
                  </tr>
                `
                  )
                  .join("")}
              </tbody>
            </table>
          </div>
        `;
      } else {
        container.innerHTML = `
          <div class="empty-state">
            <div class="empty-icon">📋</div>
            <h3 class="empty-title">Không có phiếu mượn</h3>
            <p class="empty-text">Không có phiếu mượn nào ${status ? `với trạng thái "${status}"` : ""}</p>
          </div>
        `;
      }
    } catch (e) {
      container.innerHTML = `<p class="text-muted text-center">Không thể tải danh sách: ${e.message}</p>`;
    }
  }

  async approveRecord(recordId) {
    if (!confirm("Xác nhận duyệt phiếu mượn này?")) return;

    try {
      const response = await BorrowAPI.issueRecord(recordId);
      this.showToast(response.message || "Duyệt phiếu mượn thành công!", "success");
      this.navigate("admin");
    } catch (e) {
      this.showToast(e.message || "Không thể duyệt phiếu mượn", "error");
    }
  }

  async confirmReturn(recordId) {
    if (!confirm("Xác nhận sinh viên đã trả sách?")) return;

    try {
      const response = await BorrowAPI.returnRecord(recordId);
      this.showToast(response.message || "Xác nhận trả sách thành công!", "success");
      this.navigate("admin");
    } catch (e) {
      this.showToast(e.message || "Không thể xác nhận trả sách", "error");
    }
  }

  renderErrorPage(message) {
    return `
            <div class="container">
                <div class="empty-state">
                    <div class="empty-icon">⚠️</div>
                    <h3 class="empty-title">Có lỗi xảy ra</h3>
                    <p class="empty-text">${message}</p>
                    <a href="#" class="btn btn-primary" data-page="home">Về trang chủ</a>
                </div>
            </div>
        `;
  }

  // ===============================
  // COMPONENT RENDERERS
  // ===============================

  renderBookCard(book) {
    const isAvailable = book.copiesAvailable > 0;
    const defaultImage = "/image/Logo_PTIT.jpg";
    const coverImage = book.coverImage || defaultImage;
    const coverContent = `<img src="${coverImage}" alt="${book.title}" class="book-cover-img" onerror="this.src='${defaultImage}';">`;
    return `
            <div class="book-card" data-book-id="${book.id}">
                <div class="book-cover">
                    ${coverContent}
                </div>
                <h4 class="book-title">${book.title}</h4>
                <p class="book-author">${book.author}</p>
                <div class="book-meta">
                    <span class="book-status ${isAvailable ? "available" : "unavailable"}">
                        ${isAvailable ? `Còn ${book.copiesAvailable}` : "Hết sách"}
                    </span>
                    <button class="btn btn-sm ${isAvailable ? "btn-primary" : "btn-secondary"}" 
                            ${!isAvailable ? "disabled" : ""} 
                            onclick="app.borrowBook(${book.id})">
                        Mượn
                    </button>
                </div>
            </div>
        `;
  }

  // ===============================
  // EVENT HANDLERS
  // ===============================

  bindLoginEvents() {
    document.getElementById("loginForm")?.addEventListener("submit", async (e) => {
      e.preventDefault();
      const username = document.getElementById("loginUsername").value;
      const password = document.getElementById("loginPassword").value;

      try {
        const response = await AuthAPI.login(username, password);
        TokenService.setToken(response.data.accessToken);
        TokenService.setUser(response.data.user);
        this.checkAuth();
        this.showToast("Đăng nhập thành công!", "success");

        // Nếu là admin thì redirect đến trang admin
        if (response.data.user?.role?.toUpperCase() === "ADMIN") {
          window.location.href = "admin.html";
        } else {
          this.navigate("home");
        }
      } catch (error) {
        this.showToast(error.message || "Đăng nhập thất bại", "error");
      }
    });

    // Re-bind data-page links
    document.querySelectorAll("[data-page]").forEach((el) => {
      el.addEventListener("click", (e) => {
        e.preventDefault();
        this.navigate(el.dataset.page);
      });
    });
  }

  bindRegisterEvents() {
    document.getElementById("registerForm")?.addEventListener("submit", async (e) => {
      e.preventDefault();
      const password = document.getElementById("regPassword")?.value || "";
      const retypePassword = document.getElementById("regRetypePassword")?.value || "";

      if (password !== retypePassword) {
        this.showToast("Mật khẩu xác nhận không khớp!", "error");
        return;
      }

      const data = {
        username: document.getElementById("regStudentCode")?.value?.trim() || "",
        fullName: document.getElementById("regFullName")?.value?.trim() || "",
        email: document.getElementById("regEmail")?.value?.trim() || "",
        phone: document.getElementById("regPhone")?.value?.trim() || "",
        password: password,
        retypePassword: retypePassword,
      };

      // Debug log
      console.log("Register data:", data);

      // Kiểm tra trước khi gửi
      if (!data.username || data.username.length < 3) {
        this.showToast("Mã sinh viên phải có ít nhất 3 ký tự", "error");
        return;
      }
      if (!data.email || !data.email.includes("@")) {
        this.showToast("Email không hợp lệ", "error");
        return;
      }
      if (!data.password || data.password.length < 6) {
        this.showToast("Mật khẩu phải có ít nhất 6 ký tự", "error");
        return;
      }

      try {
        await AuthAPI.register(data);
        this.showToast("Đăng ký thành công! Vui lòng đăng nhập.", "success");
        this.navigate("login");
      } catch (error) {
        this.showToast(error.message || "Đăng ký thất bại", "error");
      }
    });

    // Re-bind data-page links
    document.querySelectorAll("[data-page]").forEach((el) => {
      el.addEventListener("click", (e) => {
        e.preventDefault();
        this.navigate(el.dataset.page);
      });
    });
  }

  bindSearchEvents() {
    const searchBtn = document.getElementById("searchBtn");
    const searchInput = document.getElementById("searchInput");

    if (searchBtn && searchInput) {
      const doSearch = async () => {
        const keyword = searchInput.value.trim();
        if (!keyword) return;

        try {
          const response = await BookAPI.search(keyword);
          const books = response.data || [];
          const grid = document.getElementById("booksGrid") || document.querySelector(".grid-4");

          if (grid) {
            if (books.length > 0) {
              grid.innerHTML = books.map((book) => this.renderBookCard(book)).join("");
            } else {
              grid.innerHTML = `
                                <div class="empty-state" style="grid-column: 1/-1">
                                    <div class="empty-icon">🔍</div>
                                    <h3 class="empty-title">Không tìm thấy kết quả</h3>
                                    <p class="empty-text">Thử tìm kiếm với từ khóa khác</p>
                                </div>
                            `;
            }
          }
        } catch (e) {
          this.showToast("Không thể tìm kiếm", "error");
        }
      };

      searchBtn.addEventListener("click", doSearch);
      searchInput.addEventListener("keypress", (e) => {
        if (e.key === "Enter") doSearch();
      });
    }
  }

  async borrowBook(bookId) {
    if (!TokenService.isLoggedIn()) {
      this.showToast("Vui lòng đăng nhập để mượn sách", "warning");
      this.navigate("login");
      return;
    }

    try {
      await BorrowAPI.borrowBooks([bookId]);
      this.showToast("Đăng ký mượn sách thành công!", "success");
    } catch (error) {
      this.showToast(error.message || "Không thể mượn sách", "error");
    }
  }

  logout() {
    // Disconnect WebSocket before logout
    if (window.WebSocketService) {
      WebSocketService.disconnect();
    }
    AuthAPI.logout();
    this.checkAuth();
    this.showToast("Đã đăng xuất", "success");
    this.navigate("home");
  }

  // ===============================
  // MESSAGE HANDLERS
  // ===============================

  bindMessageEvents() {
    this.currentChatUser = null;

    // Click on friend to open chat
    document.querySelectorAll(".friend-chat-item").forEach((el) => {
      el.addEventListener("click", () => {
        this.openChat(el.dataset.userId);
      });
    });

    // Send message
    document.getElementById("sendMessageBtn")?.addEventListener("click", () => this.sendMessage());
    document.getElementById("messageInput")?.addEventListener("keypress", (e) => {
      if (e.key === "Enter") this.sendMessage();
    });
  }

  async openChat(userId) {
    this.currentChatUser = userId;

    // Update header
    document.getElementById("chatHeader").innerHTML = `
      <h3 class="card-title">Chat với @${userId}</h3>
    `;

    // Show input area
    document.getElementById("messageInputArea").style.display = "flex";

    // Highlight selected friend
    document.querySelectorAll(".friend-chat-item").forEach((el) => {
      el.classList.toggle("active", el.dataset.userId === userId);
    });

    // Load messages
    await this.loadConversation(userId);
  }

  async loadConversation(userId) {
    const body = document.getElementById("messageBody");
    body.innerHTML = '<div class="loading-container"><div class="spinner"></div></div>';

    try {
      const response = await MessageAPI.getConversation(userId);
      const messages = response.data || [];
      const currentUser = TokenService.getUser()?.username;

      if (messages.length === 0) {
        body.innerHTML = `
          <div class="empty-state">
            <div class="empty-icon">💬</div>
            <p class="empty-text">Chưa có tin nhắn nào</p>
          </div>
        `;
      } else {
        body.innerHTML = `
          <div class="messages-list">
            ${messages
              .map(
                (msg) => `
              <div class="message-item ${msg.senderId === currentUser ? "sent" : "received"}">
                <div class="message-content">${msg.content}</div>
                <div class="message-time">${new Date(msg.createdAt).toLocaleTimeString("vi-VN")}</div>
              </div>
            `
              )
              .join("")}
          </div>
        `;
        // Scroll to bottom
        body.scrollTop = body.scrollHeight;
      }
    } catch (e) {
      body.innerHTML = `<p class="text-muted text-center p-4">Không thể tải tin nhắn</p>`;
    }
  }

  async sendMessage() {
    if (!this.currentChatUser) return;

    const input = document.getElementById("messageInput");
    const content = input.value.trim();
    if (!content) return;

    // Disable button và input để tránh gửi trùng
    const sendBtn = document.getElementById("sendMessageBtn");
    if (sendBtn) sendBtn.disabled = true;
    input.disabled = true;

    try {
      const response = await MessageAPI.send(this.currentChatUser, content);
      input.value = "";

      // Reload conversation để hiển thị tin nhắn mới gửi
      if (this.currentChatUser) {
        await this.loadConversation(this.currentChatUser);
      }
    } catch (e) {
      this.showToast(e.message || "Không thể gửi tin nhắn", "error");
    } finally {
      // Enable lại button và input
      if (sendBtn) sendBtn.disabled = false;
      input.disabled = false;
      input.focus();
    }
  }

  // ===============================
  // FRIEND HANDLERS
  // ===============================

  bindFriendEvents() {
    document.getElementById("addFriendBtn")?.addEventListener("click", () => this.addFriend());
    document.getElementById("addFriendInput")?.addEventListener("keypress", (e) => {
      if (e.key === "Enter") this.addFriend();
    });
  }

  async addFriend() {
    const input = document.getElementById("addFriendInput");
    const username = input.value.trim();
    if (!username) return;

    try {
      await FriendAPI.sendRequest(username);
      this.showToast("Đã gửi lời mời kết bạn!", "success");
      input.value = "";
    } catch (e) {
      this.showToast(e.message || "Không thể gửi lời mời", "error");
    }
  }

  async acceptFriend(username) {
    try {
      await FriendAPI.accept(username);
      this.showToast("Đã chấp nhận lời mời kết bạn!", "success");
      this.navigate("friends");
    } catch (e) {
      this.showToast(e.message || "Không thể chấp nhận", "error");
    }
  }

  async rejectFriend(username) {
    try {
      await FriendAPI.decline(username);
      this.showToast("Đã từ chối lời mời", "success");
      this.navigate("friends");
    } catch (e) {
      this.showToast(e.message || "Không thể từ chối", "error");
    }
  }

  async removeFriend(username) {
    if (!confirm("Bạn có chắc muốn hủy kết bạn?")) return;

    try {
      await FriendAPI.remove(username);
      this.showToast("Đã hủy kết bạn", "success");
      this.navigate("friends");
    } catch (e) {
      this.showToast(e.message || "Không thể hủy kết bạn", "error");
    }
  }

  // ===============================
  // UTILITIES
  // ===============================

  showToast(message, type = "info") {
    const container = document.getElementById("toastContainer");
    const toast = document.createElement("div");
    toast.className = `toast toast-${type}`;

    const icons = {
      success: "check_circle",
      error: "error",
      warning: "warning",
      info: "info",
    };

    toast.innerHTML = `
            <span class="toast-icon material-symbols-outlined">${icons[type]}</span>
            <span class="toast-message">${message}</span>
            <button class="toast-close" onclick="this.parentElement.remove()">
                <span class="material-symbols-outlined">close</span>
            </button>
        `;

    container.appendChild(toast);

    setTimeout(() => {
      toast.classList.add("toast-exit");
      setTimeout(() => toast.remove(), 300);
    }, 4000);
  }

  openModal(title, content) {
    document.getElementById("modalTitle").textContent = title;
    document.getElementById("modalBody").innerHTML = content;
    document.getElementById("modalOverlay").classList.add("active");
  }

  closeModal() {
    document.getElementById("modalOverlay").classList.remove("active");
  }

  // ============================================
  // Notification Dropdown Methods
  // ============================================
  async loadNotifications() {
    const list = document.getElementById("notificationList");
    if (!list) return;

    // Show loading
    list.innerHTML = '<div class="notification-empty"><div class="spinner"></div><p>Đang tải...</p></div>';

    try {
      const token = TokenService.getToken();
      const response = await fetch("/api/notifications", {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      const result = await response.json();

      if (result.success && result.data && result.data.length > 0) {
        // Sort by createdAt desc (newest first)
        const notifications = result.data.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));

        list.innerHTML = notifications.map((n) => this.renderNotificationItem(n)).join("");

        // Update badge
        const unreadCount = notifications.filter((n) => !n.isRead).length;
        this.updateNotificationBadge(unreadCount);
      } else {
        list.innerHTML = `
          <div class="notification-empty">
            <span class="material-symbols-outlined">notifications_off</span>
            <p>Chưa có thông báo</p>
          </div>
        `;
        this.updateNotificationBadge(0);
      }
    } catch (error) {
      console.error("Load notifications error:", error);
      list.innerHTML = `
        <div class="notification-empty">
          <span class="material-symbols-outlined">error</span>
          <p>Không thể tải thông báo</p>
        </div>
      `;
    }
  }

  renderNotificationItem(notification) {
    const iconMap = {
      borrow_approved: { icon: "check_circle", class: "success" },
      borrow_rejected: { icon: "cancel", class: "error" },
      return_approved: { icon: "assignment_turned_in", class: "success" },
      reminder: { icon: "schedule", class: "warning" },
      system: { icon: "info", class: "info" },
      default: { icon: "notifications", class: "info" },
    };

    const typeInfo = iconMap[notification.notificationType] || iconMap.default;
    const isUnread = !notification.isRead;
    const timeAgo = this.formatTimeAgo(notification.createdAt);

    return `
      <div class="notification-item ${isUnread ? "unread" : ""}" 
           data-id="${notification.id}"
           onclick="app.markNotificationRead(${notification.id})">
        <div class="notification-icon ${typeInfo.class}">
          <span class="material-symbols-outlined">${typeInfo.icon}</span>
        </div>
        <div class="notification-content">
          <div class="notification-title">${notification.title || "Thông báo"}</div>
          <div class="notification-message">${notification.content}</div>
          <div class="notification-time">${timeAgo}</div>
        </div>
      </div>
    `;
  }

  formatTimeAgo(dateString) {
    const date = new Date(dateString);
    const now = new Date();
    const diffMs = now - date;
    const diffSec = Math.floor(diffMs / 1000);
    const diffMin = Math.floor(diffSec / 60);
    const diffHour = Math.floor(diffMin / 60);
    const diffDay = Math.floor(diffHour / 24);

    if (diffSec < 60) return "Vừa xong";
    if (diffMin < 60) return `${diffMin} phút trước`;
    if (diffHour < 24) return `${diffHour} giờ trước`;
    if (diffDay < 7) return `${diffDay} ngày trước`;

    return date.toLocaleDateString("vi-VN");
  }

  async markNotificationRead(id) {
    try {
      const token = TokenService.getToken();
      await fetch(`/api/notifications/${id}/read`, {
        method: "PUT",
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      // Update UI
      const item = document.querySelector(`.notification-item[data-id="${id}"]`);
      if (item) {
        item.classList.remove("unread");
      }

      // Update badge count
      const unreadItems = document.querySelectorAll(".notification-item.unread");
      this.updateNotificationBadge(unreadItems.length);
    } catch (error) {
      console.error("Mark notification read error:", error);
    }
  }

  async markAllNotificationsRead() {
    try {
      const token = TokenService.getToken();
      await fetch("/api/notifications/read-all", {
        method: "PUT",
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      // Update UI - remove unread class from all
      document.querySelectorAll(".notification-item.unread").forEach((item) => {
        item.classList.remove("unread");
      });

      // Update badge
      this.updateNotificationBadge(0);

      this.showToast("Đã đánh dấu tất cả đã đọc", "success");
    } catch (error) {
      console.error("Mark all notifications read error:", error);
      this.showToast("Có lỗi xảy ra", "error");
    }
  }

  updateNotificationBadge(count) {
    const badge = document.getElementById("notiBadge");
    if (badge) {
      if (count > 0) {
        badge.textContent = count > 99 ? "99+" : count;
        badge.style.display = "flex";
      } else {
        badge.style.display = "none";
      }
    }
  }
}

// Initialize app
const app = new App();
