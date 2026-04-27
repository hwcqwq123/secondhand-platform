package com.example.secondhand.file;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.example.secondhand.config.ApiResponse;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<UploadResponse> upload(@RequestParam("file") MultipartFile file) {
        try {
            String url = fileStorageService.storeImage(file);
            return ApiResponse.ok(new UploadResponse(url), "上传成功");
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(e.getMessage());
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

    /**
     * 删除未绑定图片。
     *
     * 这个接口只会删除没有被商品引用的图片。
     * 如果图片已经写入 item_images 或 items.cover_image_url，就不会删除。
     */
    @PostMapping("/delete-unbound")
    public ApiResponse<DeleteUnboundResponse> deleteUnboundImages(
            @RequestBody DeleteUnboundRequest request
    ) {
        int deleted = fileStorageService.deleteUnboundImages(request.getUrls());
        return ApiResponse.ok(new DeleteUnboundResponse(deleted), "未绑定图片清理完成");
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

    public static class DeleteUnboundRequest {
        private List<String> urls;

        public List<String> getUrls() {
            return urls;
        }

        public void setUrls(List<String> urls) {
            this.urls = urls;
        }
    }

    public static class DeleteUnboundResponse {
        private int deleted;

        public DeleteUnboundResponse() {
        }

        public DeleteUnboundResponse(int deleted) {
            this.deleted = deleted;
        }

        public int getDeleted() {
            return deleted;
        }

        public void setDeleted(int deleted) {
            this.deleted = deleted;
        }
    }
}