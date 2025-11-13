package sd_04.datn_fstore.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sd_04.datn_fstore.model.PhanLoai;

@Repository
public interface PhanLoaiRepository extends JpaRepository<PhanLoai, Integer> {

    /**
     * Truy vấn tìm kiếm và phân trang cho PhanLoai.
     * ĐÃ THÊM countQuery để khắc phục lỗi phân trang (lỗi ':keyword_1')
     */
    @Query(value = "SELECT pl FROM PhanLoai pl WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR pl.phanLoai LIKE %:keyword%) AND " +
            "(:trangThai IS NULL OR pl.trangThai = :trangThai)",

            // >>>>> PHẦN BỔ SUNG ĐỂ KHẮC PHỤC LỖI COUNT QUERY <<<<<
            countQuery = "SELECT COUNT(pl) FROM PhanLoai pl WHERE " +
                    "(:keyword IS NULL OR :keyword = '' OR pl.phanLoai LIKE %:keyword%) AND " +
                    "(:trangThai IS NULL OR pl.trangThai = :trangThai)")
    Page<PhanLoai> findPaginated(Pageable pageable,
                                 @Param("keyword") String keyword,
                                 @Param("trangThai") Integer trangThai);
}