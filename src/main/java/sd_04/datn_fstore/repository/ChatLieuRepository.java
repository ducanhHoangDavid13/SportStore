package sd_04.datn_fstore.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sd_04.datn_fstore.model.ChatLieu;

@Repository
public interface ChatLieuRepository extends JpaRepository<ChatLieu, Integer> {

    @Query("SELECT cl FROM ChatLieu cl WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR cl.loaiChatLieu LIKE %:keyword%) AND " +
            "(:trangThai IS NULL OR cl.trangThai = :trangThai)")
    Page<ChatLieu> findPaginated(Pageable pageable,
                                 @Param("keyword") String keyword,
                                 @Param("trangThai") Integer trangThai);
}