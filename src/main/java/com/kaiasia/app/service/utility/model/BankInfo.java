package com.kaiasia.app.service.utility.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BankInfo {
    private String bankCode;
    private String bankName;
    private String status;
    private String napasId;
}
