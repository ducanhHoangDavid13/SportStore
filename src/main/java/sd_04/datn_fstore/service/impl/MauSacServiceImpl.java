package sd_04.datn_fstore.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest; // Dùng cho Pageable.unpaged() hoặc tạo Pageable nếu cần
import org.springframework.stereotype.Service;
import sd_04.datn_fstore.model.MauSac;
import sd_04.datn_fstore.repository.MauSacRepository;
import sd_04.datn_fstore.service.MauSacService;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MauSacServiceImpl implements MauSacService {

    private final MauSacRepository mauSacRepository;

    // --- CRUD CƠ BẢN ---
    @Override public List<MauSac> getAll() { return mauSacRepository.findAll(); }
    @Override public Optional<MauSac> getById(Integer id) { return mauSacRepository.findById(id); }
    @Override public MauSac save(MauSac mauSac) { return mauSacRepository.save(mauSac); }
    @Override public void delete(Integer id) { mauSacRepository.deleteById(id); }


    // --- 1. PHÂN TRANG, TÌM KIẾM & LỌC (Dùng cho Controller View) ---
    @Override
    public Page<MauSac> searchAndPaginate(Pageable pageable, String keyword, Integer trangThai) {
        // Gọi trực tiếp phương thức @Query đã định nghĩa trong Repository
        return mauSacRepository.findPaginated(pageable, keyword, trangThai);
    }

    // --- 2. CÁC PHƯƠNG THỨC KHÔNG PHÂN TRANG (Dùng cho API/Logic khác) ---

    private Pageable unpaged = PageRequest.of(0, 2000); // Giới hạn kích thước lớn nếu không phân trang

    @Override
    public List<MauSac> searchByTen(String ten) {
        // Gọi findPaginated, chỉ truyền tên, và lấy nội dung (List)
        return mauSacRepository.findPaginated(unpaged, ten, null).getContent();
    }

    @Override
    public List<MauSac> filterByTrangThai(Integer trangThai) {
        // Gọi findPaginated, chỉ truyền trạng thái, và lấy nội dung (List)
        return mauSacRepository.findPaginated(unpaged, null, trangThai).getContent();
    }
    @Override
    public MauSac updateTrangThai(Integer id, Integer newStatus) {
        Optional<MauSac> optional = mauSacRepository.findById(id);
        if (optional.isPresent()) {
            MauSac mauSac = optional.get();
            mauSac.setTrangThai(newStatus);
            return mauSacRepository.save(mauSac);
        } else {
            throw new RuntimeException("Không tìm thấy màu sắc có ID: " + id);
        }
    }
    @Override
    public List<MauSac> searchAndFilter(String ten, Integer trangThai) {
        // Gọi findPaginated, truyền cả hai tham số, và lấy nội dung (List)
        return mauSacRepository.findPaginated(unpaged, ten, trangThai).getContent();
    }

    @Override
    public Optional<MauSac> findByMaMau(String maMau) {
        // SỬA LỖI GÕ: Gọi phương thức chính xác đã khai báo trong Repository
        return Optional.ofNullable(mauSacRepository.findByMaMau(maMau));
    }
}