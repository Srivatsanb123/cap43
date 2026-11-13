package com.cap43.common;

public final class PiiMasker {

    private PiiMasker() {}

    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "***";
        return email.charAt(0) + "***" + email.substring(email.indexOf('@'));
    }

    public static String maskPhone(String phone) {
        if (phone == null || phone.length() <= 4) return "****";
        return "*".repeat(phone.length() - 4) + phone.substring(phone.length() - 4);
    }
}
