package com.cap43.dto;

import java.math.BigDecimal;
import java.util.List;

public record LedgerView(List<CostEntryView> entries, BigDecimal netTotal) {}
