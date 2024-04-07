package com.github.open.courier.example.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class DarkDTO {

    private String env;

    private String testCase;

    private String uniqueId;

}
