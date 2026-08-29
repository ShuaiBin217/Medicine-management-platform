package com.wms.controller;

import com.wms.ai.WmsAiService;
import com.wms.common.CacheConstant;
import com.wms.common.Result;
import com.wms.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/ai")
public class AiChatController {

    @Autowired
    private WmsAiService wmsAiService;

    @Autowired
    private RedisService redisService;

    @PostMapping("/chat")
    public Result chat(@RequestBody Map<String, Object> requestBody) {
        String userMessage = (String) requestBody.get("message");

        if (userMessage == null || userMessage.trim().isEmpty()) {
            return Result.fail();
        }

        try {
            String cacheKey = CacheConstant.AI_CHAT_PREFIX + userMessage.hashCode();
            String cachedReply = (String) redisService.get(cacheKey);
            if (cachedReply != null) {
                Map<String, String> result = new HashMap<>();
                result.put("reply", cachedReply);
                return Result.suc(result);
            }

            String reply = wmsAiService.chat(userMessage);
            redisService.set(cacheKey, reply, CacheConstant.TTL_AI_CHAT_MINUTES, TimeUnit.MINUTES);

            Map<String, String> result = new HashMap<>();
            result.put("reply", reply);
            return Result.suc(result);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResult = new HashMap<>();
            errorResult.put("reply", "抱歉，智能助手暂时无法回答，请稍后再试。错误信息：" + e.getMessage());
            return Result.suc(errorResult);
        }
    }
}