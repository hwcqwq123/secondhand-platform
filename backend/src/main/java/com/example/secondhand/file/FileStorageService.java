package com.example.secondhand.file;

import com.example.secondhand.item.ItemImageRepository;
import com.example.secondhand.item.ItemRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "webp", "gif"
    );

    private final ItemImageRepository itemImages;
    private final ItemRepository items;

    @Value("${app.upload.items-dir:uploads/items}")
    private String itemsDir;

    public FileStorageService(
            ItemImageRepository itemImages,
            ItemRepository items
    ) {
        this.itemImages = itemImages;
        this.items = items;
    }

    public String storeImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        String contentType = file.getContentType();

        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("仅支持图片文件");
        }

        String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());

        if (ext == null || ext.isBlank()) {
            throw new IllegalArgumentException("图片文件后缀不能为空");
        }

        ext = ext.toLowerCase();

        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new IllegalArgumentException("仅支持 jpg、jpeg、png、webp、gif 格式图片");
        }

        Path root = uploadRoot();
        Files.createDirectories(root);

        String filename = UUID.randomUUID() + "." + ext;
        Path target = root.resolve(filename).normalize();

        if (!target.startsWith(root)) {
            throw new IllegalArgumentException("非法文件路径");
        }

        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        return "/uploads/items/" + filename;
    }

    public int deleteUnboundImages(List<String> urls) {
        if (urls == null || urls.isEmpty()) {
            return 0;
        }

        int deletedCount = 0;

        for (String url : urls) {
            if (url == null || url.isBlank()) {
                continue;
            }

            if (isReferenced(url)) {
                continue;
            }

            Path path = pathFromUrl(url);

            if (path == null) {
                continue;
            }

            try {
                if (Files.deleteIfExists(path)) {
                    deletedCount++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return deletedCount;
    }

    public int cleanUnboundImagesOlderThan(Duration maxAge) {
        Path root = uploadRoot();

        if (!Files.exists(root)) {
            return 0;
        }

        int deletedCount = 0;
        Instant cutoff = Instant.now().minus(maxAge);

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(root)) {
            for (Path path : stream) {
                if (!Files.isRegularFile(path)) {
                    continue;
                }

                String filename = path.getFileName().toString();
                String url = "/uploads/items/" + filename;

                if (isReferenced(url)) {
                    continue;
                }

                Instant lastModified = Files.getLastModifiedTime(path).toInstant();

                if (lastModified.isAfter(cutoff)) {
                    continue;
                }

                try {
                    if (Files.deleteIfExists(path)) {
                        deletedCount++;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return deletedCount;
    }

    private boolean isReferenced(String url) {
        return itemImages.existsByImageUrl(url) || items.existsByCoverImageUrl(url);
    }

    private Path uploadRoot() {
        return Paths.get(itemsDir).toAbsolutePath().normalize();
    }

    private Path pathFromUrl(String url) {
        if (!url.startsWith("/uploads/items/")) {
            return null;
        }

        String filename = Paths.get(url).getFileName().toString();

        if (filename == null || filename.isBlank()) {
            return null;
        }

        Path root = uploadRoot();
        Path path = root.resolve(filename).normalize();

        if (!path.startsWith(root)) {
            return null;
        }

        return path;
    }
}