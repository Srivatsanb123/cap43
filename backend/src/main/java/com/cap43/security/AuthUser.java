package com.cap43.security;


public record AuthUser(Integer userId, String role, Integer tenantId, Integer vendorId) {}
