package com.portfolio.dao;

import com.portfolio.model.dao.Quote;
import com.portfolio.model.dao.QuoteWrapper;
import com.portfolio.model.dao.Top;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.portfolio.Constants.COMMA;

/**
 * Created by Grebenkov.Andrey
 * <p>
 * IEX Cloud dao.
 */
@Slf4j
@Component
public class PortfolioInfoDaoImpl implements PortfolioInfoDao {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String QUOTE = "quote";

    @Value("${app.iex.rateUrl}")
    private String rateUrl;
    @Value("${app.iex.topsUrl}")
    private String topsUrl;

    @Override
    public Map<String, BigDecimal> getSymbolsToRate(Set<String> symbols) throws IOException {
        final String portfolioInfo = executeRequest(String.format(rateUrl, String.join(COMMA, symbols)));
        final List<JsonNode> quotes = OBJECT_MAPPER.readTree(portfolioInfo).findParents(QUOTE);
        final HashSet<QuoteWrapper> quoteSet = new HashSet<>();
        for (JsonNode jsonNode : quotes) {
            quoteSet.add(OBJECT_MAPPER.readValue(jsonNode, QuoteWrapper.class));
        }
        return quoteSet.stream()
                       .map(QuoteWrapper::getQuote)
                       .collect(Collectors.toMap(Quote::getSymbol, Quote::getLatestPrice));
    }

    @Override
    public Map<String, String> getSymbolToSector(Set<String> symbols) throws IOException {
        final String portfolioInfo = executeRequest(String.format(topsUrl, String.join(COMMA, symbols)));
        final Top[] tops = OBJECT_MAPPER.readValue(portfolioInfo, Top[].class);
        return Arrays.stream(tops).collect(Collectors.toMap(Top::getSymbol, Top::getSector));
    }

    private String executeRequest(String requestUrl) throws IOException {
        URLConnection connection = new URL(requestUrl).openConnection();
        return getJson(connection);
    }

    private String getJson(URLConnection connection) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            return br.lines().collect(Collectors.joining());
        }
    }
}