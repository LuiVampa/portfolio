package com.portfolio.dao;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

/**
 * Created by Grebenkov.Andrey
 * <p>
 * This interface provides methods for retrieving stocks data.
 */
public interface PortfolioInfoDao {

    /**
     * Retrieves latest price for all stacks.
     *
     * @param symbols stock symbols.
     * @return stock symbol to latest price map.
     */
    Map<String, BigDecimal> getSymbolsToRate(Set<String> symbols) throws IOException;

    /**
     * Retrieves all current supported symbols with its sector.
     *
     * @param symbols stocks symbols from request.
     * @return stock symbol to sector map.
     */
    Map<String, String> getSymbolToSector(Set<String> symbols) throws IOException;
}
