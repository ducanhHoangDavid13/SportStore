package sd_04.datn_fstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sd_04.datn_fstore.model.HoaDonChiTiet;

@Repository
public interface HoaDonChiTietRepository extends JpaRepository<HoaDonChiTiet, Integer> {
    // Đếm xem có bao nhiêu hóa đơn đang dùng 1 biến thể
    long countBySanPhamChiTietId(Integer sanPhamChiTietId);
}
