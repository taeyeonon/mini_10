package com.mycom.myapp.schedule;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 종료된 수업 일정의 상태를 주기적으로 COMPLETED 로 전환한다.
 * 실제 전환은 {@link TrainerScheduleService#completeEndedSchedules()} 의 단일 벌크 UPDATE 로 처리된다.
 */
@Component
@Slf4j
public class ScheduleStatusScheduler {

    private final TrainerScheduleService scheduleService;

    public ScheduleStatusScheduler(TrainerScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    /** 매 분 정각에 종료된 OPEN 수업을 COMPLETED 로 전환한다. */
    @Scheduled(cron = "0 * * * * *")
    public void completeEndedSchedules() {
        int completed = scheduleService.completeEndedSchedules();
        if (completed > 0) {
            log.info("종료된 수업 {}건을 COMPLETED 로 전환했습니다.", completed);
        }
    }
}
