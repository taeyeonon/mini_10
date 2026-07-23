package com.mycom.myapp.schedule;

import com.mycom.myapp.common.InvalidOperationException;

public class ScheduleConflictException extends InvalidOperationException {

    public ScheduleConflictException(String message) {
        super(message);
    }
}
