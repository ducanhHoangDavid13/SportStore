package sd_04.datn_fstore.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import sd_04.datn_fstore.model.SanPham;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
}
