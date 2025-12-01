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

    // API lấy danh sách chưa đọc (JS sẽ gọi mỗi 10s)
    @GetMapping("/unread")
    public ResponseEntity<?> getUnread() {
        return ResponseEntity.ok(thongBaoService.getUnreadNotifications());
    }

    // API đánh dấu đã đọc khi bấm vào
    @PostMapping("/read/{id}")
    public ResponseEntity<?> markRead(@PathVariable Integer id) {
        thongBaoService.markAsRead(id);
        return ResponseEntity.ok().build();
    }
}