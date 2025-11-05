package sd_04.datn_fstore.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sd_04.datn_fstore.model.TheLoai;

@Repository
public interface TheLoaiRepository extends JpaRepository<TheLoai, Integer> {

    /**
     * Truy vấn tìm kiếm và phân trang cho TheLoai.
     * ĐÃ THÊM countQuery để khắc phục lỗi phân trang (lỗi ':keyword_1')
     */
    @Query(value = "SELECT tl FROM TheLoai tl WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR tl.tenTheLoai LIKE %:keyword%) AND " +
            "(:trangThai IS NULL OR tl.trangThai = :trangThai)",

            // >>>>> PHẦN BỔ SUNG ĐỂ KHẮC PHỤC LỖI COUNT QUERY <<<<<
            countQuery = "SELECT COUNT(tl) FROM TheLoai tl WHERE " +
                    "(:keyword IS NULL OR :keyword = '' OR tl.tenTheLoai LIKE %:keyword%) AND " +
                    "(:trangThai IS NULL OR tl.trangThai = :trangThai)")
    Page<TheLoai> findPaginated(Pageable pageable,
                                @Param("keyword") String keyword,
                                @Param("trangThai") Integer trangThai);
}