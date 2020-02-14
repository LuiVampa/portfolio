package com.portfolio.service;

import com.portfolio.model.app.request.PortfolioInfoRequest;
import com.portfolio.model.app.response.PortfolioInfoResponse;
import org.springframework.http.ResponseEntity;

/**
 * Created by Grebenkov.Andrey
 * <p>
 * Service calculate portfolio cost and stocks distribution by sectors.
 */
public interface PortfolioInfoService {

    /**
     * Calculate portfolio info by incoming request.
     *
     * @param request sportfolio stocks with volume.
     * @return portfolio cost and stocks distribution by sectors.
     */
    ResponseEntity<PortfolioInfoResponse> getPortfolioInfo(PortfolioInfoRequest request);
}
