package sd_04.datn_fstore.service;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
public interface FileStorageService {

    void init();

    String storeFile(MultipartFile file);

    Resource loadFileAsResource(String filename);

    void deleteFile(String filename);
}