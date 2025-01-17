package com.kaiasia.app.service.utility.model.response;

import com.kaiasia.app.service.utility.model.validation.SuccessGroup;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class BaseResponse {
    @NotBlank(message = "Response code is required", groups = SuccessGroup.class)
    private String responseCode;

    private String code;

    private String desc;
}
