package com.cci.lucenesearch.controller;

import com.cci.lucenesearch.model.Request;
import com.cci.lucenesearch.service.DocumentService;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/api/documents")
public class searchController {

    @Autowired
    private DocumentService documentService;

    // 文章查询接口
    @PostMapping("/search")
    public Map<String, Object> searchDocuments(@RequestBody Request request) throws IOException, ParseException, InvalidTokenOffsetsException {
        // 获取返回的结果 Map
        return documentService.searchDocuments(request);
    }
    @GetMapping("/download/{fileName}")
    public ResponseEntity<InputStreamResource> downloadPdf(@PathVariable String fileName) {
        // 文件存储的根目录
        String basePath = "src/main/resources/pdf_files/";

        // 防止路径遍历攻击，确保文件名不包含 ".." 等恶意字符
        if (fileName == null || fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            return ResponseEntity.badRequest().build();
        }

        try {
            // 拼接文件路径
            Path path = Paths.get(basePath + fileName );

            // 如果文件不存在，返回404
            if (!Files.exists(path)) {
                return ResponseEntity.notFound().build();
            }

            // 创建 InputStreamResource 读取文件
            InputStreamResource resource = new InputStreamResource(Files.newInputStream(path));

            // 返回文件作为响应体，并设置Content-Disposition为附件下载
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)  // 设置文件类型为 PDF
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + path.getFileName().toString() + "\"")
                    .contentLength(Files.size(path))  // 设置文件大小
                    .body(resource);

        } catch (IOException e) {
            // 处理异常，返回 500 错误
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


}
