package com.portfolio.controller;

import com.portfolio.model.app.request.PortfolioInfoRequest;
import com.portfolio.model.app.response.PortfolioInfoResponse;
import com.portfolio.service.PortfolioInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * Created by Grebenkov.Andrey
 */
@RestController
@RequestMapping("/portfolio")
public class PortfolioInfoController {

    private PortfolioInfoService portfolioInfoService;

    @Autowired
    public PortfolioInfoController(PortfolioInfoService portfolioInfoService) {
        this.portfolioInfoService = portfolioInfoService;
    }

    @PostMapping("/info")
    public ResponseEntity<PortfolioInfoResponse> getPortfolioInfo(@Valid @RequestBody PortfolioInfoRequest request) {
        return portfolioInfoService.getPortfolioInfo(request);
    }
}
