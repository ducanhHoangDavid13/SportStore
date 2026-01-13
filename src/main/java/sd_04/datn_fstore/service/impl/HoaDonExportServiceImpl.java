package sd_04.datn_fstore.service.impl;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import org.springframework.stereotype.Service;
import sd_04.datn_fstore.model.DiaChi;
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

    // --- MÀU SẮC CHỦ ĐẠO (Đen/Xám/Đỏ như trên mẫu) ---
    private static final BaseColor TEXT_COLOR = new BaseColor(50, 50, 50); // Màu chữ đen đậm
    private static final BaseColor HEADER_COLOR = new BaseColor(30, 30, 30); // Màu header
    private static final BaseColor TOTAL_COLOR = new BaseColor(220, 50, 50); // Màu Đỏ cho Total

    // --- FONT CHỮ ---
    private BaseFont baseFont;
    private Font fontTitle;
    private Font fontHeader;
    private Font fontBold;
    private Font fontNormal;
    private Font fontSmall;
    private Font fontRedBold;

    public HoaDonExportServiceImpl(HoaDonService hoaDonService) {
        this.hoaDonService = hoaDonService;
        try {
            // ĐƯỜNG DẪN FONT: Phải trỏ đúng file .ttf hỗ trợ tiếng Việt trong resources
            // Sử dụng font Times New Roman hoặc tương đương để hỗ trợ tiếng Việt.
            String fontPath = "src/main/resources/fonts/times.ttf"; // HOẶC "fonts/times.ttf"

            baseFont = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

            // Khởi tạo các kiểu font
            fontTitle = new Font(baseFont, 16, Font.BOLD, HEADER_COLOR); // FPERFUME
            fontHeader = new Font(baseFont, 10, Font.BOLD, HEADER_COLOR); // Tiêu đề bảng
            fontBold = new Font(baseFont, 11, Font.BOLD, TEXT_COLOR);
            fontNormal = new Font(baseFont, 11, Font.NORMAL, TEXT_COLOR);
            fontSmall = new Font(baseFont, 9, Font.NORMAL, BaseColor.GRAY); // SKU, Mô tả
            fontRedBold = new Font(baseFont, 12, Font.BOLD, TOTAL_COLOR); // Tổng tiền cuối

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
            PdfWriter.getInstance(document, baos);

            document.open();

            // 1. Tiêu đề (FPERFUME & Ngày mua)
            addMainHeader(document, hoaDon);

            // Kẻ đường gạch ngang mỏng
            LineSeparator ls = new LineSeparator(0.5f, 100, BaseColor.LIGHT_GRAY, Element.ALIGN_CENTER, -2);
            document.add(new Chunk(ls));
            document.add(new Paragraph("\n"));

            // 2. Thông tin Khách hàng & Nhận hàng
            addCustomerAndShippingInfo(document, hoaDon);

            document.add(new Paragraph("\n"));

            // 3. Bảng sản phẩm
            addOrderDetails(document, hoaDon);

            // 4. Tổng tiền
            addSummary(document, hoaDon);

            // 5. Footer (Đã bỏ, vì mẫu không có)

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Lỗi xuất PDF: " + e.getMessage());
        }
    }

    // --- CÁC HÀM DỰNG GIAO DIỆN ---

    private void addMainHeader(Document document, HoaDon hoaDon) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.5f, 1.5f});
        table.setSpacingAfter(5);

        // Cột trái: Tên cửa hàng và Mã đơn hàng
        PdfPCell cellLeft = new PdfPCell();
        cellLeft.setBorder(Rectangle.NO_BORDER);
        cellLeft.addElement(new Paragraph("SportX-Fstore", fontTitle));
        cellLeft.addElement(new Paragraph("Hóa đơn mua hàng", fontSmall));
        cellLeft.addElement(new Paragraph("MÃ ĐƠN HÀNG", fontSmall));
        cellLeft.addElement(new Paragraph("#" + hoaDon.getMaHoaDon(), fontBold));

        // Nơi mua (Giả định trạng thái)
        String trangThaiHD = (hoaDon.getHinhThucBanHang() == 1) ? "Bán hàng tại Quầy" : "Bán hàng Online";
        cellLeft.addElement(new Paragraph("Đơn hàng " + trangThaiHD, fontSmall));

        table.addCell(cellLeft);

        // Cột phải: Ngày mua hàng
        PdfPCell cellRight = new PdfPCell();
        cellRight.setBorder(Rectangle.NO_BORDER);
        cellRight.setHorizontalAlignment(Element.ALIGN_RIGHT);

        String ngayTao = hoaDon.getNgayTao() != null ?
                hoaDon.getNgayTao().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "";

        Paragraph dateLabel = new Paragraph("NGÀY MUA HÀNG", fontSmall);
        dateLabel.setAlignment(Element.ALIGN_RIGHT);
        cellRight.addElement(dateLabel);

        Paragraph dateValue = new Paragraph(ngayTao, fontBold);
        dateValue.setAlignment(Element.ALIGN_RIGHT);
        cellRight.addElement(dateValue);

        table.addCell(cellRight);
        document.add(table);
    }

    private void addCustomerAndShippingInfo(Document document, HoaDon hoaDon) throws DocumentException {
        PdfPTable mainTable = new PdfPTable(2);
        mainTable.setWidthPercentage(100);
        mainTable.setWidths(new float[]{1.5f, 1.5f});

        // --- Cột trái: Thông tin Khách hàng ---
        PdfPCell cellCustomer = new PdfPCell();
        cellCustomer.setBorder(Rectangle.NO_BORDER);
        cellCustomer.addElement(new Paragraph("KHÁCH HÀNG", fontHeader));

        // Tên KH
        String tenKH = hoaDon.getKhachHang() != null ? hoaDon.getKhachHang().getTenKhachHang() : "Khách mua tại quầy";
        cellCustomer.addElement(new Paragraph(tenKH, fontNormal));

        // Trạng thái mua
        cellCustomer.addElement(new Paragraph("Khách mua tại quầy - Mua trực tiếp tại quầy", fontSmall));

        // SDT
        String sdt = (hoaDon.getKhachHang() != null && hoaDon.getKhachHang().getSoDienThoai() != null)
                ? hoaDon.getKhachHang().getSoDienThoai() : "";
        cellCustomer.addElement(new Paragraph("SĐT: " + sdt, fontSmall));

        mainTable.addCell(cellCustomer);

        // --- Cột phải: Thông tin Nhận hàng (Chỉ hiển thị nếu là đơn Online) ---
        PdfPCell cellShipping = new PdfPCell();
        cellShipping.setBorder(Rectangle.NO_BORDER);

        if (hoaDon.getHinhThucBanHang() == 0 && hoaDon.getDiaChiGiaoHang() != null) { // Đơn online
            DiaChi dc = hoaDon.getDiaChiGiaoHang();

            cellShipping.addElement(new Paragraph("THÔNG TIN NHẬN HÀNG", fontHeader));
            cellShipping.addElement(new Paragraph("Người nhận: " + dc.getHoTen(), fontNormal));
            cellShipping.addElement(new Paragraph("SĐT: " + dc.getSoDienThoai(), fontNormal));

            // Địa chỉ chi tiết
            String fullAddress = dc.getDiaChiCuThe() + ", " + dc.getXa() + ", " + dc.getThanhPho();
            cellShipping.addElement(new Paragraph("Địa chỉ: " + fullAddress, fontNormal));

        } else {
            // Nếu không có thông tin giao hàng
            cellShipping.addElement(new Paragraph(" ", fontNormal));
        }

        mainTable.addCell(cellShipping);
        document.add(mainTable);
    }

    private void addOrderDetails(Document document, HoaDon hoaDon) throws DocumentException {
        // Tiêu đề
        document.add(new Paragraph("CHI TIẾT SẢN PHẨM", fontHeader));

        // Tạo bảng 4 cột: Sản phẩm, SL, Đơn giá, Thành tiền (Bỏ STT)
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{5f, 0.8f, 1.5f, 1.8f});
        table.setSpacingBefore(5);

        // --- Header Bảng ---
        String[] headers = {"Sản phẩm", "SL", "Đơn giá", "Thành tiền"};

        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, fontHeader));
            cell.setBackgroundColor(BaseColor.WHITE);
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setBorder(Rectangle.BOTTOM); // Chỉ kẻ gạch dưới
            cell.setBorderWidthBottom(0.5f);
            cell.setBorderColorBottom(BaseColor.GRAY);
            cell.setPadding(6);
            if(h.equals("SL") || h.equals("Đơn giá") || h.equals("Thành tiền")) {
                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            }
            table.addCell(cell);
        }

        // --- Dữ liệu Bảng ---
        for (HoaDonChiTiet item : hoaDon.getHoaDonChiTiets()) {

            // Cột Sản phẩm + Thuộc tính (Không dùng hàm phụ trợ để tùy chỉnh)
            PdfPCell nameCell = new PdfPCell();
            nameCell.setBorder(Rectangle.NO_BORDER);
            nameCell.setPadding(6);
            nameCell.addElement(new Paragraph(item.getSanPhamChiTiet().getSanPham().getTenSanPham(), fontNormal));

            String thuocTinh = item.getSanPhamChiTiet().getMauSac().getTenMauSac() + ", " +
                    item.getSanPhamChiTiet().getKichThuoc().getTenKichThuoc() +
                    " | SKU " + item.getSanPhamChiTiet().getMaSanPhamChiTiet();

            nameCell.addElement(new Paragraph(thuocTinh, fontSmall));
            table.addCell(nameCell);

            // Số lượng (SL)
            addCell(table, String.valueOf(item.getSoLuong()), Element.ALIGN_RIGHT, false, Rectangle.NO_BORDER);

            // Đơn giá
            addCell(table, formatMoney(item.getDonGia(), false), Element.ALIGN_RIGHT, false, Rectangle.NO_BORDER);

            // Thành tiền
            addCell(table, formatMoney(item.getThanhTien(), false), Element.ALIGN_RIGHT, false, Rectangle.NO_BORDER);
        }
        document.add(table);
    }

    private void addSummary(Document document, HoaDon hoaDon) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(40); // Chiếm 40% chiều rộng trang (góc phải)
        table.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.setSpacingBefore(10);
        table.setWidths(new float[]{1.5f, 1.5f});

        // --- Tổng tiền hàng ---
        addSummaryRow(table, "Tổng tiền hàng", formatMoney(hoaDon.getTongTien(), false), fontNormal, fontNormal, Rectangle.NO_BORDER);

        // --- Giảm giá (Voucher) ---
        if (hoaDon.getTienGiamGia() != null && hoaDon.getTienGiamGia().compareTo(BigDecimal.ZERO) > 0) {
            addSummaryRow(table, "Giảm giá", "-" + formatMoney(hoaDon.getTienGiamGia(), false), fontNormal, fontNormal, Rectangle.NO_BORDER);
        }

        // --- Phí ship ---
        addSummaryRow(table, "Phí ship", formatMoney(hoaDon.getPhiVanChuyen(), false), fontNormal, fontNormal, Rectangle.NO_BORDER);

        // Kẻ đường gạch ngang mỏng
        LineSeparator ls = new LineSeparator(0.5f, 100, BaseColor.GRAY, Element.ALIGN_CENTER, 0);
        document.add(new Chunk(ls));

        // --- TỔNG THANH TOÁN (Hóa đơn/Thanh toán) ---
        addSummaryRow(table, "Hóa đơn/Thanh toán", formatMoney(hoaDon.getTongTienSauGiam(), true), fontBold, fontRedBold, Rectangle.NO_BORDER);

        document.add(table);
    }

    // --- CÁC HÀM PHỤ TRỢ (Đã sửa đổi) ---

    private void addCell(PdfPTable table, String text, int align, boolean bold, int borderType) {
        Font cellFont = bold ? fontBold : fontNormal;
        PdfPCell cell = new PdfPCell(new Phrase(text, cellFont));
        cell.setHorizontalAlignment(align);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorder(borderType);
        cell.setPadding(6);
        table.addCell(cell);
    }

    private void addSummaryRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont, int borderType) {
        PdfPCell c1 = new PdfPCell(new Phrase(label, labelFont));
        c1.setBorder(borderType);
        c1.setPadding(3);
        table.addCell(c1);

        PdfPCell c2 = new PdfPCell(new Phrase(value, valueFont));
        c2.setBorder(borderType);
        c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        c2.setPadding(3);
        table.addCell(c2);
    }

    private String formatMoney(BigDecimal money, boolean hasSuffix) {
        if (money == null) return "0" + (hasSuffix ? " đ" : "");
        // Dùng DecimalFormat để loại bỏ phần thập phân nếu bằng 0
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        if (money.scale() > 0 && money.stripTrailingZeros().scale() <= 0) {
            nf.setMaximumFractionDigits(0);
        }
        return nf.format(money.stripTrailingZeros()) + (hasSuffix ? " đ" : "");
    }
}