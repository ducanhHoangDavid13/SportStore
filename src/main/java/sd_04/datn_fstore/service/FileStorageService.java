package sd_04.datn_fstore.service;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
public interface FileStorageService {
    /**
     * Khởi tạo thư mục lưu trữ khi ứng dụng khởi động.
     */
    void init();

    /**
     * Lưu một file upload lên server.
     * @param file File người dùng upload (từ MultipartFile)
     * @return Tên file duy nhất đã được lưu (ví dụ: uuid_ten_file.jpg)
     */
    String storeFile(MultipartFile file);

    /**
     * Tải một file dưới dạng Resource (dùng để hiển thị hoặc download).
     * @param filename Tên file cần tải
     * @return Đối tượng Resource
     */
    Resource loadFileAsResource(String filename);

    /**
     * Xóa một file vật lý khỏi server.
     * @param filename Tên file cần xóa
     */
    void deleteFile(String filename);
}
