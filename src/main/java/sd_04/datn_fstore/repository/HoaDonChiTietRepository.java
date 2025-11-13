package sd_04.datn_fstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sd_04.datn_fstore.model.HoaDonChiTiet;

import java.util.List;

@Repository
public interface HoaDonChiTietRepository extends JpaRepository<HoaDonChiTiet, Integer> {

    // Đếm xem có bao nhiêu hóa đơn đang dùng 1 biến thể
    long countBySanPhamChiTietId(Integer sanPhamChiTietId);

    // BỔ SUNG: Lấy tất cả chi tiết hóa đơn theo ID của hóa đơn
    List<HoaDonChiTiet> findByHoaDonId(Integer hoaDonId);
}