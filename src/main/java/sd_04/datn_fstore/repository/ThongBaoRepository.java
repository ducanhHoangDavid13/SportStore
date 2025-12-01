package sd_04.datn_fstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sd_04.datn_fstore.model.ThongBao;
import java.util.List;

@Repository
public interface ThongBaoRepository extends JpaRepository<ThongBao, Integer> {
    // Lấy tất cả thông báo chưa đọc, mới nhất lên đầu
    List<ThongBao> findByTrangThaiOrderByNgayTaoDesc(Integer trangThai);

    // Lấy 10 thông báo gần nhất (kể cả đã đọc) để hiển thị lịch sử
    List<ThongBao> findTop10ByOrderByNgayTaoDesc();
    boolean existsByLoaiThongBaoAndNoiDungAndTrangThai(String loaiThongBao, String noiDung, Integer trangThai);
}