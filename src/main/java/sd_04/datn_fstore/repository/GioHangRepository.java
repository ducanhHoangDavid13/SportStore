package sd_04.datn_fstore.repository;

import sd_04.datn_fstore.model.GioHang;
import sd_04.datn_fstore.model.KhachHang;
import sd_04.datn_fstore.model.SanPhamChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional; // Cần thiết cho Optional

@Repository
public interface GioHangRepository extends JpaRepository<GioHang, Integer> {

    // Phương thức 1: Tải giỏ hàng chi tiết của khách hàng (Truy vấn hiện tại của bạn)
    // Sẽ được gọi bởi GioHangService.findByKhachHangId(idKhachHang)
    @Query("SELECT gh FROM GioHang gh " +
            "JOIN FETCH gh.sanPhamChiTiet spct " +
            "JOIN FETCH spct.mauSac " +
            "JOIN FETCH spct.kichThuoc " +
            "JOIN FETCH spct.sanPham sp " +
            "LEFT JOIN FETCH sp.hinhAnh " +
            "WHERE gh.khachHang.id = :idKhachHang AND gh.trangThai IN (0, 1)")
    List<GioHang> findByKhachHangId(@Param("idKhachHang") Integer idKhachHang);

    // 🔥 PHƯƠNG THỨC BỔ SUNG 1: Tìm kiếm một sản phẩm cụ thể trong giỏ hàng của Khách hàng.
    // Dùng để kiểm tra: sản phẩm đã tồn tại trong giỏ chưa? (trong logic themHoacCapNhat)
    // Spring Data JPA sẽ tự tạo truy vấn này dựa trên tên phương thức và Model.
    Optional<GioHang> findByKhachHangAndSanPhamChiTiet(KhachHang khachHang, SanPhamChiTiet sanPhamChiTiet);

    // 🔥 PHƯƠNG THỨC BỔ SUNG 2: Phương thức truy vấn giỏ hàng đơn giản theo ID Khách Hàng.
    // Phương thức này có tên khác đi để tránh xung đột với phương thức FETCH JOIN ở trên,
    // và để khớp với Service (findByKhachHang_Id thay vì findByKhachHangId)
    // Nó được gọi bởi GioHangService.findByKhachHang_Id(idKhachHang)
    List<GioHang> findByKhachHang_Id(Integer idKhachHang);

}