package sd_04.datn_fstore.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import sd_04.datn_fstore.model.NhanVien;

import java.util.Optional;

@Repository
public interface NhanVienRepository extends JpaRepository<NhanVien, Integer> {

    @Query(value = "SELECT nv FROM NhanVien nv WHERE " +
            "(:keyword IS NULL OR nv.maNhanVien LIKE %:keyword% OR nv.tenNhanVien LIKE %:keyword%) AND " +
            "(:vaiTro IS NULL OR nv.vaiTro = :vaiTro) AND " +
            "(:trangThai IS NULL OR nv.trangThai = :trangThai)",

            countQuery = "SELECT COUNT(nv) FROM NhanVien nv WHERE " +
                    "(:keyword IS NULL OR nv.maNhanVien LIKE %:keyword% OR nv.tenNhanVien LIKE %:keyword%) AND " +
                    "(:vaiTro IS NULL OR nv.vaiTro = :vaiTro) AND " +
                    "(:trangThai IS NULL OR nv.trangThai = :trangThai)")
    Page<NhanVien> search(Pageable pageable, String keyword, String vaiTro, Integer trangThai);
    Optional<NhanVien> findByEmail(String email);
}