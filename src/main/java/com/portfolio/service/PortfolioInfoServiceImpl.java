package com.portfolio.service;

import com.portfolio.dao.PortfolioInfoDao;
import com.portfolio.model.app.request.PortfolioInfoRequest;
import com.portfolio.model.app.response.Allocation;
import com.portfolio.model.app.response.ErrorResponse;
import com.portfolio.model.app.response.PortfolioInfoResponse;
import com.portfolio.model.app.response.SuccessResponse;
import com.portfolio.model.app.request.Stock;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Grebenkov.Andrey
 */
@Slf4j
@Service
public class PortfolioInfoServiceImpl implements PortfolioInfoService {

    @Value("${app.scale}")
    private Integer scale;

    private PortfolioInfoDao portfolioInfoDao;

    @Autowired
    public PortfolioInfoServiceImpl(PortfolioInfoDao portfolioInfoDao) {
        this.portfolioInfoDao = portfolioInfoDao;
    }

    @Override
    public ResponseEntity<PortfolioInfoResponse> getPortfolioInfo(PortfolioInfoRequest request) {

        if (request.getStocks().isEmpty()) {
            return new ResponseEntity<>(new ErrorResponse("Portfolio is empty."), HttpStatus.BAD_REQUEST);
        }

        final Set<String> symbols = request.getStocks()
                                           .stream()
                                           .map(Stock::getSymbol)
                                           .collect(Collectors.toSet());

        if (symbols.size() != request.getStocks().size()) {
            return new ResponseEntity<>(new ErrorResponse("Duplicated symbols."), HttpStatus.BAD_REQUEST);
        }

        final Map<String, String> symbolToSector;
        final Map<String, BigDecimal> symbolToLatestPrice;
        try {
            symbolToSector = portfolioInfoDao.getSymbolToSector(symbols);
            symbolToLatestPrice = portfolioInfoDao.getSymbolsToRate(symbols);
        } catch (JsonParseException | JsonMappingException e) {
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.SERVICE_UNAVAILABLE);
        }

        if (symbolToSector.size() != symbols.size()) {
            return new ResponseEntity<>(
                    new ErrorResponse("Portfolio has unsupported symbols."),
                    HttpStatus.BAD_REQUEST
            );
        }

        final Map<String, BigDecimal> sectorToValue = calculateSectorsValue(
                request,
                symbolToSector,
                symbolToLatestPrice
        );

        return new ResponseEntity<>(createResponse(sectorToValue), HttpStatus.OK);
    }

    private Map<String, BigDecimal> calculateSectorsValue(
            PortfolioInfoRequest request,
            Map<String, String> symbolToSector,
            Map<String, BigDecimal> symbolToLatestPrice
    ) {
        final Map<String, BigDecimal> sectorsToValue = new HashMap<>();
        request.getStocks()
               .forEach(stock -> {
                            final BigDecimal stockValue = symbolToLatestPrice.get(stock.getSymbol())
                                                                             .multiply(new BigDecimal(stock.getVolume()));
                            final String sector = symbolToSector.get(stock.getSymbol());
                            sectorsToValue.put(
                                    sector,
                                    Optional.ofNullable(sectorsToValue.get(sector))
                                            .map(sectorValue -> sectorValue.add(stockValue))
                                            .orElse(stockValue)
                            );
                        }
               );
        return sectorsToValue;
    }

    private SuccessResponse createResponse(Map<String, BigDecimal> sectorToValue) {
        final BigDecimal value = sectorToValue.values()
                                              .stream()
                                              .reduce(BigDecimal::add)
                                              .orElseThrow(NullPointerException::new);
        return new SuccessResponse(value, sectorToValue.entrySet()
                                                       .stream()
                                                       .map(sectorValueEntry -> buildAllocation(
                                                               sectorValueEntry,
                                                               value
                                                       ))
                                                       .collect(Collectors.toList()));
    }

    private Allocation buildAllocation(Map.Entry<String, BigDecimal> sectorValueEntry, BigDecimal value) {
        return Allocation.builder()
                         .sector(sectorValueEntry.getKey())
                         .assetValue(sectorValueEntry.getValue())
                         .proportion(sectorValueEntry.getValue().divide(value, scale, BigDecimal.ROUND_HALF_DOWN))
                         .build();
    }
}
