package sd_04.datn_fstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sd_04.datn_fstore.model.HoaDon;

import java.util.Date;
import java.util.List;

@Repository
public interface HoaDonRepository extends JpaRepository<HoaDon, Integer> {

    // Lọc theo trạng thái
    List<HoaDon> findByTrangThai(Integer trangThai);

    // Lọc theo ngày tạo
    List<HoaDon> findByNgayTaoBetween(Date startDate, Date endDate);
}
