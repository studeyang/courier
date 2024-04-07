package com.github.open.courier.core.transport;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommonPageDTO {

    private List<?> data;

    private int recordsTotal;
    private int recordsFiltered;
}
