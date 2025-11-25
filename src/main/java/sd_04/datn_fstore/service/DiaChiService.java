package sd_04.datn_fstore.service;

import sd_04.datn_fstore.model.DiaChi;
import java.util.List;
import java.util.Optional;

public interface DiaChiService {


    /**
     * Lấy danh sách tất cả địa chỉ đang hoạt động (trangThai = 1) của một khách hàng,
     * sắp xếp theo ID giảm dần (địa chỉ mới nhất lên đầu).
     * @param khachHangId ID của khách hàng
     * @return Danh sách các đối tượng DiaChi
     */
    List<DiaChi> getDiaChiByKhachHangId(Integer khachHangId);

    /**
     * Tìm một địa chỉ cụ thể bằng ID của nó.
     * @param diaChiId ID của địa chỉ cần tìm
     * @return Optional chứa đối tượng DiaChi nếu tìm thấy, ngược lại là Optional rỗng
     */
    Optional<DiaChi> getById(Integer diaChiId);

    /**
     * Thêm một địa chỉ mới cho một khách hàng.
     * @param diaChi Đối tượng DiaChi chứa thông tin mới (chưa có ID và KhachHang)
     * @param idKhachHang ID của khách hàng sở hữu địa chỉ này
     * @return Đối tượng DiaChi đã được lưu và có ID
     */
    DiaChi saveNewAddress(DiaChi diaChi, Integer idKhachHang);

    /**
     * Cập nhật thông tin của một địa chỉ đã tồn tại.
     * @param diaChiId ID của địa chỉ cần cập nhật
     * @param updatedDiaChi Đối tượng DiaChi chứa thông tin mới
     * @return Đối tượng DiaChi đã được cập nhật
     */
    DiaChi updateAddress(Integer diaChiId, DiaChi updatedDiaChi);

    /**
     * Xóa mềm một địa chỉ bằng cách đổi trạng thái của nó thành 0 (ngừng hoạt động).
     * @param diaChiId ID của địa chỉ cần xóa
     */
    void deleteAddress(Integer diaChiId);
}