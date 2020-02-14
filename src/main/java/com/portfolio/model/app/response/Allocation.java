package com.portfolio.model.app.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Created by Grebenkov.Andrey
 */
@Data
@Builder
public class Allocation {
    private String sector;
    private BigDecimal assetValue;
    private BigDecimal proportion;
}
