package com.li.socialplatform.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.li.socialplatform.common.properties.SystemConstants;
import com.li.socialplatform.pojo.entity.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * @author e69d8e
 * @since 2025/12/9 14:25
 */
@Slf4j
@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class UploadFileController {
    private final SystemConstants systemConstants;

    // http://localhost:8080/imgs/post/0/1/73197b62-3682-4f43-bb9e-e2593b62d10d.png
    @PostMapping("/post")
    public Result uploadBlogImage(@RequestParam("file") MultipartFile image) {
        return upload(image, "post");
    }

    //    http://localhost:8080/imgs/avatar/1/11/829f7288-f66e-4b8c-8c64-a9b2942270ac.png
//    http://localhost:8080/imgs/avatar/default.png
    @PostMapping("/avatar")
    public Result uploadAvatarImage(@RequestParam("file") MultipartFile image) {
        return upload(image, "avatar");
    }

    private Result upload(@RequestParam("file") MultipartFile image, String type) {
        try {
            // 获取原始文件名称
            String originalFilename = image.getOriginalFilename();
            // 生成新文件名
            String fileName = createNewFileName(originalFilename, type);
            // 保存文件
            image.transferTo(new File(systemConstants.imageUploadDir, fileName));
            // 返回结果
            log.debug("文件上传成功，{}", fileName);
            return Result.ok(systemConstants.baseUrl + fileName);
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败", e);
        }
    }

    @GetMapping("/delete")
    public Result deleteBlogImg(@RequestParam("name") String filename) {
        File file = new File(systemConstants.imageUploadDir, filename);
        if (file.isDirectory()) {
            return Result.error("错误的文件名称");
        }
        FileUtil.del(file);
        return Result.ok();
    }

    private String createNewFileName(String originalFilename, String type) {
        // 获取后缀
        String suffix = StrUtil.subAfter(originalFilename, ".", true);
        // 生成目录
        String name = UUID.randomUUID().toString();
        int hash = name.hashCode();
        int d1 = hash & 0xF;
        int d2 = (hash >> 4) & 0xF;
        // 判断目录是否存在
        File dir = new File(systemConstants.imageUploadDir, StrUtil.format("/{}/{}/{}",type, d1, d2));
        if (!dir.exists()) {
            boolean mkdir = dir.mkdirs();
            log.info("创建目录：{}", mkdir);
        }
        // 生成文件名
        return  StrUtil.format("/{}/{}/{}/{}.{}", type, d1, d2, name, suffix);
    }
}
