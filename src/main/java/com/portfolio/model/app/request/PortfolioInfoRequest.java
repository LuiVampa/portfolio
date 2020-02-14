package com.portfolio.model.app.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PortfolioInfoRequest {
    @NotNull
    @Valid
    Set<Stock> stocks;
}
