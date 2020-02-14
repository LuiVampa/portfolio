package com.portfolio.model.app.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by Grebenkov.Andrey
 */
@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SuccessResponse extends PortfolioInfoResponse {
    private BigDecimal value;
    private List<Allocation> allocations;
}
