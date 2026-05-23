package com.li.socialplatform.common.utils;

import cn.hutool.core.io.FileUtil;
import com.li.socialplatform.common.properties.SystemConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


/**
 * @author e69d8e
 * @since 2026/05/22 20:59
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteFileUtils {

    private final SystemConstants systemConstants;

    public void deleteFile(String url) {
        if (url == null || !url.startsWith("/") || url.contains("..")) {
            return;
        }

        java.io.File file = new java.io.File(systemConstants.imageUploadDir, url);
        if (file.isDirectory()) {
            return;
        }

        FileUtil.del(file);
    }
}
