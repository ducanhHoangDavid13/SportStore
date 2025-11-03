package sd_04.datn_fstore.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sd_04.datn_fstore.model.HoaDon;
import sd_04.datn_fstore.repository.HoaDonRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HoaDonService {

    private final HoaDonRepository hoaDonRepository;

    public List<HoaDon> getAll() {
        return hoaDonRepository.findAll();
    }

    public Optional<HoaDon> getById(Integer id) {
        return hoaDonRepository.findById(id);
    }

    public HoaDon add(HoaDon hoaDon) {
        if (hoaDon.getNgayTao() == null) {
            hoaDon.setNgayTao(new Date());
        }
        return hoaDonRepository.save(hoaDon);
    }

    public HoaDon update(Integer id, HoaDon hoaDon) {
        hoaDon.setId(id);
        return hoaDonRepository.save(hoaDon);
    }

    public void delete(Integer id) {
        hoaDonRepository.deleteById(id);
    }

    public List<HoaDon> getByTrangThai(Integer trangThai) {
        return hoaDonRepository.findByTrangThai(trangThai);
    }

    public List<HoaDon> getByDateRange(Date start, Date end) {
        return hoaDonRepository.findByNgayTaoBetween(start, end);
    }
}
