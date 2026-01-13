package sd_04.datn_fstore.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sd_04.datn_fstore.model.MauSac;

import java.util.List;

@Repository
public interface MauSacRepository extends JpaRepository<MauSac, Integer> {

    /**
     * Câu lệnh Query: Tìm kiếm màu sắc (theo mã hoặc tên) VÀ lọc theo trạng thái,
     * kết quả trả về có phân trang (Pageable).
     * ĐÃ THÊM countQuery để khắc phục lỗi phân trang (lỗi ':keyword_1').
     */
    @Query(value = "SELECT ms FROM MauSac ms WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR ms.tenMauSac LIKE %:keyword% OR ms.maMau LIKE %:keyword%) AND " +
            "(:trangThai IS NULL OR ms.trangThai = :trangThai)",

            // >>>>> PHẦN BỔ SUNG ĐỂ KHẮC PHỤC LỖI COUNT QUERY <<<<<
            countQuery = "SELECT COUNT(ms) FROM MauSac ms WHERE " +
                    "(:keyword IS NULL OR :keyword = '' OR ms.tenMauSac LIKE %:keyword% OR ms.maMau LIKE %:keyword%) AND " +
                    "(:trangThai IS NULL OR ms.trangThai = :trangThai)")
    Page<MauSac> findPaginated(Pageable pageable,
                               @Param("keyword") String keyword,
                               @Param("trangThai") Integer trangThai);

    MauSac findByMaMau(String maMau);

    List<MauSac> findByTrangThai(Integer trangThai);
}