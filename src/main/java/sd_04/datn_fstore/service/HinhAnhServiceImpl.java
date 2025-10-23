package sd_04.datn_fstore.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sd_04.datn_fstore.model.HinhAnh;
import sd_04.datn_fstore.model.SanPham;
import sd_04.datn_fstore.repository.HinhAnhRepository;

 //import sd_04.datn_fstore.service.FileStorageService;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class HinhAnhServiceImpl implements HinhAnhService {

    @Autowired
    private HinhAnhRepository hinhAnhRepo;

    // @Autowired
    // private FileStorageService fileStorageService; // Bạn CẦN service này để xóa file

    @Override
    public List<HinhAnh> getBySanPhamId(Integer sanPhamId) {
        return hinhAnhRepo.findAllBySanPhamId(sanPhamId);
    }

    @Override
    public Optional<HinhAnh> getAvatar(Integer sanPhamId) {
        return hinhAnhRepo.findAvatarBySanPhamId(sanPhamId);
    }

    @Override
    public Optional<HinhAnh> findById(Integer id) {
        return hinhAnhRepo.findById(id);
    }

    @Override
    @Transactional
    public HinhAnh save(HinhAnh hinhAnh) {
        // Logic nghiệp vụ:
        // Giả định: Controller đã gọi FileStorageService.save(file)
        // và gán hinhAnh.setTenHinhAnh("ten_file_da_luu.jpg")

        if (hinhAnh.getId() == null) {
            hinhAnh.setNgayTao(new Date()); // Gán ngày tạo nếu là ảnh mới
        }
        hinhAnh.setNgaySua(new Date()); // Luôn cập nhật ngày sửa

        return hinhAnhRepo.save(hinhAnh);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        // Tìm ảnh trong CSDL
        Optional<HinhAnh> opt = hinhAnhRepo.findById(id);
        if (opt.isPresent()) {
            HinhAnh hinhAnh = opt.get();
            String tenHinhAnh = hinhAnh.getTenHinhAnh(); // Lấy tên file

            // --- Logic nghiệp vụ quan trọng ---
            // 1. Xóa file vật lý (ví dụ: "abc.jpg") khỏi server
            // try {
            //     fileStorageService.delete(tenHinhAnh);
            // } catch (Exception e) {
            //     // Ghi log lỗi xóa file
            //     System.err.println("Lỗi khi xóa file vật lý: " + tenHinhAnh);
            // }

            // 2. Xóa record trong CSDL
            hinhAnhRepo.delete(hinhAnh);
        }
    }

    @Override
    @Transactional
    public void deleteBySanPham(SanPham sanPham) {
        // 1. Lấy danh sách tất cả ảnh của sản phẩm
        List<HinhAnh> hinhAnhs = hinhAnhRepo.findBySanPham(sanPham);

        // 2. Lặp qua và xóa từng file vật lý
        // for (HinhAnh img : hinhAnhs) {
        //     try {
        //         fileStorageService.delete(img.getTenHinhAnh());
        //     } catch (Exception e) {
        //         System.err.println("Lỗi khi xóa file vật lý: " + img.getTenHinhAnh());
        //     }
        // }

        // 3. Xóa tất cả record trong CSDL (nhanh hơn là xóa từng cái)
        hinhAnhRepo.deleteAll(hinhAnhs);
    }
}
