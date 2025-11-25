package sd_04.datn_fstore.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sd_04.datn_fstore.model.DiaChi;
import sd_04.datn_fstore.model.KhachHang;
import sd_04.datn_fstore.repository.DiaChiRepo;
import sd_04.datn_fstore.repository.KhachHangRepo;
import sd_04.datn_fstore.service.DiaChiService;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor // Tự động tạo constructor cho các trường final (Dependency Injection)
public class DiaChiServiceImpl implements DiaChiService {

    // Inject các repository cần thiết
    private final DiaChiRepo diaChiRepository;
    private final KhachHangRepo khachHangRepository;

    @Override
    @Transactional(readOnly = true) // Chỉ đọc, giúp tối ưu hiệu suất
    public List<DiaChi> getDiaChiByKhachHangId(Integer khachHangId) {
        // Gọi phương thức từ repository để lấy các địa chỉ có trạng thái là 1 (Hoạt động)
        return diaChiRepository.findByKhachhang_IdAndTrangThaiOrderByIdDesc(khachHangId, 1);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DiaChi> getById(Integer diaChiId) {
        return diaChiRepository.findById(diaChiId);
    }

    @Override
    @Transactional // Giao dịch có ghi dữ liệu
    public DiaChi saveNewAddress(DiaChi diaChi, Integer idKhachHang) {
        // 1. Tìm khách hàng, nếu không có sẽ báo lỗi
        KhachHang khachHang = khachHangRepository.findById(idKhachHang)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khách hàng với ID: " + idKhachHang));

        // 2. Gán khách hàng và trạng thái mặc định cho địa chỉ mới
        diaChi.setKhachhang(khachHang);
        diaChi.setTrangThai(1); // 1 = Hoạt động

        // 3. Lưu vào cơ sở dữ liệu và trả về đối tượng đã lưu
        return diaChiRepository.save(diaChi);
    }

    @Override
    @Transactional
    public DiaChi updateAddress(Integer diaChiId, DiaChi updatedDiaChiDetails) {
        // 1. Tìm địa chỉ hiện có, nếu không có sẽ báo lỗi
        DiaChi existingDiaChi = diaChiRepository.findById(diaChiId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy địa chỉ với ID: " + diaChiId));

        // 2. Cập nhật các trường thông tin từ đối tượng được truyền vào
        existingDiaChi.setHoTen(updatedDiaChiDetails.getHoTen());
        existingDiaChi.setSoDienThoai(updatedDiaChiDetails.getSoDienThoai());
        existingDiaChi.setDiaChiCuThe(updatedDiaChiDetails.getDiaChiCuThe());
        existingDiaChi.setXa(updatedDiaChiDetails.getXa());
        existingDiaChi.setThanhPho(updatedDiaChiDetails.getThanhPho());
        existingDiaChi.setLoaiDiaChi(updatedDiaChiDetails.getLoaiDiaChi());
        existingDiaChi.setGhiChu(updatedDiaChiDetails.getGhiChu());

        // 3. Lưu lại thay đổi và trả về đối tượng đã cập nhật
        return diaChiRepository.save(existingDiaChi);
    }

    @Override
    @Transactional
    public void deleteAddress(Integer diaChiId) {
        // 1. Tìm địa chỉ, nếu không có sẽ báo lỗi
        DiaChi diaChi = diaChiRepository.findById(diaChiId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy địa chỉ với ID: " + diaChiId));

        // 2. Thực hiện xóa mềm bằng cách thay đổi trạng thái
        diaChi.setTrangThai(0); // 0 = Ngừng hoạt động

        // 3. Lưu lại thay đổi
        diaChiRepository.save(diaChi);
    }
}