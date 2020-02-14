package com.portfolio.model.app.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Stock {
    @NotNull
    String symbol;
    @NotNull
    Long volume;
}
