package com.cci.lucenesearch.service;

import com.cci.lucenesearch.model.Request;
import jakarta.annotation.PostConstruct;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.search.highlight.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class DocumentService {

    private static final String INDEX_DIR = "index"; // Lucene 索引目录
    // 存储查询结果的缓存，key 是查询的唯一标识，value 是对应的文档列表
    private Map<String, CacheEntry> searchCache = new ConcurrentHashMap<>();
    private static final long CACHE_EXPIRY_TIME = 60 * 60 * 1000;  // 1 hour in milliseconds
    private static final long CACHE_CLEAN_INTERVAL = 30 * 60 * 1000;  // 30 minutes to clean expired cache
    // 使用 ScheduledExecutorService 定期清理缓存
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @PostConstruct
    public void init() {
        // 每 30 分钟清理一次过期缓存
        scheduler.scheduleAtFixedRate(this::clearExpiredCache, 0, CACHE_CLEAN_INTERVAL, TimeUnit.MILLISECONDS);
    }

    // 定时清理缓存中过期的条目
    private void clearExpiredCache() {
        long currentTime = System.currentTimeMillis();
        searchCache.entrySet().removeIf(entry -> currentTime - entry.getValue().timestamp > CACHE_EXPIRY_TIME);
    }
    // 查询文献String queryStr, String startDate, String endDate,Integer page, String sortField
    public Map<String, Object> searchDocuments(Request request) throws IOException, ParseException, InvalidTokenOffsetsException {
        // 获取请求参数
        String queryStr = request.getQueryStr();
        String startDate = request.getStartDate();
        String endDate = request.getEndDate();
        int pageSize = 10;  // 默认每页10条
        int page = request.getPage();
        String sortField = request.getSortField();

        // 创建缓存的 queryId，用于存储查询结果
        String queryId = queryStr + ":" + startDate + ":" + endDate + ":" + sortField;

        // 如果缓存中有该查询结果，直接返回缓存的结果
        if (searchCache.containsKey(queryId)) {
            return getStringObjectMap(pageSize, page, queryId);
        }

        // 执行新的搜索
        List<Document> resultDocs = new ArrayList<>();
        DirectoryReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(INDEX_DIR)));
        IndexSearcher searcher = new IndexSearcher(reader);

        // 查询解析器
        QueryParser parser = new QueryParser(sortField, new StandardAnalyzer());
        Query query = parser.parse(queryStr);

        // 日期范围过滤
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
        TopDocs topDocs = searcher.search(query, 1000);

        // 高亮设置
        Formatter formatter = new SimpleHTMLFormatter("<span style='color:red;'>", "</span>");
        Highlighter highlighter = new Highlighter(formatter, new QueryScorer(query));

        // 处理搜索结果
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            Document doc = searcher.doc(scoreDoc.doc);

            // 高亮标题
            String titleText = doc.get("title");
            if (titleText != null) {
                try (TokenStream titleTokenStream = new StandardAnalyzer().tokenStream("title", titleText)) {
                    String highlightedTitle = highlighter.getBestFragments(titleTokenStream, titleText, 5, "...");
                    doc.add(new TextField("highlightedTitle", highlightedTitle, Field.Store.YES));
                }
            }
            // 高亮摘要
            String abstractText = doc.get("abstract");
            if (abstractText != null) {
                try (TokenStream abstractTokenStream = new StandardAnalyzer().tokenStream("abstract", abstractText)) {
                    String highlightedAbstract = highlighter.getBestFragments(abstractTokenStream, abstractText, 5, "...");
                    doc.add(new TextField("highlightedAbstract", highlightedAbstract, Field.Store.YES));
                }

            }
            resultDocs.add(doc);
        }
            long totalCount = topDocs.totalHits.value;
            // 将搜索结果缓存
            searchCache.put(queryId, new CacheEntry(resultDocs,totalCount));
            reader.close();
            // 分页处理
            return getStringObjectMap(pageSize, page, queryId,totalCount);

    }
    @NotNull
    private Map<String, Object> getStringObjectMap(int pageSize, int page, String queryId) {
        List<Document> cachedDocs = getCachedResults(queryId, page, pageSize);
        Map<String, Object> response = new HashMap<>();
        long totalCount = searchCache.get(queryId).totalCount;
        response.put("page", page);
        response.put("pageSize", pageSize);
        response.put("results", cachedDocs);
        response.put("totalCount", totalCount);
        return response;
    }

    @NotNull
    private Map<String, Object> getStringObjectMap(int pageSize, int page, String queryId, long totalCount) {
        List<Document> cachedDocs = getCachedResults(queryId, page, pageSize);
        Map<String, Object> response = new HashMap<>();

        response.put("page", page);
        response.put("pageSize", pageSize);
        response.put("totalCount", totalCount);
        response.put("results", cachedDocs);// 新增字段：总数
        return response;
    }


    // 从缓存中获取分页数据
    private List<Document> getCachedResults(String queryId, int page, int pageSize) {
        List<Document> results = searchCache.get(queryId).documents;
        if (results == null) {
            return Collections.emptyList();
        }

        int fromIndex = page * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, results.size());

        if (fromIndex >= results.size()) {
            return Collections.emptyList();
        }

        return results.subList(fromIndex, toIndex);
    }
    private static class CacheEntry {
        List<Document> documents;
        long totalCount;
        long timestamp;

        CacheEntry(List<Document> documents, long totalCount) {
            this.documents = documents;
            this.totalCount = totalCount;
            this.timestamp = System.currentTimeMillis();
        }
    }
}
