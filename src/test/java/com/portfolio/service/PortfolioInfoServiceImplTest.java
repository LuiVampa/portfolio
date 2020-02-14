package com.portfolio.service;

import com.portfolio.dao.PortfolioInfoDao;
import com.portfolio.model.app.request.PortfolioInfoRequest;
import com.portfolio.model.app.response.Allocation;
import com.portfolio.model.app.response.ErrorResponse;
import com.portfolio.model.app.response.PortfolioInfoResponse;
import com.portfolio.model.app.response.SuccessResponse;
import org.codehaus.jackson.JsonLocation;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * Created by Grebenkov.Andrey
 */
@ExtendWith(MockitoExtension.class)
class PortfolioInfoServiceImplTest {

    private ResourceLoader resourceLoader = new DefaultResourceLoader();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Mock
    private PortfolioInfoDao doa;
    @InjectMocks
    private PortfolioInfoServiceImpl service;

    @Test
    void getPortfolioInfoTest_emptyPortfolio() throws IOException {
        final PortfolioInfoRequest request = createRequest("EmptyPortfolio.json");

        final ResponseEntity<PortfolioInfoResponse> portfolioInfo = service.getPortfolioInfo(request);

        checkErrorResponse(portfolioInfo, HttpStatus.BAD_REQUEST, "Portfolio is empty.");
    }

    @Test
    void getPortfolioInfoTest_duplicatedSymbols() throws IOException {
        final PortfolioInfoRequest request = createRequest("DuplicatedSymbols.json");

        final ResponseEntity<PortfolioInfoResponse> portfolioInfo = service.getPortfolioInfo(request);

        checkErrorResponse(portfolioInfo, HttpStatus.BAD_REQUEST, "Duplicated symbols.");
    }

    @Test
    void getPortfolioInfoTest_jsonParseException() throws IOException {
        final PortfolioInfoRequest request = createRequest("OkJson.json");
        when(doa.getSymbolToSector(any())).thenThrow(new JsonParseException("Bad json.", JsonLocation.NA));

        final ResponseEntity<PortfolioInfoResponse> portfolioInfo = service.getPortfolioInfo(request);

        checkErrorResponse(
                portfolioInfo,
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Bad json.\n at [Source: N/A; line: -1, column: -1]"
        );
    }

    @Test
    void getPortfolioInfoTest_ioException() throws IOException {
        final PortfolioInfoRequest request = createRequest("OkJson.json");
        when(doa.getSymbolToSector(any())).thenThrow(new IOException("Connection exception."));

        final ResponseEntity<PortfolioInfoResponse> portfolioInfo = service.getPortfolioInfo(request);

        checkErrorResponse(portfolioInfo, HttpStatus.SERVICE_UNAVAILABLE, "Connection exception.");
    }

    @Test
    void getPortfolioInfoTest_unsupportedSymbols() throws IOException {
        final PortfolioInfoRequest request = createRequest("UnsupportedJson.json");
        final HashMap<String, String> doaResult = new HashMap<>();
        doaResult.put("AAPL", "Tech");
        when(doa.getSymbolToSector(any())).thenReturn(doaResult);

        final ResponseEntity<PortfolioInfoResponse> portfolioInfo = service.getPortfolioInfo(request);

        checkErrorResponse(portfolioInfo, HttpStatus.BAD_REQUEST, "Portfolio has unsupported symbols.");
    }

    @Test
    void getPortfolioInfoTest_ok() throws IOException {
        final PortfolioInfoRequest request = createRequest("OkJson.json");
        final HashMap<String, String> symbolToSector = new HashMap<>();
        symbolToSector.put("AAPL", "Tech");
        symbolToSector.put("FB", "Sy");
        when(doa.getSymbolToSector(any())).thenReturn(symbolToSector);
        final HashMap<String, BigDecimal> symbolToLatestPrice = new HashMap<>();
        symbolToLatestPrice.put("AAPL", BigDecimal.valueOf(50));
        symbolToLatestPrice.put("FB", BigDecimal.valueOf(50));
        when(doa.getSymbolsToRate(any())).thenReturn(symbolToLatestPrice);
        ReflectionTestUtils.setField(service, "scale", 3);

        final ResponseEntity<PortfolioInfoResponse> portfolioInfo = service.getPortfolioInfo(request);

        checkSuccessResponse(portfolioInfo, createSuccessResponse());
    }

    private PortfolioInfoRequest createRequest(String fileName) throws IOException {
        return OBJECT_MAPPER.readValue(
                resourceLoader.getResource(String.format("request/%s", fileName)).getFile(),
                PortfolioInfoRequest.class
        );
    }

    private void checkErrorResponse(
            ResponseEntity<PortfolioInfoResponse> portfolioInfo,
            HttpStatus status,
            String errorMsg
    ) {
        Assertions.assertEquals(status, portfolioInfo.getStatusCode());
        final PortfolioInfoResponse responseBody = portfolioInfo.getBody();
        Assertions.assertNotNull(responseBody);
        Assertions.assertEquals(ErrorResponse.class, responseBody.getClass());
        Assertions.assertEquals(errorMsg, ((ErrorResponse) responseBody).getMessage());
    }

    private void checkSuccessResponse(
            ResponseEntity<PortfolioInfoResponse> portfolioInfo,
            SuccessResponse expectedResponse
    ) {
        Assertions.assertEquals(HttpStatus.OK, portfolioInfo.getStatusCode());
        final PortfolioInfoResponse responseBody = portfolioInfo.getBody();
        Assertions.assertNotNull(responseBody);
        Assertions.assertEquals(SuccessResponse.class, responseBody.getClass());
        final SuccessResponse response = (SuccessResponse) responseBody;
        Assertions.assertEquals(expectedResponse.getValue(), response.getValue());
        Assertions.assertEquals(expectedResponse.getAllocations(), response.getAllocations());
    }

    private SuccessResponse createSuccessResponse() {
        return new SuccessResponse(
                BigDecimal.valueOf(100),
                Arrays.asList(
                        createAllocation("Tech"),
                        createAllocation("Sy")
                )
        );
    }

    private Allocation createAllocation(String sector) {

        return Allocation.builder()
                         .sector(sector)
                         .assetValue(BigDecimal.valueOf(50))
                         .proportion(BigDecimal.valueOf(0.5).setScale(3))
                         .build();
    }
}