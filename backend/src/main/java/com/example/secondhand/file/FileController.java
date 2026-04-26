package com.example.secondhand.file;

import com.example.secondhand.config.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final Path uploadRoot = Paths.get("uploads", "items");

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<UploadResponse> upload(@RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return ApiResponse.fail("文件不能为空");
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ApiResponse.fail("仅支持图片文件");
            }

            Files.createDirectories(uploadRoot);

            String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
            String filename = UUID.randomUUID().toString();
            if (ext != null && !ext.isBlank()) {
                filename += "." + ext;
            }

            Path target = uploadRoot.resolve(filename);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            String url = "/uploads/items/" + filename;
            return ApiResponse.ok(new UploadResponse(url), "上传成功");

        } catch (IOException e) {
            e.printStackTrace();
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "图片保存失败: " + e.getMessage()
            );
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "图片上传失败: " + e.getMessage()
            );
        }
    }

    public static class UploadResponse {
        private String url;

        public UploadResponse() {
        }

        public UploadResponse(String url) {
            this.url = url;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}