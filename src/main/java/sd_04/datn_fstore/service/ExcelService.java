package sd_04.datn_fstore.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import sd_04.datn_fstore.model.SanPham;
import sd_04.datn_fstore.model.SanPhamChiTiet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@Service
public class ExcelService {

    public ByteArrayInputStream exportSanPhamToExcel(List<SanPham> listSanPham) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Danh sách Sản phẩm");

            // Header
            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "Mã SP", "Tên Sản Phẩm", "Giá Bán", "Số Lượng", "Trạng Thái"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                // Style cho header (In đậm)
                CellStyle style = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                style.setFont(font);
                cell.setCellStyle(style);
            }

            // Data
            int rowIdx = 1;
            for (SanPham sp : listSanPham) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(sp.getId());
                row.createCell(1).setCellValue(sp.getMaSanPham());
                row.createCell(2).setCellValue(sp.getTenSanPham());
                row.createCell(3).setCellValue(sp.getGiaTien().doubleValue());
                row.createCell(4).setCellValue(sp.getSoLuong());
                row.createCell(5).setCellValue(sp.getTrangThai() == 1 ? "Đang bán" : "Ngừng bán");
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Lỗi tạo file Excel: " + e.getMessage());
        }
    }
    public ByteArrayInputStream exportSanPhamChiTietToExcel(List<SanPhamChiTiet> listSpct) {
        String[] columns = {
                "ID", "Mã SPCT", "Mã SP Gốc", "Tên SP Gốc", "Màu Sắc",
                "Kích Thước", "Chất Liệu", "Phân Loại", "Thể Loại",
                "Giá Bán", "Tồn Kho", "Trạng Thái"
        };

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Danh sách Biến thể");

            // 1. Tạo CellStyle cho Header
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // 2. Tạo CellStyle cho Giá tiền (Format tiền tệ VNĐ)
            CellStyle currencyStyle = workbook.createCellStyle();
            DataFormat dataFormat = workbook.createDataFormat();
            // Định dạng tiền tệ: #,##0 ₫
            currencyStyle.setDataFormat(dataFormat.getFormat("#,##0 \\₫"));

            // 3. Header Row
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // 4. Data Rows
            int rowIdx = 1;
            for (SanPhamChiTiet spct : listSpct) {
                Row row = sheet.createRow(rowIdx++);

                // Cột 0: ID
                row.createCell(0).setCellValue(spct.getId());

                // Cột 1: Mã SPCT
                row.createCell(1).setCellValue(spct.getMaSanPhamChiTiet());

                // Cột 2 & 3: Sản phẩm gốc (Cần kiểm tra null để tránh lỗi)
                row.createCell(2).setCellValue(spct.getSanPham() != null ? spct.getSanPham().getMaSanPham() : "N/A");
                row.createCell(3).setCellValue(spct.getSanPham() != null ? spct.getSanPham().getTenSanPham() : "N/A");

                // Cột 4-8: Các thuộc tính khác (Kiểm tra null)
                row.createCell(4).setCellValue(spct.getMauSac() != null ? spct.getMauSac().getTenMauSac() : "N/A");
                row.createCell(5).setCellValue(spct.getKichThuoc() != null ? spct.getKichThuoc().getTenKichThuoc() : "N/A");
                row.createCell(6).setCellValue(spct.getChatLieu() != null ? spct.getChatLieu().getLoaiChatLieu() : "N/A");
                row.createCell(7).setCellValue(spct.getPhanLoai() != null ? spct.getPhanLoai().getPhanLoai() : "N/A");
                row.createCell(8).setCellValue(spct.getTheLoai() != null ? spct.getTheLoai().getTenTheLoai() : "N/A");

                // Cột 9: Giá Bán (Áp dụng format tiền tệ)
                Cell giaBanCell = row.createCell(9);
                BigDecimal giaTien = spct.getGiaTien() != null ? spct.getGiaTien() : BigDecimal.ZERO;
                giaBanCell.setCellValue(giaTien.doubleValue());
                giaBanCell.setCellStyle(currencyStyle); // Áp dụng style tiền tệ

                // Cột 10 & 11: Số lượng và Trạng thái
                row.createCell(10).setCellValue(spct.getSoLuong());
                row.createCell(11).setCellValue(spct.getTrangThai() == 1 ? "Đang bán" : "Ngừng bán");
            }

            // Tùy chọn: Tự động điều chỉnh độ rộng cột
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Lỗi tạo file Excel cho Biến thể: " + e.getMessage());
        }
    }
}
