package com.kaiasia.app.service.utility.service;

import com.kaiasia.app.core.utils.GetErrorUtils;
import com.kaiasia.app.register.KaiMethod;
import com.kaiasia.app.register.KaiService;
import com.kaiasia.app.register.Register;
import com.kaiasia.app.service.utility.exception.ExceptionHandler;
import com.kaiasia.app.service.utility.model.response.BaseResponse;
import com.kaiasia.app.service.utility.model.response.FundsTransferOut;
import com.kaiasia.app.service.utility.model.request.GetBanksIn;
import com.kaiasia.app.service.utility.model.validation.FundsTransferOptional;
import com.kaiasia.app.service.utility.model.validation.SuccessGroup;
import com.kaiasia.app.service.utility.utils.ObjectAndJsonUtils;
import com.kaiasia.app.service.utility.utils.ServiceUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ms.apiclient.model.*;
import ms.apiclient.t24util.T24FundTransferResponse;
import ms.apiclient.t24util.T24Request;
import ms.apiclient.t24util.T24UtilClient;

@KaiService
@Slf4j
@RequiredArgsConstructor
public class GetBanksService {
    private final GetErrorUtils apiErrorUtils;
    private final ExceptionHandler exceptionHandler;
    private final T24UtilClient t24UtilClient;

    @KaiMethod(name = "FTInsideService", type = Register.VALIDATE)
    public ApiError validate(ApiRequest req) {
        return ServiceUtils.validate(req, GetBanksIn.class, apiErrorUtils, "TRANSACTION", FundsTransferOptional.class);
    }

    @KaiMethod(name = "FTInsideService")
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

            // Call T2405 api
                T24FundTransferResponse t2405Response = t24UtilClient.fundTransfer(location,
                        T24Request.builder()
                                  .bankId(requestData.getBankCode())
                                  .build(),
                        request.getHeader());

            log.warn("#{}{}", t2405Response.getTransactionNO(), t2405Response.getResponseCode());

            error = t2405Response.getError();
            if (error != null) {
                log.error("#{}:{}", location + "#After call T2405", error);
                response.setError(error);
                return response;
            }

            // Kiểm tra kết quả trả về đủ field không.
            BaseResponse validateT2505Error = ServiceUtils.validate(ObjectAndJsonUtils.fromObject(t2405Response, FundsTransferOut.class), SuccessGroup.class);
            if (!validateT2505Error.getCode().equals(ApiError.OK_CODE)) {
                log.error("#{}:{}", location + "#After call T2405", validateT2505Error);
                response.setError(new ApiError(validateT2505Error.getCode(), validateT2505Error.getDesc()));
                return response;
            }

            header.setReqType("RESPONSE");
            body.put("transaction", t2405Response);
            response.setBody(body);
            return response;
        }, req, "#GetBanksService/" + "/" + System.currentTimeMillis());
    }
}
