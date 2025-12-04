package sd_04.datn_fstore.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sd_04.datn_fstore.model.XuatXu;

import java.util.List;

@Repository
public interface XuatXuRepository extends JpaRepository<XuatXu, Integer> {

    /**
     * Truy vấn tìm kiếm và phân trang cho XuatXu.
     * ĐÃ THÊM countQuery để khắc phục lỗi phân trang (lỗi ':keyword_1').
     */
    @Query(value = "SELECT xx FROM XuatXu xx WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR xx.tenXuatXu LIKE %:keyword%) AND " +
            "(:trangThai IS NULL OR xx.trangThai = :trangThai)",

            // >>>>> PHẦN BỔ SUNG ĐỂ KHẮC PHỤC LỖI COUNT QUERY <<<<<
            countQuery = "SELECT COUNT(xx) FROM XuatXu xx WHERE " +
                    "(:keyword IS NULL OR :keyword = '' OR xx.tenXuatXu LIKE %:keyword%) AND " +
                    "(:trangThai IS NULL OR xx.trangThai = :trangThai)")
    Page<XuatXu> findPaginated(Pageable pageable,
                               @Param("keyword") String keyword,
                               @Param("trangThai") Integer trangThai);

    List<XuatXu> findByTrangThai(Integer trangThai);
}