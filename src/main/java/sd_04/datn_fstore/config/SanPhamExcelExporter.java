//package sd_04.datn_fstore.config;
//import jakarta.servlet.ServletOutputStream;
//import jakarta.servlet.http.HttpServletResponse;
//import org.apache.poi.ss.usermodel.Cell;
//import org.apache.poi.ss.usermodel.CellStyle;
//import org.apache.poi.ss.usermodel.HorizontalAlignment;
//import org.apache.poi.ss.usermodel.Row;
//import org.apache.poi.xssf.usermodel.XSSFFont;
//import org.apache.poi.xssf.usermodel.XSSFSheet;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//import sd_04.datn_fstore.model.SanPham;
//
//import java.io.IOException;
//import java.text.SimpleDateFormat;
//import java.util.List;
//
//public class SanPhamExcelExporter {
//    private XSSFWorkbook workbook;
//    private XSSFSheet sheet;
//    private List<SanPham> listSanPhams;
//
//    public SanPhamExcelExporter(List<SanPham> listSanPhams) {
//        this.listSanPhams = listSanPhams;
//        workbook = new XSSFWorkbook();
//    }
//
//    // Tạo style cho header
//    private CellStyle createHeaderStyle() {
//        CellStyle style = workbook.createCellStyle();
//        XSSFFont font = workbook.createFont();
//        font.setBold(true);
//        font.setFontHeight(14);
//        style.setFont(font);
//        style.setAlignment(HorizontalAlignment.CENTER);
//        return style;
//    }
//
//    // Helper tạo cell
//    private void createCell(Row row, int columnCount, Object value, CellStyle style) {
//        sheet.autoSizeColumn(columnCount);
//        Cell cell = row.createCell(columnCount);
//        if (value instanceof Integer) {
//            cell.setCellValue((Integer) value);
//        } else if (value instanceof Double) {
//            cell.setCellValue((Double) value);
//        } else if (value instanceof Boolean) {
//            cell.setCellValue((Boolean) value);
//        } else if (value instanceof Long) {
//            cell.setCellValue((Long) value);
//        } else {
//            cell.setCellValue((String) value);
//        }
//        if (style != null) {
//            cell.setCellStyle(style);
//        }
//    }
//
//    // Viết dòng tiêu đề
//    private void writeHeaderLine() {
//        sheet = workbook.createSheet("San Pham");
//        Row row = sheet.createRow(0);
//        CellStyle style = createHeaderStyle();
//
//        // Tạo các ô tiêu đề
//        createCell(row, 0, "ID", style);
//        createCell(row, 1, "Mã Sản Phẩm", style);
//        createCell(row, 2, "Tên Sản Phẩm", style);
//        createCell(row, 3, "Giá Tiền", style);
//        createCell(row, 4, "Số Lượng", style);
//        createCell(row, 5, "Trạng Thái", style);
//        createCell(row, 6, "Ngày Tạo", style);
//        createCell(row, 7, "Mô Tả", style);
//    }
//
//    // Viết dữ liệu
//    private void writeDataLines() {
//        int rowCount = 1; // Bắt đầu từ dòng 1 (dòng 0 là header)
//        CellStyle style = workbook.createCellStyle(); // Style cơ bản
//        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
//
//        for (SanPham sp : listSanPhams) {
//            Row row = sheet.createRow(rowCount++);
//            int columnCount = 0;
//
//            createCell(row, columnCount++, sp.getId(), style);
//            createCell(row, columnCount++, sp.getMaSanPham() != null ? sp.getMaSanPham() : "", style);
//            createCell(row, columnCount++, sp.getTenSanPham() != null ? sp.getTenSanPham() : "", style);
//            createCell(row, columnCount++, sp.getGiaTien() != null ? sp.getGiaTien() : 0.0, style);
//            createCell(row, columnCount++, sp.getSoLuong() != null ? sp.getSoLuong() : 0, style);
//
//            // Xử lý trạng thái
//            String trangThai = (sp.getTrangThai() != null && sp.getTrangThai() == 1) ? "Đang bán" : "Ngừng bán";
//            createCell(row, columnCount++, trangThai, style);
//
//            // Xử lý ngày tạo (nếu có)
//            String ngayTao = (sp.getNgayTao() != null) ? sdf.format(sp.getNgayTao()) : "";
//            createCell(row, columnCount++, ngayTao, style);
//
//            createCell(row, columnCount++, sp.getMoTa() != null ? sp.getMoTa() : "", style);
//        }
//    }
//
//    // Phương thức chính để xuất file
//    public void export(HttpServletResponse response) throws IOException {
//        writeHeaderLine(); // Tạo header
//        writeDataLines();  // Ghi dữ liệu
//
//        ServletOutputStream outputStream = response.getOutputStream();
//        workbook.write(outputStream); // Ghi workbook vào output stream
//        workbook.close();
//        outputStream.close();
//    }
//}