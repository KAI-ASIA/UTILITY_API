package com.kaiasia.app.service.utility.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kaiasia.app.service.utility.model.validation.GetBankInRequired;
import lombok.*;
import javax.validation.constraints.*;

/**
 * Class này dùng để định nghĩa dữ liệu cần gửi tới FundsTransfer và cũng có thể gửi tới T2405
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetBanksIn {
    @NotBlank(message = "Authentication type is required", groups = GetBankInRequired.class)
    private String authenType;

    private String bankCode;
}
