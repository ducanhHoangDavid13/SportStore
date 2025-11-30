package sd_04.datn_fstore.service.impl;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import org.springframework.stereotype.Service;
import sd_04.datn_fstore.model.HoaDon;
import sd_04.datn_fstore.model.HoaDonChiTiet;
import sd_04.datn_fstore.service.HoaDonExportService;
import sd_04.datn_fstore.service.HoaDonService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

@Service
public class HoaDonExportServiceImpl implements HoaDonExportService {

    private final HoaDonService hoaDonService;

    // --- MÀU SẮC CHỦ ĐẠO (Xanh F-Store) ---
    private static final BaseColor THEME_COLOR = new BaseColor(41, 128, 185);
    private static final BaseColor HEADER_BG = new BaseColor(240, 242, 245);

    // --- FONT CHỮ ---
    private BaseFont baseFont;
    private Font fontTitle;
    private Font fontHeader;
    private Font fontBold;
    private Font fontNormal;
    private Font fontItalic;

    public HoaDonExportServiceImpl(HoaDonService hoaDonService) {
        this.hoaDonService = hoaDonService;
        try {
            // ĐƯỜNG DẪN FONT: Phải trỏ đúng file .ttf hỗ trợ tiếng Việt trong resources
            // Bạn cần copy file 'times.ttf' vào thư mục src/main/resources/fonts/
            String fontPath = "src/main/resources/fonts/times.ttf";

            baseFont = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

            // Khởi tạo các kiểu font
            fontTitle = new Font(baseFont, 20, Font.BOLD, THEME_COLOR);
            fontHeader = new Font(baseFont, 12, Font.BOLD, BaseColor.WHITE); // Chữ trắng trên nền xanh
            fontBold = new Font(baseFont, 11, Font.BOLD, BaseColor.BLACK);
            fontNormal = new Font(baseFont, 11, Font.NORMAL, BaseColor.BLACK);
            fontItalic = new Font(baseFont, 10, Font.ITALIC, BaseColor.GRAY);

        } catch (DocumentException | IOException e) {
            e.printStackTrace();
            // Fallback nếu không thấy font (Sẽ lỗi tiếng Việt)
            fontNormal = new Font(Font.FontFamily.TIMES_ROMAN, 12);
        }
    }

    @Override
    public byte[] exportHoaDon(Integer hoaDonId) {
        Optional<HoaDon> hoaDonOpt = hoaDonService.getById(hoaDonId);
        if (hoaDonOpt.isEmpty()) {
            throw new RuntimeException("Không tìm thấy hóa đơn: " + hoaDonId);
        }
        HoaDon hoaDon = hoaDonOpt.get();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // Cấu hình trang A4, lề 30
            Document document = new Document(PageSize.A4, 30, 30, 30, 30);
            PdfWriter writer = PdfWriter.getInstance(document, baos);

            // Vẽ khung viền trang cho đẹp
            writer.setPageEvent(new PdfBorderEvent());

            document.open();

            // 1. Header (Logo & Thông tin cửa hàng)
            addHeader(document, hoaDon);

            document.add(new Paragraph("\n"));

            // 2. Thông tin Khách hàng
            addCustomerInfo(document, hoaDon);

            document.add(new Paragraph("\n"));

            // 3. Bảng sản phẩm
            addOrderDetails(document, hoaDon);

            // 4. Tổng tiền (Đã bỏ phí ship)
            addSummary(document, hoaDon);

            // 5. Footer
            addFooter(document);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Lỗi xuất PDF: " + e.getMessage());
        }
    }

    // --- CÁC HÀM DỰNG GIAO DIỆN ---

    private void addHeader(Document document, HoaDon hoaDon) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.2f, 0.8f});

        // Cột trái: Thông tin cửa hàng
        PdfPCell cellLeft = new PdfPCell();
        cellLeft.setBorder(Rectangle.NO_BORDER);
        cellLeft.addElement(new Paragraph("F-STORE FASHION", fontTitle));
        cellLeft.addElement(new Paragraph("Địa chỉ: 123 Trịnh Văn Bô, Hà Nội", fontNormal));
        cellLeft.addElement(new Paragraph("Hotline: 0988.888.999", fontNormal));
        table.addCell(cellLeft);

        // Cột phải: Thông tin hóa đơn
        PdfPCell cellRight = new PdfPCell();
        cellRight.setBorder(Rectangle.NO_BORDER);
        cellRight.setHorizontalAlignment(Element.ALIGN_RIGHT);

        Paragraph title = new Paragraph("HÓA ĐƠN BÁN HÀNG", new Font(baseFont, 16, Font.BOLD));
        title.setAlignment(Element.ALIGN_RIGHT);
        cellRight.addElement(title);

        Paragraph code = new Paragraph("#" + hoaDon.getMaHoaDon(), new Font(baseFont, 12, Font.BOLD, BaseColor.RED));
        code.setAlignment(Element.ALIGN_RIGHT);
        cellRight.addElement(code);

        String ngayTao = hoaDon.getNgayTao() != null ?
                hoaDon.getNgayTao().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "";
        Paragraph date = new Paragraph("Ngày: " + ngayTao, fontItalic);
        date.setAlignment(Element.ALIGN_RIGHT);
        cellRight.addElement(date);

        table.addCell(cellRight);
        document.add(table);

        // Kẻ đường gạch ngang phân cách
        LineSeparator ls = new LineSeparator();
        ls.setLineColor(new BaseColor(200, 200, 200));
        document.add(new Chunk(ls));
    }

    private void addCustomerInfo(Document document, HoaDon hoaDon) throws DocumentException {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);

        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPaddingTop(10);

        // Lấy thông tin khách hàng (null check)
        String tenKH = hoaDon.getKhachHang() != null ? hoaDon.getKhachHang().getTenKhachHang() : "Khách lẻ";
        String sdt = (hoaDon.getKhachHang() != null && hoaDon.getKhachHang().getSoDienThoai() != null)
                ? hoaDon.getKhachHang().getSoDienThoai() : "";

        // Nếu hóa đơn có địa chỉ giao hàng thì hiển thị, không thì mặc định
        // (Tùy model của bạn có field 'diaChi' trong HoaDon hay không, nếu không có thì bỏ dòng này)
        // String diaChi = hoaDon.getDiaChi() != null ? hoaDon.getDiaChi() : "Mua tại quầy";

        cell.addElement(new Paragraph("Khách hàng: " + tenKH, fontBold));
        if(!sdt.isEmpty()) {
            cell.addElement(new Paragraph("Số điện thoại: " + sdt, fontNormal));
        }
        // cell.addElement(new Paragraph("Địa chỉ: " + diaChi, fontNormal));

        table.addCell(cell);
        document.add(table);
    }

    private void addOrderDetails(Document document, HoaDon hoaDon) throws DocumentException {
        // Tạo bảng 5 cột: STT, Tên SP, SL, Đơn giá, Thành tiền
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{0.5f, 4f, 0.8f, 1.5f, 1.8f});
        table.setSpacingBefore(10);

        // --- Header Bảng ---
        String[] headers = {"STT", "Sản Phẩm", "SL", "Đơn Giá", "Thành Tiền"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, fontHeader));
            cell.setBackgroundColor(THEME_COLOR); // Nền xanh
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPadding(6);
            cell.setBorderColor(BaseColor.WHITE);
            table.addCell(cell);
        }

        // --- Dữ liệu Bảng ---
        int i = 1;
        for (HoaDonChiTiet item : hoaDon.getHoaDonChiTiets()) {
            // STT
            addCell(table, String.valueOf(i++), Element.ALIGN_CENTER, false);

            // Tên SP + Thuộc tính (Màu/Size)
            String tenSP = item.getSanPhamChiTiet().getSanPham().getTenSanPham();
            String thuocTinh = item.getSanPhamChiTiet().getMauSac().getTenMauSac() + " / " +
                    item.getSanPhamChiTiet().getKichThuoc().getTenKichThuoc();

            PdfPCell nameCell = new PdfPCell();
            nameCell.setPadding(5);
            nameCell.addElement(new Paragraph(tenSP, fontNormal));
            nameCell.addElement(new Paragraph(thuocTinh, new Font(baseFont, 9, Font.ITALIC, BaseColor.GRAY)));
            table.addCell(nameCell);

            // Số lượng
            addCell(table, String.valueOf(item.getSoLuong()), Element.ALIGN_CENTER, false);

            // Đơn giá
            addCell(table, formatMoney(item.getDonGia()), Element.ALIGN_RIGHT, false);

            // Thành tiền
            addCell(table, formatMoney(item.getThanhTien()), Element.ALIGN_RIGHT, true);
        }
        document.add(table);
    }

    private void addSummary(Document document, HoaDon hoaDon) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(40); // Chiếm 40% chiều rộng trang (góc phải)
        table.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.setSpacingBefore(10);
        table.setWidths(new float[]{1.5f, 1.5f});

        // Tổng tiền hàng
        addSummaryRow(table, "Tổng tiền:", formatMoney(hoaDon.getTongTien()), false);

        // Giảm giá (Chỉ hiện nếu có giảm giá)
        if (hoaDon.getTienGiamGia() != null && hoaDon.getTienGiamGia().compareTo(BigDecimal.ZERO) > 0) {
            addSummaryRow(table, "Giảm giá:", "-" + formatMoney(hoaDon.getTienGiamGia()), false);
        }

        // --- ĐÃ BỎ PHẦN PHÍ SHIP THEO YÊU CẦU ---

        // TỔNG THANH TOÁN (In đậm, chữ to, màu đỏ)
        PdfPCell labelTotal = new PdfPCell(new Phrase("THANH TOÁN:", new Font(baseFont, 12, Font.BOLD, THEME_COLOR)));
        labelTotal.setBorder(Rectangle.TOP);
        labelTotal.setPaddingTop(5);
        table.addCell(labelTotal);

        PdfPCell valTotal = new PdfPCell(new Phrase(formatMoney(hoaDon.getTongTienSauGiam()), new Font(baseFont, 13, Font.BOLD, BaseColor.RED)));
        valTotal.setBorder(Rectangle.TOP);
        valTotal.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valTotal.setPaddingTop(5);
        table.addCell(valTotal);

        document.add(table);
    }

    private void addFooter(Document document) throws DocumentException {
        document.add(new Paragraph("\n\n"));

        Paragraph p1 = new Paragraph("Cảm ơn quý khách đã mua hàng!", fontItalic);
        p1.setAlignment(Element.ALIGN_CENTER);
        document.add(p1);

        Paragraph p2 = new Paragraph("Hẹn gặp lại quý khách lần sau.", fontItalic);
        p2.setAlignment(Element.ALIGN_CENTER);
        document.add(p2);
    }

    // --- CÁC HÀM PHỤ TRỢ ---

    private void addCell(PdfPTable table, String text, int align, boolean bold) {
        PdfPCell cell = new PdfPCell(new Phrase(text, bold ? fontBold : fontNormal));
        cell.setHorizontalAlignment(align);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(6);
        table.addCell(cell);
    }

    private void addSummaryRow(PdfPTable table, String label, String value, boolean bold) {
        PdfPCell c1 = new PdfPCell(new Phrase(label, fontNormal));
        c1.setBorder(Rectangle.NO_BORDER);
        table.addCell(c1);

        PdfPCell c2 = new PdfPCell(new Phrase(value, bold ? fontBold : fontNormal));
        c2.setBorder(Rectangle.NO_BORDER);
        c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(c2);
    }

    private String formatMoney(BigDecimal money) {
        if (money == null) return "0 đ";
        return NumberFormat.getInstance(new Locale("vi", "VN")).format(money) + " đ";
    }

    // Class vẽ khung viền trang (Trang trí thêm cho đẹp)
    class PdfBorderEvent extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            cb.setColorStroke(THEME_COLOR);
            cb.setLineWidth(1);
            // Vẽ hình chữ nhật cách lề 15 đơn vị
            cb.rectangle(15, 15, document.getPageSize().getWidth() - 30, document.getPageSize().getHeight() - 30);
            cb.stroke();
        }
    }
}