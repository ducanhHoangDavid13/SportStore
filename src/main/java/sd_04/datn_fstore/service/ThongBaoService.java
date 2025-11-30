package sd_04.datn_fstore.service;

import sd_04.datn_fstore.model.ThongBao;
import java.util.List;

public interface ThongBaoService {

    // Tạo thông báo mới
    void createNotification(String tieuDe, String noiDung, String loai, String url);

    // Lấy danh sách chưa đọc
    List<ThongBao> getUnreadNotifications();

    // Đánh dấu đã đọc
    void markAsRead(Integer id);
}