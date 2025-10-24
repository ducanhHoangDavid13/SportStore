package sd_04.datn_fstore.service;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import sd_04.datn_fstore.config.FileStorageProperties;
import sd_04.datn_fstore.service.FileStorageService;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {
    private final Path rootLocation; // Đường dẫn thư mục gốc để lưu file

    @Autowired
    public FileStorageServiceImpl(FileStorageProperties properties) {
        // Lấy đường dẫn từ file properties
        this.rootLocation = Paths.get(properties.getUploadDir());
    }

    /**
     * Hàm này được gọi ngay sau khi Service được tạo.
     * Nó sẽ kiểm tra và tạo thư mục Upload nếu chưa tồn tại.
     */
    @Override
    @PostConstruct // Đảm bảo hàm này chạy khi khởi động
    public void init() {
        try {
            Files.createDirectories(rootLocation);
            System.out.println("Đã tạo thư mục lưu trữ: " + rootLocation.toString());
        } catch (IOException e) {
            throw new RuntimeException("Không thể khởi tạo thư mục lưu trữ file!", e);
        }
    }

    /**
     * Logic lưu file
     */
    @Override
    public String storeFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("Lỗi: File rỗng!");
        }

        // 1. Chuẩn hóa tên file (loại bỏ ký tự đặc biệt)
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());

        // 2. Tạo tên file duy nhất (Rất quan trọng)
        // Ví dụ: abc.jpg -> 123e4567-e89b-12d3-a456-426614174000_abc.jpg
        String uniqueFilename = UUID.randomUUID().toString() + "_" + originalFilename;

        try {
            // 3. Resolve đường dẫn lưu file
            Path destinationFile = this.rootLocation.resolve(uniqueFilename);

            // 4. Copy file vào thư mục đích (ghi đè nếu đã tồn tại)
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            // 5. Trả về tên file duy nhất đã lưu
            return uniqueFilename;

        } catch (IOException e) {
            throw new RuntimeException("Lưu file thất bại. " + originalFilename, e);
        }
    }

    /**
     * Logic tải file
     */
    @Override
    public Resource loadFileAsResource(String filename) {
        try {
            Path file = rootLocation.resolve(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Không thể đọc file: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Lỗi: " + filename, e);
        }
    }

    /**
     * Logic xóa file
     */
    @Override
    public void deleteFile(String filename) {
        try {
            Path file = rootLocation.resolve(filename);
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new RuntimeException("Không thể xóa file: " + filename, e);
        }
    }
}
