package sd_04.datn_fstore.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sd_04.datn_fstore.model.KichThuoc;

import java.util.List;

@Repository
public interface KichThuocRepository extends JpaRepository<KichThuoc, Integer> {

    /**
     * Tìm kiếm và phân trang theo keyword (chỉ theo tên) và trạng thái.
     * ĐÃ THÊM countQuery để khắc phục lỗi phân trang (lỗi ':keyword_1')
     */
    @Query(value = "SELECT kt FROM KichThuoc kt WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR kt.tenKichThuoc LIKE %:keyword%) AND " +
            "(:trangThai IS NULL OR kt.trangThai = :trangThai)",

            // >>>>> PHẦN BỔ SUNG ĐỂ KHẮC PHỤC LỖI COUNT QUERY <<<<<
            countQuery = "SELECT COUNT(kt) FROM KichThuoc kt WHERE " +
                    "(:keyword IS NULL OR :keyword = '' OR kt.tenKichThuoc LIKE %:keyword%) AND " +
                    "(:trangThai IS NULL OR kt.trangThai = :trangThai)")
    Page<KichThuoc> findPaginated(Pageable pageable,
                                  @Param("keyword") String keyword,
                                  @Param("trangThai") Integer trangThai);

    List<KichThuoc> findByTrangThai(Integer trangThai);
}