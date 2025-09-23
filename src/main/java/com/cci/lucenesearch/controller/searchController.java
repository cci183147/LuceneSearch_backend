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

@RestController
@RequestMapping("/api/documents")
public class searchController {

    @Autowired
    private DocumentService documentService;

    // 文章查询接口
    @GetMapping("/search")
    public List<Document> searchDocuments(@RequestBody Request request) throws IOException, ParseException, InvalidTokenOffsetsException {

        return documentService.searchDocuments(request);
    }

    // 下载 PDF 的接口（返回 PDF 文件路径）
    @GetMapping("/download/{fileName}")
    public String downloadPdf(@PathVariable String fileName) {
        // 返回文献对应的 PDF 文件路径，前端可用此路径进行下载
        return "src/main/resources/pdf_files/" + fileName + ".pdf";
    }
}
