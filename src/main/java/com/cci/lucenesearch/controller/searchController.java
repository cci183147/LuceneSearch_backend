package com.cci.lucenesearch.controller;

import com.cci.lucenesearch.model.Request;
import com.cci.lucenesearch.service.DocumentService;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
public class searchController {

    @Autowired
    private DocumentService documentService;

    // 文章查询接口
    @GetMapping("/search")
    public List<Document> searchDocuments(@RequestBody Request request) throws IOException, ParseException, InvalidTokenOffsetsException {
        // 获取返回的结果 Map
        Map<String, Object> response = documentService.searchDocuments(request);

        // 强制类型转换
        Object resultsObj = response.get("results");
        if (resultsObj instanceof List<?>) {
            return (List<Document>) resultsObj;
        } else {
            // 处理类型不匹配的情况
            throw new IllegalStateException("Invalid results type");
        }
    }

    // 下载 PDF 的接口（返回 PDF 文件路径）
    @GetMapping("/download/{fileName}")
    public String downloadPdf(@PathVariable String fileName) {
        // 返回文献对应的 PDF 文件路径，前端可用此路径进行下载
        return "src/main/resources/pdf_files/" + fileName + ".pdf";
    }
}
