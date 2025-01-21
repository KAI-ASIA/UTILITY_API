package com.kaiasia.app.service.utility.service;

import com.kaiasia.app.core.utils.GetErrorUtils;
import com.kaiasia.app.register.KaiMethod;
import com.kaiasia.app.register.KaiService;
import com.kaiasia.app.register.Register;
import com.kaiasia.app.service.utility.exception.ExceptionHandler;
import com.kaiasia.app.service.utility.model.request.GetBanksIn;
import com.kaiasia.app.service.utility.model.validation.GetBankInRequired;
import com.kaiasia.app.service.utility.utils.ObjectAndJsonUtils;
import com.kaiasia.app.service.utility.utils.ServiceUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ms.apiclient.model.*;
import ms.apiclient.t24util.T24BankListResponse;
import ms.apiclient.t24util.T24Request;
import ms.apiclient.t24util.T24UtilClient;
import org.springframework.cache.annotation.Cacheable;

import java.util.HashMap;
import java.util.Map;

@KaiService
@Slf4j
@RequiredArgsConstructor
public class GetBanksService {
    private final GetErrorUtils apiErrorUtils;
    private final ExceptionHandler exceptionHandler;
    private final CacheService cacheService;

    @KaiMethod(name = "KAI.API.BANKS", type = Register.VALIDATE)
    public ApiError validate(ApiRequest req) {
        return ServiceUtils.validate(req, GetBanksIn.class, apiErrorUtils, "ENQUIRY", GetBankInRequired.class);
    }

    @KaiMethod(name = "KAI.API.BANKS")
    public ApiResponse process(ApiRequest req) throws Exception {
        GetBanksIn requestData = ObjectAndJsonUtils.fromObject(req
                .getBody()
                .get("enquiry"), GetBanksIn.class);
        String location = "#GetBanksService-" + "-" + System.currentTimeMillis();

        return exceptionHandler.handle(request -> {
            ApiResponse response = new ApiResponse();
            ApiHeader header = req.getHeader();
            response.setHeader(header);
            ApiError error = new ApiError();
            ApiBody body = new ApiBody();

            // Call T2408 api
            T24BankListResponse t2408Response = cacheService.callT2408(requestData.getBankCode(), location, request.getHeader());
            log.warn("#{}", t2408Response.getBanks());

            error = t2408Response.getError();
            if (!ApiError.OK_CODE.equals(error.getCode())) {
                log.error("#{}:{}", location + "#After call T2408", error);
                response.setError(error);
                return response;
            }

            header.setReqType("RESPONSE");
            Map<String, Object> listBanks = new HashMap<>();
            listBanks.put("banks", t2408Response.getBanks());
            body.put("enquiry", listBanks);
            response.setBody(body);
            return response;
        }, req, "#GetBanksService/" + "/" + System.currentTimeMillis());
    }

}
