package sd_04.datn_fstore.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sd_04.datn_fstore.service.ThongBaoService;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationApiController {

    private final ThongBaoService thongBaoService;

    // 1. API lấy danh sách chưa đọc (JS sẽ gọi mỗi 10s)
    @GetMapping("/unread")
    public ResponseEntity<?> getUnread() {
        return ResponseEntity.ok(thongBaoService.getUnreadNotifications());
    }

    // 2. API đánh dấu đã đọc 1 cái khi bấm vào
    @PostMapping("/read/{id}")
    public ResponseEntity<?> markRead(@PathVariable Integer id) {
        thongBaoService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    // 3. [CẦN THÊM] API đánh dấu TẤT CẢ là đã đọc
    @PostMapping("/read-all")
    public ResponseEntity<?> markAllAsRead() {
        // Lưu ý: Bạn cần update cả bên Service để có hàm này
        thongBaoService.markAllAsRead();
        return ResponseEntity.ok().build();
    }
}