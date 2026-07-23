package com.mycom.myapp.schedule;

import com.mycom.myapp.common.InvalidOperationException;

public class DuplicateRecurringScheduleException extends InvalidOperationException {

    public DuplicateRecurringScheduleException(String message) {
        super(message);
    }
}
