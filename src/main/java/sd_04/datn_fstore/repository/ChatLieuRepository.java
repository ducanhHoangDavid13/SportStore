package sd_04.datn_fstore.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sd_04.datn_fstore.model.ChatLieu;

import java.util.List;

@Repository
public interface ChatLieuRepository extends JpaRepository<ChatLieu, Integer> {

    /**
     * Truy vấn tìm kiếm và phân trang cho ChatLieu.
     * ĐÃ THÊM countQuery để khắc phục lỗi Hibernate/JPA khi tự động tạo truy vấn đếm phức tạp
     * (thường gây ra lỗi ':keyword_1' trong quá trình phân trang).
     */
    @Query(value = "SELECT cl FROM ChatLieu cl WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR cl.loaiChatLieu LIKE %:keyword%) AND " +
            "(:trangThai IS NULL OR cl.trangThai = :trangThai)",

            // >>>>> PHẦN BỔ SUNG ĐỂ KHẮC PHỤC LỖI COUNT QUERY <<<<<
            countQuery = "SELECT COUNT(cl) FROM ChatLieu cl WHERE " +
                    "(:keyword IS NULL OR :keyword = '' OR cl.loaiChatLieu LIKE %:keyword%) AND " +
                    "(:trangThai IS NULL OR cl.trangThai = :trangThai)")
    Page<ChatLieu> findPaginated(Pageable pageable,
                                 @Param("keyword") String keyword,
                                 @Param("trangThai") Integer trangThai);

    List<ChatLieu> findByTrangThai(Integer trangThai);
}