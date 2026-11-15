package com.cap43.common;

import com.cap43.entity.Issue;
import java.time.LocalDateTime;

public final class SlaUtil {

    private SlaUtil() {}

    
    public static boolean isOverdue(Issue i, LocalDateTime now) {
        return !"RESOLVED".equals(i.getStatus())
                && i.getSlaDueAt() != null
                && now.isAfter(i.getSlaDueAt());
    }
}
