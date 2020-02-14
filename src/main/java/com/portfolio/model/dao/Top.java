package com.portfolio.model.dao;

import lombok.Data;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Top {
    String symbol;
    String sector;
}
