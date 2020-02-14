package com.portfolio.model.dao;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Quote {
    String symbol;
    BigDecimal latestPrice;
}
