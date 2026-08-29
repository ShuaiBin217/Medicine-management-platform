package com.wms.controller;

import com.wms.ai.KnowledgeBaseService;
import com.wms.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/knowledge")
public class KnowledgeBaseController {

    @Autowired
    private KnowledgeBaseService knowledgeBaseService;

    @PostMapping("/import/file")
    public Result importFile(@RequestParam String filePath) {
        try {
            int segments = knowledgeBaseService.importDocument(filePath);
            Map<String, Object> data = new HashMap<>();
            data.put("filePath", filePath);
            data.put("segments", segments);
            return Result.suc(data);
        } catch (Exception e) {
            return Result.fail();
        }
    }

    @PostMapping("/import/text")
    public Result importText(@RequestBody Map<String, String> requestBody) {
        String title = requestBody.get("title");
        String content = requestBody.get("content");
        if (title == null || content == null || content.trim().isEmpty()) {
            return Result.fail();
        }
        try {
            int segments = knowledgeBaseService.importText(content, title);
            Map<String, Object> data = new HashMap<>();
            data.put("title", title);
            data.put("segments", segments);
            return Result.suc(data);
        } catch (Exception e) {
            return Result.fail();
        }
    }

    @GetMapping("/search")
    public Result search(@RequestParam String query) {
        String result = knowledgeBaseService.search(query);
        Map<String, String> data = new HashMap<>();
        data.put("result", result);
        return Result.suc(data);
    }

    @GetMapping("/list")
    public Result list() {
        Set<String> documents = knowledgeBaseService.listDocuments();
        Map<String, Object> data = new HashMap<>();
        data.put("documents", documents);
        data.put("count", documents.size());
        data.put("enabled", knowledgeBaseService.isEnabled());
        return Result.suc(data);
    }

    @PostMapping("/reload")
    public Result reload() {
        knowledgeBaseService.loadFromDirectory();
        Map<String, Object> data = new HashMap<>();
        data.put("count", knowledgeBaseService.getDocumentCount());
        data.put("enabled", knowledgeBaseService.isEnabled());
        return Result.suc(data);
    }

    @DeleteMapping("/clear")
    public Result clear() {
        knowledgeBaseService.clearIndex();
        return Result.suc();
    }
}