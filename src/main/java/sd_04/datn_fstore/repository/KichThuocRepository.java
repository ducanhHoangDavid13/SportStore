package sd_04.datn_fstore.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sd_04.datn_fstore.model.KichThuoc;

@Repository
public interface KichThuocRepository extends JpaRepository<KichThuoc, Integer> {

    /**
     * Tìm kiếm và phân trang theo keyword (chỉ theo tên) và trạng thái.
     */
    @Query("SELECT kt FROM KichThuoc kt WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR kt.tenKichThuoc LIKE %:keyword%) AND " +
            "(:trangThai IS NULL OR kt.trangThai = :trangThai)")
    Page<KichThuoc> findPaginated(Pageable pageable,
                                  @Param("keyword") String keyword,
                                  @Param("trangThai") Integer trangThai);

    // Đã loại bỏ phương thức findByMaKichThuoc
}