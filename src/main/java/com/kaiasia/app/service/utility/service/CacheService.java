package com.kaiasia.app.service.utility.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ms.apiclient.model.ApiHeader;
import ms.apiclient.t24util.T24BankListResponse;
import ms.apiclient.t24util.T24Request;
import ms.apiclient.t24util.T24UtilClient;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CacheService {
    private final T24UtilClient t24UtilClient;

    @Cacheable(value = "listBanks", key = "T(org.springframework.util.StringUtils).hasText(#bankCode) ? #bankCode : 'all'")
    public T24BankListResponse callT2408(String bankCode, String location, ApiHeader header) {
        log.info("Calling T2408 API for bankCode: {}", bankCode);
        return t24UtilClient.getBankList(location,
                T24Request.builder()
                          .bankCode(bankCode)
                          .build(),
                header);
    }
}
