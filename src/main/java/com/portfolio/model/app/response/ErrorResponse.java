package com.portfolio.model.app.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by Grebenkov.Andrey
 */
@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ErrorResponse extends PortfolioInfoResponse {
    private String message;
}
