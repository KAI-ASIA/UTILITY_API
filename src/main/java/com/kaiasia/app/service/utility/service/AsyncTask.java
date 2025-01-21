package com.kaiasia.app.service.utility.service;

import com.kaiasia.app.service.utility.utils.ObjectAndJsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ms.apiclient.t24util.Bank;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class AsyncTask {
    @Value("${spring.redis.time-to-live}")
    private long timeToLive;
    private final RedisTemplate<String, String> redisTemplate;

    @Async("asyncExecutor")
    public void asyncSaveToRedis(List<Bank> bankList, String key) {
        log.warn("{}", "#Begin save to redis");
        try {
            bankList.forEach(bank ->
                    redisTemplate.opsForHash()
                                 .put(key, bank.getBankCode(), ObjectAndJsonUtils.toJson(bank))
            );
            redisTemplate.expire(key, timeToLive, TimeUnit.MINUTES);
            log.info("Successfully saved bank list to Redis.");
        } catch (Exception e) {
            log.error("Failed to save bank list to Redis. Error: {}", e.getMessage(), e);
        }
    }
}
