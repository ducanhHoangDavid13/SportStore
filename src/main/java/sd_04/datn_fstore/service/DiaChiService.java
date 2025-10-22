package sd_04.datn_fstore.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sd_04.datn_fstore.model.DiaChi;
import sd_04.datn_fstore.repo.DiaChiRepo;

import java.util.List;

@Service
public class DiaChiService {
    @Autowired
    private DiaChiRepo diaChiRepo;

    // Lấy danh sách địa chỉ hoạt động (trangThai = 1) theo ID khách hàng
    public List<DiaChi> getDiaChiByKhachHangId(Integer khachHangId) {
        final int TRANG_THAI_HOAT_DONG = 1;
        // Gọi Repo với điều kiện trangThai = 1
        return diaChiRepo.findByKhachhang_IdAndTrangThai(khachHangId, TRANG_THAI_HOAT_DONG);
    }
}
