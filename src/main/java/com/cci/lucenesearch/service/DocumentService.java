package com.cci.lucenesearch.service;

import com.cci.lucenesearch.model.Request;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.search.highlight.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DocumentService {

    private static final String INDEX_DIR = "index"; // Lucene 索引目录
    // 存储查询结果的缓存，key 是查询的唯一标识，value 是对应的文档列表
    private Map<String, List<Document>> searchCache = new ConcurrentHashMap<>();

    // 查询文献String queryStr, String startDate, String endDate,Integer page, String sortField
    public List<Document> searchDocuments(Request request) throws IOException, ParseException, InvalidTokenOffsetsException {
        List<Document> resultDocs = new ArrayList<>();
        DirectoryReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(INDEX_DIR)));
        IndexSearcher searcher = new IndexSearcher(reader);
        String queryStr = request.getQueryStr();
        String startDate = request.getStartDate();
        String endDate = request.getEndDate();
        int pageSize = 10; // 每页大小
        int page = request.getPage() != null ? request.getPage() : 0;
        String sortField = request.getSortField();
        // 查询解析器
        QueryParser parser = new QueryParser(sortField, new StandardAnalyzer());
        Query query = parser.parse(queryStr);

        // 添加日期筛选
        if (startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            long startTimestamp = 0;
            try {
                startTimestamp = format.parse(startDate).getTime();
            } catch (java.text.ParseException e) {
                throw new RuntimeException(e);
            }
            long endTimestamp = 0;
            try {
                endTimestamp = format.parse(endDate).getTime();
            } catch (java.text.ParseException e) {
                throw new RuntimeException(e);
            }

            Query dateRangeQuery = LongPoint.newRangeQuery("publicationDate", startTimestamp, endTimestamp);

            BooleanQuery.Builder combinedQuery = new BooleanQuery.Builder();
            combinedQuery.add(query, BooleanClause.Occur.MUST);
            combinedQuery.add(dateRangeQuery, BooleanClause.Occur.FILTER);
            query = combinedQuery.build();
        }

        // 查询并获取结果
        TopDocs topDocs = searcher.search(query, page*pageSize); // 获取前10条





        Formatter formatter = new SimpleHTMLFormatter("<span style='color:red;'>", "</span>");
        Highlighter highlighter = new Highlighter(formatter, new QueryScorer(query));

        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            Document doc = searcher.doc(scoreDoc.doc);

            // 高亮标题
            String titleText = doc.get("title");
            TokenStream titleTokenStream = new StandardAnalyzer().tokenStream("title", titleText);
            String highlightedTitle = highlighter.getBestFragments(titleTokenStream, titleText, 3, "...");
            doc.add(new TextField("highlightedTitle", highlightedTitle, Field.Store.YES));

            // 高亮摘要
            String abstractText = doc.get("abstract");
            TokenStream abstractTokenStream = new StandardAnalyzer().tokenStream("abstract", abstractText);
            String highlightedAbstract = highlighter.getBestFragments(abstractTokenStream, abstractText, 3, "...");
            doc.add(new TextField("highlightedAbstract", highlightedAbstract, Field.Store.YES));

            resultDocs.add(doc);
        }

        reader.close();
        return resultDocs;
    }
}
