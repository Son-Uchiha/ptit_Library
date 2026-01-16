/**
 * PTIT Library - WebSocket Service
 * Handles real-time notifications via WebSocket
 */

const WebSocketService = {
  stompClient: null,
  connected: false,
  subscriptions: [],
  reconnectAttempts: 0,
  maxReconnectAttempts: 5,

  /**
   * Connect to WebSocket server
   */
  connect() {
    if (this.connected || !TokenService.isLoggedIn()) {
      return;
    }

    const socket = new SockJS("http://localhost:8080/ws");
    this.stompClient = Stomp.over(socket);

    // Disable debug logging
    this.stompClient.debug = null;

    const user = TokenService.getUser();
    const username = user?.username;

    if (!username) {
      console.warn("No username found, cannot connect WebSocket");
      return;
    }

    this.stompClient.connect(
      {},
      (frame) => {
        console.log("WebSocket connected");
        this.connected = true;
        this.reconnectAttempts = 0;

        // Subscribe to personal message queue
        this.subscribeToMessages(username);

        // Subscribe to friend events
        this.subscribeToFriends(username);

        // Subscribe to notifications
        this.subscribeToNotifications(username);
      },
      (error) => {
        console.error("WebSocket connection error:", error);
        this.connected = false;
        this.attemptReconnect();
      }
    );
  },

  /**
   * Subscribe to personal messages
   */
  subscribeToMessages(username) {
    // Subscribe to topic (không cần authentication)
    const sub = this.stompClient.subscribe(`/topic/messages/${username}`, (message) => {
      try {
        const payload = JSON.parse(message.body);
        console.log("[WS] RAW message received:", payload);
        this.handleNewMessage(payload);
      } catch (e) {
        console.error("Error parsing message:", e);
      }
    });
    this.subscriptions.push(sub);
  },

  /**
   * Subscribe to friend events
   */
  subscribeToFriends(username) {
    const sub = this.stompClient.subscribe(`/user/${username}/queue/friends`, (message) => {
      try {
        const payload = JSON.parse(message.body);
        this.handleFriendEvent(payload);
      } catch (e) {
        console.error("Error parsing friend event:", e);
      }
    });
    this.subscriptions.push(sub);
  },

  /**
   * Subscribe to notifications
   */
  subscribeToNotifications(username) {
    // Subscribe to user queue
    const sub1 = this.stompClient.subscribe(`/user/${username}/queue/notifications`, (message) => {
      try {
        const payload = JSON.parse(message.body);
        this.handleNotification(payload);
      } catch (e) {
        console.error("Error parsing notification:", e);
      }
    });
    this.subscriptions.push(sub1);

    // Subscribe to topic (for server-side push)
    const sub2 = this.stompClient.subscribe(`/topic/notifications/${username}`, (message) => {
      try {
        const payload = JSON.parse(message.body);
        this.handleNotification(payload);
      } catch (e) {
        console.error("Error parsing notification:", e);
      }
    });
    this.subscriptions.push(sub2);
  },

  /**
   * Handle new message received
   */
  handleNewMessage(payload) {
    const user = TokenService.getUser();
    const myUsername = user?.username?.toLowerCase();

    // BE trả về sender và receiver
    const sender = payload.sender?.toLowerCase();
    const receiver = payload.receiver?.toLowerCase();

    // Tin nhắn này do tôi gửi?
    const isMine = sender === myUsername;

    // Xác định người còn lại trong cuộc trò chuyện
    const otherPerson = isMine ? receiver : sender;

    // Chỉ show notification nếu tin nhắn từ người khác
    if (!isMine) {
      this.showNotification(`Tin nhắn mới từ ${payload.sender}`, payload.content?.substring(0, 50) || "", "message");
      this.incrementMessageBadge();
      this.playNotificationSound();
    }

    // Nếu đang chat với người liên quan, reload conversation để cập nhật
    const chatWith = window.app?.currentChatUser?.toLowerCase();
    if (chatWith && chatWith === otherPerson && window.app?.loadConversation) {
      // Reload conversation để hiển thị tin nhắn mới
      window.app.loadConversation(window.app.currentChatUser);
    }
  },

  /**
   * Append a message to the chat (called from real-time updates)
   */
  appendMessageToChat(payload, isSent) {
    console.log("[WS] appendMessageToChat called:", { content: payload.content, isSent });

    const body = document.getElementById("messageBody");
    console.log("[WS] messageBody found:", !!body);
    if (!body) return;

    let messagesList = body.querySelector(".messages-list");
    console.log("[WS] messagesList found:", !!messagesList);

    if (!messagesList) {
      // Create messages list if empty state
      body.innerHTML = '<div class="messages-list"></div>';
      messagesList = body.querySelector(".messages-list");
    }

    const msgDiv = document.createElement("div");
    msgDiv.className = `message-item ${isSent ? "sent" : "received"}`;
    msgDiv.innerHTML = `
      <div class="message-content">${payload.content}</div>
      <div class="message-time">${new Date(payload.createdAt || Date.now()).toLocaleTimeString("vi-VN")}</div>
    `;

    messagesList.appendChild(msgDiv);
    console.log("[WS] Message appended successfully");

    // Scroll to bottom
    body.scrollTop = body.scrollHeight;
  },

  /**
   * Handle friend event
   */
  handleFriendEvent(payload) {
    const eventMessages = {
      REQUEST_RECEIVED: `${payload.from} đã gửi lời mời kết bạn`,
      REQUEST_ACCEPTED: `${payload.from} đã chấp nhận lời mời kết bạn`,
      REQUEST_REJECTED: `${payload.from} đã từ chối lời mời kết bạn`,
      UNFRIENDED: `${payload.from} đã hủy kết bạn`,
    };

    const message = eventMessages[payload.type];
    if (message) {
      this.showNotification("Bạn bè", message, "friend");

      // Refresh friends page if currently viewing
      if (window.app && window.app.currentPage === "friends") {
        window.app.navigate("friends");
      }
    }
  },

  /**
   * Handle general notification
   */
  handleNotification(payload) {
    this.showNotification(payload.title || "Thông báo", payload.content, "notification");
    this.incrementNotificationBadge();
  },

  /**
   * Show notification toast
   */
  showNotification(title, message, type = "info") {
    // Create notification element
    const container = document.getElementById("toastContainer");
    if (!container) return;

    const toast = document.createElement("div");
    toast.className = `toast toast-${type === "message" ? "info" : type === "friend" ? "success" : "info"}`;

    const icons = {
      message: "chat",
      friend: "group",
      notification: "notifications",
    };

    toast.innerHTML = `
      <span class="toast-icon material-symbols-outlined">${icons[type] || "info"}</span>
      <div class="toast-content">
        <strong class="toast-title">${title}</strong>
        <span class="toast-message">${message}</span>
      </div>
      <button class="toast-close" onclick="this.parentElement.remove()">
        <span class="material-symbols-outlined">close</span>
      </button>
    `;

    container.appendChild(toast);

    // Auto remove after 5 seconds
    setTimeout(() => {
      toast.classList.add("toast-exit");
      setTimeout(() => toast.remove(), 300);
    }, 5000);

    // Also show browser notification if permitted
    this.showBrowserNotification(title, message);
  },

  /**
   * Show browser notification
   */
  showBrowserNotification(title, body) {
    if (!("Notification" in window)) return;

    if (Notification.permission === "granted") {
      new Notification(title, {
        body: body,
        icon: "https://ui-avatars.com/api/?name=PTIT&background=ed1c24&color=fff",
      });
    } else if (Notification.permission !== "denied") {
      Notification.requestPermission().then((permission) => {
        if (permission === "granted") {
          new Notification(title, { body: body });
        }
      });
    }
  },

  /**
   * Play notification sound
   */
  playNotificationSound() {
    try {
      // Use Web Audio API for a simple beep
      const audioContext = new (window.AudioContext || window.webkitAudioContext)();
      const oscillator = audioContext.createOscillator();
      const gainNode = audioContext.createGain();

      oscillator.connect(gainNode);
      gainNode.connect(audioContext.destination);

      oscillator.frequency.value = 800;
      oscillator.type = "sine";
      gainNode.gain.setValueAtTime(0.1, audioContext.currentTime);

      oscillator.start(audioContext.currentTime);
      oscillator.stop(audioContext.currentTime + 0.1);
    } catch (e) {
      // Ignore audio errors
    }
  },

  /**
   * Increment message badge count (for new real-time messages only)
   */
  incrementMessageBadge() {
    const badge = document.getElementById("msgBadge");
    if (badge) {
      const currentCount = parseInt(badge.textContent) || 0;
      badge.textContent = currentCount + 1;
      badge.style.display = "flex";
    }
  },

  /**
   * Reset message badge (when user opens messages page)
   */
  resetMessageBadge() {
    const badge = document.getElementById("msgBadge");
    if (badge) {
      badge.textContent = "0";
      badge.style.display = "none";
    }
  },

  /**
   * Increment notification badge count
   */
  incrementNotificationBadge() {
    const badge = document.getElementById("notiBadge");
    if (badge) {
      const currentCount = parseInt(badge.textContent) || 0;
      badge.textContent = currentCount + 1;
      badge.style.display = "flex";
    }
  },

  /**
   * Attempt to reconnect
   */
  attemptReconnect() {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++;
      console.log(`Attempting to reconnect... (${this.reconnectAttempts}/${this.maxReconnectAttempts})`);
      setTimeout(() => this.connect(), 3000 * this.reconnectAttempts);
    }
  },

  /**
   * Disconnect from WebSocket
   */
  disconnect() {
    if (this.stompClient) {
      // Unsubscribe all
      this.subscriptions.forEach((sub) => {
        try {
          sub.unsubscribe();
        } catch (e) {}
      });
      this.subscriptions = [];

      // Disconnect
      try {
        this.stompClient.disconnect();
      } catch (e) {}

      this.stompClient = null;
      this.connected = false;
    }
  },
};

// Make it global
window.WebSocketService = WebSocketService;

// Request notification permission on load
if ("Notification" in window && Notification.permission === "default") {
  Notification.requestPermission();
}
