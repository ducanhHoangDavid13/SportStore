package sd_04.datn_fstore.service.impl;
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
    private final Path rootLocation;

    @Autowired
    public FileStorageServiceImpl(FileStorageProperties properties) {
        this.rootLocation = Paths.get(properties.getUploadDir()).toAbsolutePath().normalize(); // Thêm normalize
    }

    @Override
    @PostConstruct
    public void init() {
        try {
            if (!Files.exists(rootLocation)) {
                Files.createDirectories(rootLocation);
                System.out.println("Đã tạo thư mục lưu trữ: " + rootLocation.toString());
            } else {
                System.out.println("Thư mục lưu trữ đã tồn tại: " + rootLocation.toString());
            }
        } catch (IOException e) {
            throw new RuntimeException("Không thể khởi tạo thư mục lưu trữ file!", e);
        }
    }

    @Override
    public String storeFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("Lỗi: File rỗng!");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());

        // Kiểm tra đường dẫn có hợp lệ (ví dụ: tránh ../../)
        if (originalFilename.contains("..")) {
            throw new RuntimeException("Tên file chứa chuỗi ký tự không hợp lệ: " + originalFilename);
        }

        String uniqueFilename = UUID.randomUUID().toString() + "_" + originalFilename;

        try {
            Path destinationFile = this.rootLocation.resolve(uniqueFilename);

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            return uniqueFilename;

        } catch (IOException e) {
            throw new RuntimeException("Lưu file thất bại. " + originalFilename, e);
        }
    }

    @Override
    public Resource loadFileAsResource(String filename) {
        try {
            Path file = rootLocation.resolve(filename).normalize();
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Không thể đọc file: " + filename + ". File không tồn tại hoặc không đọc được.");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Lỗi đường dẫn file: " + filename, e);
        }
    }

    @Override
    public void deleteFile(String filename) {
        try {
            Path file = rootLocation.resolve(filename).normalize();
            Files.deleteIfExists(file);
        } catch (IOException e) {
            // Không throw exception mà chỉ ghi log, vì mục tiêu chính là xóa record DB.
            // Nếu xóa file vật lý thất bại, DB vẫn phải được update.
            System.err.println("Lỗi khi xóa file vật lý: " + filename + " - " + e.getMessage());
        }
    }
}