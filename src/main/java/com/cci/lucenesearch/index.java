package com.cci.lucenesearch;

import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.store.*;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import javax.xml.parsers.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import static com.cci.lucenesearch.Utils.*;

public class index {

    public static void main(String[] args) throws Exception {
        // 创建索引目录
        Directory directory = FSDirectory.open(new File("index").toPath());
        IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
        IndexWriter indexWriter = new IndexWriter(directory, config);

        // PDF 文件所在目录
        File pdfDir = new File("src/main/resources/pdf_files");
        File[] pdfFiles = pdfDir.listFiles();

        if (pdfFiles != null) {
            for (File pdfFile : pdfFiles) {
                String fileName = pdfFile.getName();
                String pdfPath = pdfFile.getAbsolutePath();

                // 读取 PDF 文件对应的 XML 文件（文件名一致，扩展名为 .xml）
                String xmlFilePath = "src/main/resources/xml_files/" + fileName.replace(".pdf", ".xml");
                File xmlFile = new File(xmlFilePath);

                if (xmlFile.exists()) {
                    // 解析 XML 文件并提取信息
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    factory.setNamespaceAware(true);  // 启用命名空间支持
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    org.w3c.dom.Document doc = builder.parse(xmlFile);

                    // 提取 XML 信息
                    String title = extractTitle(doc); // 提取标题
                    String author = extractAuthors(doc); // 提取作者
                    String abstractText = extractAbstract(doc); // 提取摘要
                    String publicationDate = extractPublicationDate(doc); // 提取出版日期
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    Date date = format.parse(publicationDate);
                    // 创建 Lucene 文档
                    Document luceneDoc = new Document();
                    luceneDoc.add(new TextField("title", title, Field.Store.YES));
                    luceneDoc.add(new TextField("author", author, Field.Store.YES));
                    luceneDoc.add(new TextField("abstract", abstractText, Field.Store.YES));
                    luceneDoc.add(new StringField("pdfPath", fileName, Field.Store.YES)); // 存储 PDF 路径
                    long timestamp = date.getTime(); // 转换为时间戳
                    luceneDoc.add(new TextField("publicationDate",publicationDate,Field.Store.YES));
                    luceneDoc.add(new LongPoint("publicationDate", timestamp));
                    // 添加文档到索引
                    indexWriter.addDocument(luceneDoc);
                }
            }
        }

        // 提交并关闭索引
        indexWriter.commit();
        indexWriter.close();

        System.out.println("Indexing completed!");
    }

    // 解析标题

}
