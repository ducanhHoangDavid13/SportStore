package sd_04.datn_fstore.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sd_04.datn_fstore.model.DiaChi;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DiaChiRepo extends JpaRepository<DiaChi, Integer> {


    /**
     * TÌM KIẾM CÁC ĐỊA CHỈ HOẠT ĐỘNG CỦA MỘT KHÁCH HÀNG
     *
     * - findBy: Bắt đầu một truy vấn
     * - Khachhang_Id: Truy cập vào trường 'id' của thuộc tính 'khachhang' trong entity DiaChi.
     *   (QUAN TRỌNG: 'Khachhang' phải viết hoa chữ cái đầu theo quy ước, dù tên thuộc tính là 'khachhang')
     * - AndTrangThai: Kết hợp với điều kiện trên trường 'trangThai'
     * - OrderByIdDesc: Sắp xếp kết quả theo trường 'id' giảm dần (địa chỉ mới nhất lên đầu)
     */
    List<DiaChi> findByKhachhang_IdAndTrangThaiOrderByIdDesc(Integer khachHangId, Integer trangThai);

    // CÁC PHƯƠNG THỨC CŨ ĐÃ ĐƯỢC XÓA VÌ KHÔNG CÒN PHÙ HỢP:
    // - findByKhachHangIdAndTrangThaiOrderByDiaChiMacDinhDesc (sai tên thuộc tính và dùng trường không tồn tại)
    // - countByKhachHangIdAndTrangThai (không cần thiết, có thể dùng list.size())
    // - findByKhachHangIdAndDiaChiMacDinhIsTrueAndTrangThai (dùng trường không tồn tại)
    // - clearDefaultAddressForCustomer (dùng trường không tồn tại)
    // - setDefaultAddress (dùng trường không tồn tại)
}
