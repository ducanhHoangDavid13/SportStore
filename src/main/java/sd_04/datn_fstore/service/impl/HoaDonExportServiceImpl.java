package sd_04.datn_fstore.service.impl;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import sd_04.datn_fstore.model.HoaDon;
import sd_04.datn_fstore.model.HoaDonChiTiet;
import sd_04.datn_fstore.service.HoaDonExportService;
import sd_04.datn_fstore.service.HoaDonService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;

@Service
public class HoaDonExportServiceImpl implements HoaDonExportService {

    private final HoaDonService hoaDonService;

    // Khai báo Font
    private Font FONT_NORMAL;
    private Font FONT_BOLD;
    private Font FONT_HEADER;

    // Sử dụng Constructor Injection
    public HoaDonExportServiceImpl(HoaDonService hoaDonService) {
        this.hoaDonService = hoaDonService;
        try {
            // Thay thế "fonts/arial.ttf" bằng đường dẫn font của bạn nếu cần
            BaseFont baseFont = BaseFont.createFont("fonts/arial.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
            FONT_NORMAL = new Font(baseFont, 10, Font.NORMAL);
            FONT_BOLD = new Font(baseFont, 10, Font.BOLD);
            FONT_HEADER = new Font(baseFont, 14, Font.BOLD, BaseColor.BLUE);

        } catch (DocumentException | IOException e) {
            FONT_NORMAL = FontFactory.getFont(FontFactory.TIMES_ROMAN, 10, Font.NORMAL);
            FONT_BOLD = FontFactory.getFont(FontFactory.TIMES_ROMAN, 10, Font.BOLD);
            FONT_HEADER = FontFactory.getFont(FontFactory.TIMES_ROMAN, 14, Font.BOLD, BaseColor.BLUE);
            System.err.println("Lỗi tải Font Unicode, sử dụng Times New Roman thay thế.");
        }
    }

    @Override
    public byte[] exportHoaDon(Integer hoaDonId) {
        Optional<HoaDon> hoaDonOpt = hoaDonService.getById(hoaDonId);
        if (hoaDonOpt.isEmpty()) {
            throw new RuntimeException("Không tìm thấy Hóa đơn ID: " + hoaDonId);
        }
        HoaDon hoaDon = hoaDonOpt.get();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 20, 20, 20, 20);
            PdfWriter.getInstance(document, baos);
            document.open();

            // --- HEADER ---
            document.add(new Paragraph("HÓA ĐƠN BÁN LẺ F-STORE", FONT_HEADER));
            document.add(new Paragraph("Mã GD: #" + hoaDon.getMaHoaDon(), FONT_BOLD));
            document.add(new Paragraph("Ngày tạo: " + hoaDon.getNgayTao().toLocalDate(), FONT_NORMAL));
            document.add(Chunk.NEWLINE);

            // --- THÔNG TIN CHUNG ---
            document.add(createGeneralInfoTable(hoaDon));
            document.add(Chunk.NEWLINE);

            // --- BẢNG CHI TIẾT SẢN PHẨM ---
            document.add(createDetailTable(hoaDon));
            document.add(Chunk.NEWLINE);

            // --- TỔNG KẾT VÀ THANH TOÁN ---
            document.add(createSummaryTable(hoaDon));

            document.close();
            return baos.toByteArray();

        } catch (DocumentException | IOException e) {
            throw new RuntimeException("Lỗi khi tạo file PDF: " + e.getMessage(), e);
        }
    }

    // --- HELPER METHODS ---

    private PdfPTable createGeneralInfoTable(HoaDon hoaDon) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 1});
        table.addCell(createCell("Khách hàng: " + (hoaDon.getKhachHang() != null ? hoaDon.getKhachHang().getTenKhachHang() : "Khách lẻ"), FONT_NORMAL, 1, false));
        table.addCell(createCell("Nhân viên: " + hoaDon.getNhanVien().getTenNhanVien(), FONT_NORMAL, 1, false));
        return table;
    }

    private PdfPTable createDetailTable(HoaDon hoaDon) throws DocumentException {
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{4, 1.5f, 2, 2.5f});

        table.addCell(createCell("Tên Sản phẩm", FONT_BOLD, 1, true));
        table.addCell(createCell("SL", FONT_BOLD, 1, true));
        table.addCell(createCell("Đơn giá (VND)", FONT_BOLD, 1, true));
        table.addCell(createCell("Thành tiền (VND)", FONT_BOLD, 1, true));

        for (HoaDonChiTiet hdct : hoaDon.getHoaDonChiTiets()) {
                    String tenSp = hdct.getSanPhamChiTiet().getSanPham().getTenSanPham();
            table.addCell(createCell(tenSp, FONT_NORMAL, 1, false));
            table.addCell(createCell(String.valueOf(hdct.getSoLuong()), FONT_NORMAL, 1, false, Element.ALIGN_CENTER));
            table.addCell(createCell(formatCurrency(hdct.getDonGia()), FONT_NORMAL, 1, false, Element.ALIGN_RIGHT));
            table.addCell(createCell(formatCurrency(hdct.getThanhTien()), FONT_NORMAL, 1, false, Element.ALIGN_RIGHT));
        }

        return table;
    }

    private PdfPTable createSummaryTable(HoaDon hoaDon) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(50);
        table.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.setWidths(new float[]{1, 1.5f});

        table.addCell(createCell("Tổng tiền hàng:", FONT_NORMAL, 1, false, Element.ALIGN_LEFT));
        table.addCell(createCell(formatCurrency(hoaDon.getTongTien()), FONT_NORMAL, 1, false, Element.ALIGN_RIGHT));

        table.addCell(createCell("Chiết khấu:", FONT_NORMAL, 1, false, Element.ALIGN_LEFT));
        table.addCell(createCell("-" + formatCurrency(hoaDon.getTienGiamGia()), FONT_NORMAL, 1, false, Element.ALIGN_RIGHT));

        table.addCell(createCell("CẦN THANH TOÁN:", FONT_BOLD, 1, true, Element.ALIGN_LEFT));
        table.addCell(createCell(formatCurrency(hoaDon.getTongTienSauGiam()), FONT_BOLD, 1, true, Element.ALIGN_RIGHT, BaseColor.RED));

        return table;
    }

    // --- UTILITY METHODS ---

    private PdfPCell createCell(String content, Font font, int colspan, boolean isHeader) {
        return createCell(content, font, colspan, isHeader, Element.ALIGN_LEFT, BaseColor.BLACK);
    }

    private PdfPCell createCell(String content, Font font, int colspan, boolean isHeader, int alignment) {
        return createCell(content, font, colspan, isHeader, alignment, BaseColor.BLACK);
    }

    private PdfPCell createCell(String content, Font font, int colspan, boolean isHeader, int alignment, BaseColor color) {
        Phrase phrase = new Phrase(content, font);
        phrase.getFont().setColor(color);
        PdfPCell cell = new PdfPCell(phrase);
        cell.setColspan(colspan);
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5);
        if (isHeader) {
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        }
        return cell;
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "0";
        return String.format("%,.0f", amount.doubleValue());
    }
}