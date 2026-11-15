package com.cap43.dto;

public record LoginResponse(String token, String role, String fullName, long expiresInMinutes) {}
