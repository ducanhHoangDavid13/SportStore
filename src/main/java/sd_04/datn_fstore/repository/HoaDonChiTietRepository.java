package sd_04.datn_fstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import sd_04.datn_fstore.dto.TopProductDTO;
import sd_04.datn_fstore.model.HoaDonChiTiet;

import java.util.List;

@Repository
public interface HoaDonChiTietRepository extends JpaRepository<HoaDonChiTiet, Integer> {

    // Đếm xem có bao nhiêu hóa đơn đang dùng 1 biến thể
    long countBySanPhamChiTietId(Integer sanPhamChiTietId);

    // BỔ SUNG: Lấy tất cả chi tiết hóa đơn theo ID của hóa đơn
    List<HoaDonChiTiet> findByHoaDonId(Integer hoaDonId);
// ... (Các hàm cũ giữ nguyên)

    // [MỚI] Lấy Top 5 sản phẩm bán chạy nhất
    // Lưu ý: Ta group theo ID sản phẩm cha để tính tổng số lượng
    @Query("SELECT new sd_04.datn_fstore.dto.TopProductDTO(" +
            " ct.sanPhamChiTiet.sanPham.tenSanPham, " +
            " MAX(ct.donGia), " + // Lấy giá đại diện cao nhất
            " SUM(ct.soLuong)) " +
            " FROM HoaDonChiTiet ct " +
            " GROUP BY ct.sanPhamChiTiet.sanPham.id, ct.sanPhamChiTiet.sanPham.tenSanPham " +
            " ORDER BY SUM(ct.soLuong) DESC LIMIT 5")
    List<TopProductDTO> findTop5BestSellers();
}