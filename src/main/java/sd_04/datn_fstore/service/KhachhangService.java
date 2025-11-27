package sd_04.datn_fstore.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sd_04.datn_fstore.model.KhachHang;
import sd_04.datn_fstore.repository.KhachHangRepo;

import java.util.List;
import java.util.Optional;

@Service
public class KhachhangService {
    @Autowired
    private KhachHangRepo khachHangRepo;

    // Hàm chính: Lấy danh sách khách hàng đã được LỌC và PHÂN TRANG
    public Page<KhachHang> getFilteredKhachHang(
            String keyword, String sdt, Boolean gioiTinh, int pageNo, int pageSize) {

        Pageable pageable = PageRequest.of(pageNo - 1, pageSize);

        String searchKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        String searchSdt = (sdt != null && !sdt.trim().isEmpty()) ? sdt.trim() : null;

        return khachHangRepo.findFilteredKhachHang(searchKeyword, searchSdt, gioiTinh, pageable);
    }

    public KhachHang save(KhachHang khachhang) {
        if (khachhang.getId() == null) {
            khachhang.setTrangThai(1);
        }
        return khachHangRepo.save(khachhang);
    }

    public Optional<KhachHang> findById(Integer id) {
        return khachHangRepo.findById(id);
    }

    public void softDeleteById(Integer id) {
        Optional<KhachHang> khachhangOpt = khachHangRepo.findById(id);
        if (khachhangOpt.isPresent()) {
            KhachHang khachhang = khachhangOpt.get();
            khachhang.setTrangThai(0);
            khachHangRepo.save(khachhang);
        } else {
            throw new RuntimeException("Không tìm thấy khách hàng với ID: " + id);
        }
    }

    public List<KhachHang> findAll() {
        return khachHangRepo.findAll();
    }
    public List<KhachHang> searchCustomerByNameOrPhone(String keyword) {
        String searchKeyword = (keyword != null && !keyword.trim().isEmpty()) ? "%" + keyword.trim() + "%" : null;

        if (searchKeyword == null) {
            return khachHangRepo.findAll();
        }

        return khachHangRepo.findByTenKhachHangLikeOrSoDienThoaiLike(searchKeyword, searchKeyword);
    }
}