package com.github.open.courier.example.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class DarkAddDTO {

    private String env;

    private String testCase;

    private String uniqueId;

    private String addReturn;

}
