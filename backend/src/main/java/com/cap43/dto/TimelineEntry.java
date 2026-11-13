package com.cap43.dto;

import java.time.LocalDateTime;

public record TimelineEntry(LocalDateTime at, String status) {}
