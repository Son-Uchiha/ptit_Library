package com.ptit.library.controller.api;

import com.ptit.library.dto.response.ApiResponse;
import com.ptit.library.dto.response.FriendResponse;
import com.ptit.library.service.FriendService;
import com.ptit.library.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/friends")
public class FriendRestController {

    @Autowired
    private FriendService friendService;

    /**
     * GET /api/friends
     * Lấy danh sách bạn bè
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<?>>> listFriends() {
        String me = SecurityUtil.getAuthenticatedUsername();
        if (me == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Vui lòng đăng nhập", 401));
        }

        try {
            return ResponseEntity.ok(ApiResponse.success(friendService.listFriends(me)));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi: " + e.getMessage()));
        }
    }

    /**
     * GET /api/friends/pending/received
     * Lấy danh sách lời mời kết bạn đã nhận
     */
    @GetMapping("/pending/received")
    public ResponseEntity<ApiResponse<List<?>>> listPendingReceived() {
        String me = SecurityUtil.getAuthenticatedUsername();
        if (me == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Vui lòng đăng nhập", 401));
        }

        return ResponseEntity.ok(ApiResponse.success(friendService.listPendingReceived(me)));
    }

    /**
     * GET /api/friends/pending/sent
     * Lấy danh sách lời mời kết bạn đã gửi
     */
    @GetMapping("/pending/sent")
    public ResponseEntity<ApiResponse<List<?>>> listPendingSent() {
        String me = SecurityUtil.getAuthenticatedUsername();
        if (me == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Vui lòng đăng nhập", 401));
        }

        return ResponseEntity.ok(ApiResponse.success(friendService.listPendingSent(me)));
    }

    /**
     * POST /api/friends/request
     * Gửi lời mời kết bạn
     */
    @PostMapping("/request")
    public ResponseEntity<ApiResponse<Void>> requestFriend(@RequestParam("to") String to) {
        String me = SecurityUtil.getAuthenticatedUsername();
        if (me == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Vui lòng đăng nhập", 401));
        }

        try {
            friendService.requestFriend(me, to);
            return ResponseEntity.ok(ApiResponse.success("Đã gửi lời mời kết bạn"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * POST /api/friends/accept
     * Chấp nhận lời mời kết bạn
     */
    @PostMapping("/accept")
    public ResponseEntity<ApiResponse<Void>> acceptFriend(@RequestParam("of") String of) {
        String me = SecurityUtil.getAuthenticatedUsername();
        if (me == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Vui lòng đăng nhập", 401));
        }

        try {
            friendService.acceptFriend(me, of);
            return ResponseEntity.ok(ApiResponse.success("Đã chấp nhận lời mời kết bạn"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * POST /api/friends/decline
     * Từ chối lời mời kết bạn
     */
    @PostMapping("/decline")
    public ResponseEntity<ApiResponse<Void>> declineFriend(@RequestParam("of") String of) {
        String me = SecurityUtil.getAuthenticatedUsername();
        if (me == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Vui lòng đăng nhập", 401));
        }

        try {
            friendService.declineFriend(me, of);
            return ResponseEntity.ok(ApiResponse.success("Đã từ chối lời mời kết bạn"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * DELETE /api/friends/{username}
     * Xóa bạn bè
     */
    @DeleteMapping("/{username}")
    public ResponseEntity<ApiResponse<Void>> removeFriend(@PathVariable String username) {
        String me = SecurityUtil.getAuthenticatedUsername();
        if (me == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Vui lòng đăng nhập", 401));
        }

        try {
            friendService.removeFriend(me, username);
            return ResponseEntity.ok(ApiResponse.success("Đã xóa bạn bè"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * POST /api/friends/block
     * Chặn người dùng
     */
    @PostMapping("/block")
    public ResponseEntity<ApiResponse<Void>> blockUser(@RequestParam("user") String user) {
        String me = SecurityUtil.getAuthenticatedUsername();
        if (me == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Vui lòng đăng nhập", 401));
        }

        try {
            friendService.blockUser(me, user);
            return ResponseEntity.ok(ApiResponse.success("Đã chặn người dùng"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * POST /api/friends/unblock
     * Bỏ chặn người dùng
     */
    @PostMapping("/unblock")
    public ResponseEntity<ApiResponse<Void>> unblockUser(@RequestParam("user") String user) {
        String me = SecurityUtil.getAuthenticatedUsername();
        if (me == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Vui lòng đăng nhập", 401));
        }

        try {
            friendService.unblockUser(me, user);
            return ResponseEntity.ok(ApiResponse.success("Đã bỏ chặn người dùng"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * GET /api/friends/is-friends/{username}
     * Kiểm tra có phải bạn bè không
     */
    @GetMapping("/is-friends/{username}")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> isFriends(@PathVariable String username) {
        String me = SecurityUtil.getAuthenticatedUsername();
        if (me == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Vui lòng đăng nhập", 401));
        }

        Map<String, Boolean> data = new HashMap<>();
        data.put("friends", friendService.isFriends(me, username));

        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
