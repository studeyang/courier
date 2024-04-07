package com.github.open.courier.core.transport;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessagePageDTO {

    private List<?> data;
    private long total;
    private int pageNum;
    private int pageSize;
}
