package sd_04.datn_fstore.repository;

import sd_04.datn_fstore.model.DiaChi;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiaChiRepo extends JpaRepository<DiaChi, Integer> {
    List<DiaChi> findByKhachhang_IdAndTrangThai(Integer khachHangId, Integer trangThai);
}
