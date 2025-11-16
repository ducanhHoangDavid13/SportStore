package sd_04.datn_fstore.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import sd_04.datn_fstore.model.NhanVien;

import java.util.List; // Kept from second commit
import java.util.Optional;

@Repository
public interface NhanVienRepository extends JpaRepository<NhanVien, Integer> {

    // Paginated Search (Kept from first commit)
    @Query(value = "SELECT nv FROM NhanVien nv WHERE " +
            "(:keyword IS NULL OR nv.maNhanVien LIKE %:keyword% OR nv.tenNhanVien LIKE %:keyword%) AND " +
            "(:vaiTro IS NULL OR nv.vaiTro = :vaiTro) AND " +
            "(:trangThai IS NULL OR nv.trangThai = :trangThai)",

            countQuery = "SELECT COUNT(nv) FROM NhanVien nv WHERE " +
                    "(:keyword IS NULL OR nv.maNhanVien LIKE %:keyword% OR nv.tenNhanVien LIKE %:keyword%) AND " +
                    "(:vaiTro IS NULL OR nv.vaiTro = :vaiTro) AND " +
                    "(:trangThai IS NULL OR nv.trangThai = :trangThai)")
    Page<NhanVien> search(Pageable pageable, String keyword, String vaiTro, Integer trangThai);

    // Non-Paginated Search (Kept and renamed/modified from second commit to avoid conflict)
    // NOTE: I'm leaving the method name as 'search' for now, but if you intend to use both the Pageable
    // and non-Pageable versions, you might want to rename this one (e.g., to searchList) for clarity
    // in your service layer, although the different parameters prevent a *signature* conflict.
    @Query(value = "SELECT nv FROM NhanVien nv WHERE " +
            "(:keyword IS NULL OR nv.maNhanVien LIKE %:keyword% OR nv.tenNhanVien LIKE %:keyword%) AND " +
            "(:vaiTro IS NULL OR nv.vaiTro = :vaiTro) AND " +
            "(:trangThai IS NULL OR nv.trangThai = :trangThai)")
    List<NhanVien> search(String keyword, String vaiTro, Integer trangThai);

    // findByEmail (Kept from both)
    Optional<NhanVien> findByEmail(String email);
}