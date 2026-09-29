package vn.iotstar.service.impl;

import com.cloudinary.Cloudinary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.iotstar.service.CloudinaryService;
import vn.iotstar.service.CloudinaryUploadResult;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    @Override
    public CloudinaryUploadResult upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Chưa chọn ảnh");
        }

        String type = file.getContentType();
        if (type == null || !type.startsWith("image/")) {
            throw new IllegalArgumentException("Chỉ cho phép file hình ảnh");
        }

        if (cloudinary.config.cloudName != null && !cloudinary.config.cloudName.isBlank()
                && !cloudinary.config.cloudName.equalsIgnoreCase("dfdfdf")) {
            try {
                Map<?, ?> result = cloudinary.uploader().upload(
                        file.getBytes(),
                        Map.of("folder", "shop/products")
                );
                return new CloudinaryUploadResult(
                        String.valueOf(result.get("secure_url")),
                        String.valueOf(result.get("public_id"))
                );
            } catch (Exception ignored) {
            }
        }

        try {
            Path uploadDir = Paths.get("uploads");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            String filename = System.currentTimeMillis() + "_" + (file.getOriginalFilename() != null ? file.getOriginalFilename() : "image.jpg");
            Files.copy(file.getInputStream(), uploadDir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
            return new CloudinaryUploadResult("/uploads/" + filename, "local_" + filename);
        } catch (Exception e) {
            throw new IllegalStateException("Upload Cloudinary thất bại", e);
        }
    }

    @Override
    public void delete(String publicId) {
        if (publicId == null || publicId.isBlank()) return;

        if (publicId.startsWith("local_")) {
            try {
                Files.deleteIfExists(Paths.get("uploads", publicId.substring(6)));
            } catch (Exception ignored) {
            }
            return;
        }

        try {
            cloudinary.uploader().destroy(
                    publicId, Map.of("resource_type", "image")
            );
        } catch (Exception ignored) {
        }
    }
}
