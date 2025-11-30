package sd_04.datn_fstore.service;

public interface HoaDonExportService {

    /**
     * Xuất Hóa đơn thành file PDF.
     * @param hoaDonId ID của Hóa đơn cần xuất.
     * @return Mảng byte đại diện cho nội dung file PDF.
     */
    byte[] exportHoaDon(Integer hoaDonId);
}