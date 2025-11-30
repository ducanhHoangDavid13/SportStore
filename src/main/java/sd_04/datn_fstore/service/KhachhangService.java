package sd_04.datn_fstore.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sd_04.datn_fstore.dto.KhachHangRegistration;
import sd_04.datn_fstore.enums.RoleEnum;
import sd_04.datn_fstore.model.Account;
import sd_04.datn_fstore.model.KhachHang;
import sd_04.datn_fstore.repository.AccountRepository;
import sd_04.datn_fstore.repository.KhachHangRepo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class KhachhangService {
    private final KhachHangRepo khachHangRepo;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    // Hàm chính: Lấy danh sách khách hàng đã được LỌC và PHÂN TRANG
    public Page<KhachHang> getFilteredKhachHang(
            String keyword, String sdt, Boolean gioiTinh, int pageNo, int pageSize) {

        Pageable pageable = PageRequest.of(pageNo - 1, pageSize);

        String searchKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        String searchSdt = (sdt != null && !sdt.trim().isEmpty()) ? sdt.trim() : null;

        return khachHangRepo.findFilteredKhachHang(searchKeyword, searchSdt, gioiTinh, pageable);
    }

    public KhachHang save(KhachHangRegistration registration) {
        Optional<Account> account = accountRepository.findByEmail(registration.getEmail());
        Optional<KhachHang> khachhangOpt = khachHangRepo.findByEmail(registration.getEmail());

        if (khachhangOpt.isPresent() || account.isPresent()) {
            new RuntimeException("Tài khoản đã tồi tại.");
        }

        // save tai khoan
        Account save = account.orElse(new Account());
        save.setEmail(registration.getEmail());
        save.setPassword(passwordEncoder.encode(registration.getPassword()));
        save.setRole(RoleEnum.USDE);
        accountRepository.save(save);

        // save khachhang
        KhachHang khachhang = new KhachHang();
        khachhang.setMaKhachHang(registration.getMaKhachHang());
        khachhang.setTenKhachHang(registration.getTenKhachHang());
        khachhang.setSoDienThoai(registration.getSoDienThoai());
        khachhang.setEmail(registration.getEmail());
        khachhang.setGioiTinh(registration.getGioiTinh());
        khachhang.setNgaySinh(registration.getNgaySinh());
        khachhang.setVaiTro(registration.getVaiTro());
        khachhang.setNgayTao(LocalDateTime.now());
        khachhang.setTrangThai(1);

        return khachHangRepo.save(khachhang);
    }

    public KhachHang update(KhachHang khachhang) {
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