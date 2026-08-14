package com.nexora.admin.biz;

import com.nexora.admin.component.KnowledgeVectorComponent;
import com.nexora.admin.dto.KnowledgeSearchTestRequest;
import com.nexora.admin.vo.KnowledgeImportResultVO;
import com.nexora.admin.vo.KnowledgeOverviewVO;
import com.nexora.admin.vo.KnowledgeSearchResultVO;
import com.nexora.admin.vo.KnowledgeTreeNodeVO;
import com.nexora.entity.po.KnowledgeDoc;
import com.nexora.entity.po.KnowledgePoint;
import com.nexora.entity.query.KnowledgeDocQuery;
import com.nexora.entity.query.KnowledgePointQuery;
import com.nexora.entity.vo.PaginationResultVO;
import com.nexora.exception.BusinessException;
import com.nexora.service.KnowledgeDocService;
import com.nexora.service.KnowledgePointService;
import com.nexora.utils.StringTools;
import com.nexora.utils.TextChunker;
import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * 知识库管理业务：总览、目录、文档、知识点、导入、向量化、问答测试。
 */
@Service
public class KnowledgeBaseBiz {

    private static final Pattern FRONTMATTER_PATTERN = Pattern.compile(
            "^---\\s*\\n(.*?)\\n---\\s*\\n", Pattern.DOTALL);
    private static final int CHUNK_SIZE = 500;

    @Resource
    private KnowledgeDocService knowledgeDocService;

    @Resource
    private KnowledgePointService knowledgePointService;

    @Resource
    private KnowledgeVectorComponent knowledgeVectorComponent;

    @Value("${project.folder}")
    private String projectFolder;

    public KnowledgeOverviewVO overview() {
        List<KnowledgeDoc> docs = knowledgeDocService.findListByParam(new KnowledgeDocQuery());
        KnowledgeOverviewVO vo = new KnowledgeOverviewVO();
        vo.setTotalDocs(docs.size());
        vo.setTotalPoints(knowledgePointService.findCountByParam(new KnowledgePointQuery()));
        Map<String, Integer> stageMap = new LinkedHashMap<>();
        Map<String, Integer> statusMap = new LinkedHashMap<>();
        int chunks = 0;
        for (KnowledgeDoc doc : docs) {
            String stage = doc.getStage() == null ? "UNKNOWN" : doc.getStage();
            stageMap.merge(stage, 1, Integer::sum);
            int status = doc.getVectorStatus() == null ? 0 : doc.getVectorStatus();
            statusMap.merge(String.valueOf(status), 1, Integer::sum);
            chunks += doc.getChunkCount() == null ? 0 : doc.getChunkCount();
            if (status == 2) {
                vo.setReadyDocs(vo.getReadyDocs() + 1);
            } else if (status == 3) {
                vo.setFailedDocs(vo.getFailedDocs() + 1);
            } else if (status == 4) {
                vo.setExpiredDocs(vo.getExpiredDocs() + 1);
            }
        }
        vo.setTotalChunks(chunks);
        vo.setStageDistribution(stageMap);
        vo.setVectorStatusDistribution(statusMap);
        return vo;
    }

    public List<KnowledgeTreeNodeVO> tree() {
        List<KnowledgePoint> points = knowledgePointService.findListByParam(new KnowledgePointQuery());
        List<KnowledgeDoc> docs = knowledgeDocService.findListByParam(new KnowledgeDocQuery());
        Map<String, Integer> docCountByPoint = new LinkedHashMap<>();
        for (KnowledgeDoc doc : docs) {
            if (doc.getKnowledgePointId() != null) {
                docCountByPoint.merge(doc.getKnowledgePointId(), 1, Integer::sum);
            }
        }

        Map<String, KnowledgeTreeNodeVO> stageMap = new LinkedHashMap<>();
        Map<String, KnowledgeTreeNodeVO> subjectMap = new LinkedHashMap<>();
        for (KnowledgePoint point : points) {
            String stage = point.getStage() == null ? "UNKNOWN" : point.getStage();
            String subject = point.getSubject() == null ? "AI" : point.getSubject();
            String stageKey = "stage:" + stage;
            String subjectKey = "subject:" + stage + ":" + subject;
            KnowledgeTreeNodeVO stageNode = stageMap.computeIfAbsent(stageKey, k -> {
                KnowledgeTreeNodeVO node = new KnowledgeTreeNodeVO();
                node.setKey(stageKey);
                node.setLabel(stage);
                node.setType("stage");
                node.setStage(stage);
                return node;
            });
            KnowledgeTreeNodeVO subjectNode = subjectMap.computeIfAbsent(subjectKey, k -> {
                KnowledgeTreeNodeVO node = new KnowledgeTreeNodeVO();
                node.setKey(subjectKey);
                node.setLabel(subject);
                node.setType("subject");
                node.setStage(stage);
                node.setSubject(subject);
                stageNode.getChildren().add(node);
                return node;
            });
            KnowledgeTreeNodeVO pointNode = new KnowledgeTreeNodeVO();
            pointNode.setKey("point:" + point.getKnowledgePointId());
            pointNode.setLabel(point.getName());
            pointNode.setType("point");
            pointNode.setStage(stage);
            pointNode.setSubject(subject);
            pointNode.setKnowledgePointId(point.getKnowledgePointId());
            pointNode.setDifficulty(point.getDifficulty());
            pointNode.setDocCount(docCountByPoint.getOrDefault(point.getKnowledgePointId(), 0));
            subjectNode.getChildren().add(pointNode);
        }
        return new ArrayList<>(stageMap.values());
    }

    public PaginationResultVO<KnowledgeDoc> docList(KnowledgeDocQuery query) {
        if (query.getPageNo() == null) {
            query.setPageNo(1);
        }
        if (query.getPageSize() == null) {
            query.setPageSize(10);
        }
        return knowledgeDocService.findListByPage(query);
    }

    public void docAdd(KnowledgeDoc bean) {
        validateDoc(bean);
        bean.setDocId(UUID.randomUUID().toString().replace("-", ""));
        bean.setDataType(bean.getDataType() == null ? "KNOWLEDGE" : bean.getDataType());
        bean.setSourceType(bean.getSourceType() == null ? 0 : bean.getSourceType());
        bean.setVectorStatus(bean.getVectorStatus() == null ? 0 : bean.getVectorStatus());
        bean.setChunkCount(bean.getChunkCount() == null ? 0 : bean.getChunkCount());
        bean.setStatus(bean.getStatus() == null ? 1 : bean.getStatus());
        Date now = new Date();
        bean.setCreateTime(now);
        bean.setUpdateTime(now);
        knowledgeDocService.add(bean);
    }

    public void docUpdate(KnowledgeDoc bean) {
        if (StringTools.isEmpty(bean.getDocId())) {
            throw new BusinessException("文档ID不能为空");
        }
        KnowledgeDoc exist = knowledgeDocService.getKnowledgeDocByDocId(bean.getDocId());
        if (exist == null) {
            throw new BusinessException("文档不存在");
        }
        if (bean.getVectorStatus() == null) {
            bean.setVectorStatus(4);
        }
        if (bean.getContent() != null && !bean.getContent().equals(exist.getContent())) {
            bean.setVectorStatus(4);
        }
        if (bean.getStage() != null && !bean.getStage().equals(exist.getStage())) {
            bean.setVectorStatus(4);
        }
        if (bean.getKnowledgePointId() != null && !bean.getKnowledgePointId().equals(exist.getKnowledgePointId())) {
            bean.setVectorStatus(4);
        }
        if (bean.getDifficulty() != null && !bean.getDifficulty().equals(exist.getDifficulty())) {
            bean.setVectorStatus(4);
        }
        bean.setUpdateTime(new Date());
        knowledgeDocService.updateKnowledgeDocByDocId(bean, bean.getDocId());
    }

    public void docDel(String docId) {
        if (StringTools.isEmpty(docId)) {
            throw new BusinessException("文档ID不能为空");
        }
        KnowledgeDoc doc = knowledgeDocService.getKnowledgeDocByDocId(docId);
        if (doc != null) {
            int count = doc.getChunkCount() == null ? 0 : doc.getChunkCount();
            safeDeleteChunks(docId, count);
        }
        knowledgeDocService.deleteKnowledgeDocByDocId(docId);
    }

    public void pointAdd(KnowledgePoint bean) {
        if (StringTools.isEmpty(bean.getName()) || StringTools.isEmpty(bean.getStage())) {
            throw new BusinessException("知识点名称和学段不能为空");
        }
        bean.setKnowledgePointId(UUID.randomUUID().toString().replace("-", ""));
        bean.setSubject(bean.getSubject() == null ? "AI" : bean.getSubject());
        bean.setDifficulty(bean.getDifficulty() == null ? 1 : bean.getDifficulty());
        bean.setSort(bean.getSort() == null ? 0 : bean.getSort());
        bean.setStatus(bean.getStatus() == null ? 1 : bean.getStatus());
        Date now = new Date();
        bean.setCreateTime(now);
        bean.setUpdateTime(now);
        knowledgePointService.add(bean);
    }

    public void pointUpdate(KnowledgePoint bean) {
        if (StringTools.isEmpty(bean.getKnowledgePointId())) {
            throw new BusinessException("知识点ID不能为空");
        }
        bean.setUpdateTime(new Date());
        knowledgePointService.updateKnowledgePointByKnowledgePointId(bean, bean.getKnowledgePointId());
    }

    public void pointDel(String knowledgePointId) {
        if (StringTools.isEmpty(knowledgePointId)) {
            throw new BusinessException("知识点ID不能为空");
        }
        KnowledgeDocQuery query = new KnowledgeDocQuery();
        query.setKnowledgePointId(knowledgePointId);
        if (knowledgeDocService.findCountByParam(query) > 0) {
            throw new BusinessException("知识点下存在文档，不能删除");
        }
        knowledgePointService.deleteKnowledgePointByKnowledgePointId(knowledgePointId);
    }

    public KnowledgeImportResultVO importDir() {
        KnowledgeImportResultVO result = new KnowledgeImportResultVO();
        Path root = Paths.get(projectFolder, "knowledge").toAbsolutePath().normalize();
        if (!Files.exists(root)) {
            return result;
        }
        List<Path> files = new ArrayList<>();
        try (Stream<Path> stream = Files.walk(root)) {
            stream.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().toLowerCase().endsWith(".md"))
                    .filter(path -> root.relativize(path).getNameCount() >= 2)
                    .filter(path -> !path.getFileName().toString().equalsIgnoreCase("README.md"))
                    .forEach(files::add);
        } catch (IOException e) {
            result.setFailedCount(1);
            result.getErrors().add("扫描 knowledge 目录失败: " + e.getMessage());
            return result;
        }
        for (Path file : files) {
            try {
                String relative = root.relativize(file).toString().replace('\\', '/');
                String content = Files.readString(file, StandardCharsets.UTF_8);
                Map<String, String> meta = parseFrontmatter(content);
                String title = firstNonBlank(meta.get("title"), stripExtension(file.getFileName().toString()));
                String stage = normalizeStage(firstNonBlank(meta.get("stage"), guessStage(relative)));
                String pointId = firstNonBlank(meta.get("knowledgePointId"),
                        UUID.nameUUIDFromBytes(("kp:" + stage + ":" + title).getBytes(StandardCharsets.UTF_8))
                                .toString().replace("-", ""));
                String pointName = firstNonBlank(meta.get("knowledgePointName"), title);
                int difficulty = parseIntSafe(meta.get("difficulty"), 1);
                String docId = UUID.nameUUIDFromBytes(relative.getBytes(StandardCharsets.UTF_8))
                        .toString().replace("-", "");

                ensureKnowledgePoint(pointId, pointName, stage, difficulty);

                KnowledgeDoc doc = knowledgeDocService.getKnowledgeDocByDocId(docId);
                if (doc == null) {
                    doc = new KnowledgeDoc();
                    doc.setDocId(docId);
                    doc.setCreateTime(new Date());
                    doc.setSourceType(0);
                    doc.setStatus(1);
                    doc.setVectorStatus(0);
                    doc.setChunkCount(0);
                    doc.setTitle(title);
                    doc.setStage(stage);
                    doc.setKnowledgePointId(pointId);
                    doc.setDifficulty(difficulty);
                    doc.setDataType("KNOWLEDGE");
                    doc.setContent(content);
                    doc.setUpdateTime(new Date());
                    knowledgeDocService.add(doc);
                } else {
                    KnowledgeDoc update = new KnowledgeDoc();
                    update.setTitle(title);
                    update.setStage(stage);
                    update.setKnowledgePointId(pointId);
                    update.setDifficulty(difficulty);
                    update.setContent(content);
                    update.setVectorStatus(4);
                    update.setUpdateTime(new Date());
                    knowledgeDocService.updateKnowledgeDocByDocId(update, docId);
                }
                vectorize(docId);
                result.setSuccessCount(result.getSuccessCount() + 1);
            } catch (Exception e) {
                result.setFailedCount(result.getFailedCount() + 1);
                result.getErrors().add(file.getFileName() + ": " + e.getMessage());
            }
        }
        return result;
    }

    public void vectorize(String docId) {
        if (StringTools.isEmpty(docId)) {
            throw new BusinessException("文档ID不能为空");
        }
        KnowledgeDoc doc = knowledgeDocService.getKnowledgeDocByDocId(docId);
        if (doc == null) {
            throw new BusinessException("文档不存在");
        }
        if (StringTools.isEmpty(doc.getContent())) {
            throw new BusinessException("文档内容为空，无法入库");
        }
        int oldCount = doc.getChunkCount() == null ? 0 : doc.getChunkCount();
        KnowledgeDoc processing = new KnowledgeDoc();
        processing.setVectorStatus(1);
        processing.setVectorError(null);
        processing.setUpdateTime(new Date());
        knowledgeDocService.updateKnowledgeDocByDocId(processing, docId);
        try {
            List<String> chunks = TextChunker.split(doc.getContent(), CHUNK_SIZE);
            if (chunks.isEmpty()) {
                throw new BusinessException("文档分块结果为空");
            }
            knowledgeVectorComponent.deleteChunks(docId, Math.max(oldCount, chunks.size()));
            knowledgeVectorComponent.saveChunks(docId, doc.getTitle(), doc.getStage(),
                    doc.getKnowledgePointId(), doc.getDifficulty(), chunks);
            KnowledgeDoc done = new KnowledgeDoc();
            done.setVectorStatus(2);
            done.setVectorError(null);
            done.setChunkCount(chunks.size());
            done.setUpdateTime(new Date());
            knowledgeDocService.updateKnowledgeDocByDocId(done, docId);
        } catch (Exception e) {
            KnowledgeDoc failed = new KnowledgeDoc();
            failed.setVectorStatus(3);
            failed.setVectorError(e.getMessage() == null ? "向量化失败" : e.getMessage());
            failed.setUpdateTime(new Date());
            knowledgeDocService.updateKnowledgeDocByDocId(failed, docId);
            throw new BusinessException("向量化失败: " + failed.getVectorError());
        }
    }

    public List<KnowledgeSearchResultVO> searchTest(KnowledgeSearchTestRequest request) {
        if (request == null || StringTools.isEmpty(request.getQuestion())) {
            throw new BusinessException("请输入测试问题");
        }
        int topK = request.getTopK() == null ? 10 : Math.min(Math.max(request.getTopK(), 1), 50);
        double threshold = request.getThreshold() == null ? 0.5 : request.getThreshold();
        List<Document> vectorHits = knowledgeVectorComponent.search(
                request.getQuestion(), request.getStage(), request.getKnowledgePointId(),
                request.getDifficulty(), topK, threshold);
        if (vectorHits != null && !vectorHits.isEmpty()) {
            return vectorHits.stream().map(this::toVectorResult).toList();
        }
        return keywordSearch(request, topK);
    }

    private KnowledgeSearchResultVO toVectorResult(Document document) {
        KnowledgeSearchResultVO vo = new KnowledgeSearchResultVO();
        Map<String, Object> metadata = document.getMetadata();
        vo.setDocId(asString(metadata.get("docId")));
        vo.setTitle(asString(metadata.get("title")));
        vo.setStage(asString(metadata.get("stage")));
        vo.setKnowledgePointId(asString(metadata.get("knowledgePointId")));
        vo.setDifficulty(asInteger(metadata.get("difficulty")));
        vo.setChunkIndex(asInteger(metadata.get("chunkIndex")));
        vo.setContent(document.getText());
        vo.setScore(document.getScore());
        vo.setSearchMode("vector");
        return vo;
    }

    private List<KnowledgeSearchResultVO> keywordSearch(KnowledgeSearchTestRequest request, int topK) {
        KnowledgeDocQuery query = new KnowledgeDocQuery();
        query.setStage(request.getStage());
        query.setKnowledgePointId(request.getKnowledgePointId());
        query.setDifficulty(request.getDifficulty());
        query.setContentFuzzy(request.getQuestion());
        query.setStatus(1);
        List<KnowledgeDoc> docs = knowledgeDocService.findListByParam(query);
        List<KnowledgeSearchResultVO> results = new ArrayList<>();
        String lowerQuery = request.getQuestion().toLowerCase();
        for (KnowledgeDoc doc : docs) {
            List<String> chunks = TextChunker.split(doc.getContent(), CHUNK_SIZE);
            for (int i = 0; i < chunks.size(); i++) {
                String chunk = chunks.get(i);
                int hits = countHits(chunk.toLowerCase(), lowerQuery);
                if (hits <= 0 && doc.getTitle() != null
                        && !doc.getTitle().toLowerCase().contains(lowerQuery)) {
                    continue;
                }
                double score = Math.min(1.0, 0.5 + hits * 0.1
                        + (doc.getTitle() != null && doc.getTitle().toLowerCase().contains(lowerQuery) ? 0.2 : 0));
                KnowledgeSearchResultVO vo = new KnowledgeSearchResultVO();
                vo.setDocId(doc.getDocId());
                vo.setTitle(doc.getTitle());
                vo.setStage(doc.getStage());
                vo.setKnowledgePointId(doc.getKnowledgePointId());
                vo.setDifficulty(doc.getDifficulty());
                vo.setChunkIndex(i);
                vo.setContent(chunk);
                vo.setScore(score);
                vo.setSearchMode("keyword");
                results.add(vo);
            }
        }
        results.sort(Comparator.comparing(KnowledgeSearchResultVO::getScore).reversed());
        return results.stream().limit(topK).toList();
    }

    private int countHits(String text, String query) {
        if (text == null || query == null || query.isBlank()) {
            return 0;
        }
        int count = 0;
        int idx = 0;
        while ((idx = text.indexOf(query, idx)) >= 0) {
            count++;
            idx += query.length();
        }
        return count;
    }

    private void ensureKnowledgePoint(String pointId, String name, String stage, int difficulty) {
        KnowledgePoint exist = knowledgePointService.getKnowledgePointByKnowledgePointId(pointId);
        if (exist != null) {
            return;
        }
        KnowledgePoint point = new KnowledgePoint();
        point.setKnowledgePointId(pointId);
        point.setName(name);
        point.setStage(stage);
        point.setSubject("AI");
        point.setDifficulty(difficulty);
        point.setSort(0);
        point.setStatus(1);
        Date now = new Date();
        point.setCreateTime(now);
        point.setUpdateTime(now);
        knowledgePointService.add(point);
    }

    private Map<String, String> parseFrontmatter(String content) {
        Map<String, String> meta = new LinkedHashMap<>();
        Matcher matcher = FRONTMATTER_PATTERN.matcher(content);
        if (!matcher.find()) {
            return meta;
        }
        String block = matcher.group(1);
        for (String line : block.split("\n")) {
            int idx = line.indexOf(':');
            if (idx <= 0) {
                continue;
            }
            String key = line.substring(0, idx).trim();
            String value = line.substring(idx + 1).trim();
            if (value.startsWith("[") && value.endsWith("]")) {
                value = value.substring(1, value.length() - 1).trim();
            }
            meta.put(key, value);
        }
        return meta;
    }

    private String normalizeStage(String stage) {
        if (stage == null) {
            return "JUNIOR";
        }
        String upper = stage.trim().toUpperCase().replace("-", "_");
        if (upper.startsWith("PRIMARY_LOW") || upper.startsWith("小学低")) {
            return "PRIMARY_LOW";
        }
        if (upper.startsWith("PRIMARY_HIGH") || upper.startsWith("小学高")) {
            return "PRIMARY_HIGH";
        }
        if (upper.startsWith("JUNIOR") || upper.startsWith("初中")) {
            return "JUNIOR";
        }
        if (upper.startsWith("SENIOR") || upper.startsWith("高中")) {
            return "SENIOR";
        }
        return upper;
    }

    private String guessStage(String relativePath) {
        String lower = relativePath.toLowerCase();
        if (lower.contains("primary-low") || lower.contains("小学低")) {
            return "PRIMARY_LOW";
        }
        if (lower.contains("primary-high") || lower.contains("小学高")) {
            return "PRIMARY_HIGH";
        }
        if (lower.contains("junior") || lower.contains("初中")) {
            return "JUNIOR";
        }
        if (lower.contains("senior") || lower.contains("高中")) {
            return "SENIOR";
        }
        return "JUNIOR";
    }

    private void validateDoc(KnowledgeDoc bean) {
        if (StringTools.isEmpty(bean.getTitle()) || StringTools.isEmpty(bean.getStage())
                || StringTools.isEmpty(bean.getKnowledgePointId()) || StringTools.isEmpty(bean.getContent())) {
            throw new BusinessException("标题、学段、知识点和正文不能为空");
        }
        if (bean.getDifficulty() == null || bean.getDifficulty() < 1 || bean.getDifficulty() > 3) {
            throw new BusinessException("难度必须在 1-3 之间");
        }
    }

    private void safeDeleteChunks(String docId, int count) {
        try {
            knowledgeVectorComponent.deleteChunks(docId, Math.max(count, 200));
        } catch (Exception ignored) {
            // ES 不可用时仍允许删除文档记录
        }
    }

    private String firstNonBlank(String a, String b) {
        return a != null && !a.isBlank() ? a.trim() : (b == null ? "" : b.trim());
    }

    private String stripExtension(String name) {
        int idx = name.lastIndexOf('.');
        return idx > 0 ? name.substring(0, idx) : name;
    }

    private int parseIntSafe(String value, int defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Integer asInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        return parseIntSafe(String.valueOf(value), 0);
    }
}
