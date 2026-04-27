package com.example.secondhand.file;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
/**
 * 每隔一段时间扫描 uploads/items。
如果图片超过 60 分钟还没有被商品引用，就自动删除。
 */
@Component
public class FileCleanupTask {

    private final FileStorageService fileStorageService;

    @Value("${app.upload.cleanup-expire-minutes:60}")
    private long cleanupExpireMinutes;

    public FileCleanupTask(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @Scheduled(
            initialDelayString = "${app.upload.cleanup-initial-delay-ms:60000}",
            fixedDelayString = "${app.upload.cleanup-fixed-delay-ms:3600000}"
    )
    public void cleanUnboundImages() {
        int deleted = fileStorageService.cleanUnboundImagesOlderThan(
                Duration.ofMinutes(cleanupExpireMinutes)
        );

        if (deleted > 0) {
            System.out.println("[FileCleanupTask] 已清理未绑定图片数量：" + deleted);
        }
    }
}