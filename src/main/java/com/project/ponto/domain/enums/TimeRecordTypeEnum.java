package com.project.ponto.domain.enums;

import java.util.EnumSet;
import java.util.Set;

public enum TimeRecordTypeEnum {

    CLOCK_IN,
    LUNCH_OUT,
    LUNCH_IN,
    CLOCK_OUT;

    private static final Set<TimeRecordTypeEnum> NEXT_AFTER_CLOCK_IN = Set.of(LUNCH_OUT, CLOCK_OUT);
    private static final Set<TimeRecordTypeEnum> NEXT_AFTER_LUNCH_OUT = Set.of(LUNCH_IN);
    private static final Set<TimeRecordTypeEnum> NEXT_AFTER_LUNCH_IN = Set.of(CLOCK_OUT);
    private static final Set<TimeRecordTypeEnum> NEXT_AFTER_CLOCK_OUT = Set.of(CLOCK_IN);

    public Set<TimeRecordTypeEnum> allowedNextTypes() {
        return switch (this) {
            case CLOCK_IN  -> NEXT_AFTER_CLOCK_IN;
            case LUNCH_OUT -> NEXT_AFTER_LUNCH_OUT;
            case LUNCH_IN  -> NEXT_AFTER_LUNCH_IN;
            case CLOCK_OUT -> NEXT_AFTER_CLOCK_OUT;
        };
    }

    public boolean canBeFollowedBy(TimeRecordTypeEnum nextType) {
        return allowedNextTypes().contains(nextType);
    }

    public boolean canStartDay() {
        return this == CLOCK_IN;
    }
}