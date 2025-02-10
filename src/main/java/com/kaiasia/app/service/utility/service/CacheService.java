package com.kaiasia.app.service.utility.service;

import com.kaiasia.app.service.utility.utils.ObjectAndJsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ms.apiclient.model.ApiHeader;
import ms.apiclient.t24util.Bank;
import ms.apiclient.t24util.T24BankListResponse;
import ms.apiclient.t24util.T24Request;
import ms.apiclient.t24util.T24UtilClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CacheService {
    @Value("${spring.redis.key-list-banks}")
    private String keyBanks;
    private String keyPrefix = "bank:";
    private final T24UtilClient t24UtilClient;
    private final RedisTemplate<String, String> redisTemplate;
    private final AsyncTask asyncTask;

    public T24BankListResponse beforeCallT2408(String bankCode, String location, ApiHeader header) {
        try {
            log.warn("{}", "#Begin get from redis");
            T24BankListResponse response = new T24BankListResponse();
            List<Object> banks = null;
            if ("".equals(bankCode)) {
                banks = redisTemplate.opsForHash()
                                     .values(keyBanks);
                log.warn("{}", "#Successful get from redis");
                if (!banks.isEmpty()) {
                    response.setBanks(banks.stream()
                                           .map(bank -> ObjectAndJsonUtils.fromJson(bank.toString(), Bank.class))
                                           .collect(Collectors.toList()));
                    return response;
                }
            } else {
                String detailKey = keyPrefix + bankCode;
                Object bankData = redisTemplate.opsForHash().get(detailKey, bankCode);
                if (bankData != null) {
                    banks = new ArrayList<>();
                    log.warn("{}", "#Có key redis chi tiết");
                    banks.add(bankData);
                    response.setBanks(banks.stream()
                                           .map(bank -> ObjectAndJsonUtils.fromJson(bank.toString(), Bank.class))
                                           .collect(Collectors.toList()));
                    return response;
                }
                bankData = redisTemplate.opsForHash().get(keyBanks, bankCode);
                if (bankData != null) {
                    log.warn("{}", "#Có key redis tổng");
                    banks = new ArrayList<>();
                    banks.add(bankData);
                    log.warn("{}", "#Successful get from redis");
                    response.setBanks(banks.stream()
                                           .map(bank -> ObjectAndJsonUtils.fromJson(bank.toString(), Bank.class))
                                           .collect(Collectors.toList()));
                    return response;
                }
            }
            log.warn("{}", "#Không key redis chi tiết");
            response = callT2408(bankCode, location, header);
            asyncTask.asyncSaveToRedis(response.getBanks(), bankCode.isEmpty() ? keyBanks : keyPrefix + bankCode);
            return response;
        } catch (Exception e) {
            log.error("Failed to get bank list from Redis. Error: {}", e.getMessage(), e);
            return callT2408(bankCode, location, header);
        }
    }


    public T24BankListResponse callT2408(String bankCode, String location, ApiHeader header) {
        log.info("#Calling T2408 API for bankCode: {}", bankCode);
//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        List<Bank> bankList = new ArrayList<>();
//        Bank bank1 = new Bank();
//        bank1.setBankCode("300");
//        bank1.setBankName("Seabank");
//        bank1.setStatus("ACTIVE");
//        bankList.add(bank1);
//        if("".equals(bankCode)) {
//            Bank bank2 = new Bank();
//            bank2.setBankCode("999");
//            bank2.setBankName("BIDV");
//            bank2.setStatus("ACTIVE");
//            bankList.add(bank2);
//        }
//        T24BankListResponse response = new T24BankListResponse();
        T24BankListResponse response = t24UtilClient.getBankList(location,
                T24Request.builder()
                          .bankCode(bankCode)
                          .build(),
                header);
//        response.setBanks(bankList);
        asyncTask.asyncSaveToRedis(response.getBanks(), bankCode.isEmpty() ? keyBanks : keyPrefix + bankCode);
        return response;
    }
}
