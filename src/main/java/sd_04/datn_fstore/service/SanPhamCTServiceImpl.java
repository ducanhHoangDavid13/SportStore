package sd_04.datn_fstore.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sd_04.datn_fstore.model.SanPham;
import sd_04.datn_fstore.model.SanPhamChiTiet;
import sd_04.datn_fstore.repository.SanPhamCTRepository;


import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SanPhamCTServiceImpl implements SanPhamCTService {

    private final SanPhamCTRepository sanPhamChiTietRepository;

    @Override
    public List<SanPhamChiTiet> getAll() {
        return sanPhamChiTietRepository.findAll();
    }

    @Override
    public Page<SanPhamChiTiet> getAll(Pageable pageable) {
        return sanPhamChiTietRepository.findAll(pageable);
    }

    @Override
    public Optional<SanPhamChiTiet> getById(Integer id) {
        return sanPhamChiTietRepository.findById(id);
    }



    @Override
    public SanPhamChiTiet save(SanPhamChiTiet sanPhamChiTiet) {
        return sanPhamChiTietRepository.save(sanPhamChiTiet);
    }

    @Override
    public void delete(Integer id) {
        if (sanPhamChiTietRepository.existsById(id)) {
            sanPhamChiTietRepository.deleteById(id);
        }
    }
    @Override
    public Page<SanPhamChiTiet> search(Pageable pageable,
                                       Integer idSanPham,
                                       Integer idKichThuoc,
                                       Integer idPhanLoai,
                                       Integer idXuatXu,
                                       Integer idChatLieu,
                                       Integer idMauSac,
                                       Integer idTheLoai,
                                       BigDecimal giaMin,
                                       BigDecimal giaMax,
                                       Integer trangThai) {

        // Chỉ cần gọi thẳng phương thức trong repository
        // vì logic "IS NULL" đã được xử lý trong JPQL
        return sanPhamChiTietRepository.search(
                pageable,
                idSanPham,
                idKichThuoc,
                idPhanLoai,
                idXuatXu,
                idChatLieu,
                idMauSac,
                idTheLoai,
                giaMin,
                giaMax,
                trangThai
        );
    }
}
