package sd_04.datn_fstore.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sd_04.datn_fstore.model.NhanVien;
import sd_04.datn_fstore.repository.NhanVienRepository;

import java.util.List;
import java.util.Optional;

@Service
public class NhanVienService {
    @Autowired
    private NhanVienRepository nvrp;

    public List<NhanVien> getAll() {
        return nvrp.findAll();
    }

    public Optional<NhanVien> getById(Integer id) {
        return nvrp.findById(id);
    }

    public NhanVien create(NhanVien nv) {
        return nvrp.save(nv);
    }

    public NhanVien update(Integer id, NhanVien nv) {
        if (nvrp.existsById(id)) {
            nv.setId(id);
            return nvrp.save(nv);
        }
        return null;
    }

    public void delete(Integer id) {
        nvrp.deleteById(id);
    }

    public List<NhanVien> search(String keyword, String vaiTro, Integer trangThai) {
        return nvrp.search(keyword, vaiTro, trangThai);
    }
}