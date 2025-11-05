package sd_04.datn_fstore.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sd_04.datn_fstore.model.NhanVien;
import sd_04.datn_fstore.repository.NhanVienRepository;

// 1. Thêm import cho Page và Pageable
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

// import java.util.List; // (Không cần dùng List cho getAll nữa)
import java.util.List;
import java.util.Optional;

@Service
public class NhanVienService {
    @Autowired
    private NhanVienRepository nvrp;

    // 2. XÓA HÀM getAll()
    // Hàm này không hiệu quả, hãy dùng search() thay thế
    // public List<NhanVien> getAll() {
    //    return nvrp.findAll();
    // }

    public Optional<NhanVien> getById(Integer id) {
        return nvrp.findById(id);
    }
    public List<NhanVien> getAll() {
        return nvrp.findAll();
    }
    public NhanVien create(NhanVien nv) {
        return nvrp.save(nv);
    }
    public Optional<NhanVien> findByEmail(String email) {
        // Gọi hàm mới mà bạn vừa thêm vào Repository
        return nvrp.findByEmail(email);
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

    // 3. SỬA LẠI HOÀN TOÀN HÀM SEARCH
    // Phải nhận Pageable và trả về Page
    public Page<NhanVien> search(Pageable pageable, String keyword, String vaiTro, Integer trangThai) {
        // Gửi Pageable (tham số đầu tiên) vào Repository
        return nvrp.search(pageable, keyword, vaiTro, trangThai);
    }
}