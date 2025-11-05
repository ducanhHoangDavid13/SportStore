package sd_04.datn_fstore.repository;

// 1. Thêm import cho Page, Pageable và LocalDateTime
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sd_04.datn_fstore.model.HoaDon;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HoaDonRepository extends JpaRepository<HoaDon, Integer> {
    @Query(value = "SELECT hd FROM HoaDon hd WHERE " +
            // ----- SỬA DÒNG NÀY -----
            "(:trangThaiList IS NULL OR hd.trangThai IN :trangThaiList) AND " +
            // (Đã xóa ':trangThaiList IS EMPTY')
            // -----------------------

            "(:ngayBatDau IS NULL OR hd.ngayTao >= :ngayBatDau) AND " +
            "(:ngayKetThuc IS NULL OR hd.ngayTao <= :ngayKetThuc) AND " +
            "(:keyword IS NULL OR hd.maHoaDon LIKE %:keyword%)",

            // ----- SỬA DÒNG NÀY (CHO countQuery) -----
            countQuery = "SELECT COUNT(hd) FROM HoaDon hd WHERE " +
                    "(:trangThaiList IS NULL OR hd.trangThai IN :trangThaiList) AND " +
                    // (Đã xóa ':trangThaiList IS EMPTY')
                    // ----------------------------------------

                    "(:ngayBatDau IS NULL OR hd.ngayTao >= :ngayBatDau) AND " +
                    "(:ngayKetThuc IS NULL OR hd.ngayTao <= :ngayKetThuc) AND " +
                    "(:keyword IS NULL OR hd.maHoaDon LIKE %:keyword%)")
    Page<HoaDon> searchByTrangThaiAndNgayTao(
            Pageable pageable,
            @Param("trangThaiList") List<Integer> trangThaiList,
            @Param("ngayBatDau") LocalDateTime ngayBatDau,
            @Param("ngayKetThuc") LocalDateTime ngayKetThuc,
            @Param("keyword") String keyword
    );
    Optional<HoaDon> findByMaHoaDon(String maHoaDon);
}