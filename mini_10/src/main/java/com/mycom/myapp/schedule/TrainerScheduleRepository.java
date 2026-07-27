package com.mycom.myapp.schedule;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TrainerScheduleRepository extends JpaRepository<TrainerSchedule, Long> {

    /**
     * 종료 시각이 지난 OPEN 수업을 한 번의 UPDATE 로 COMPLETED 로 전환한다.
     * 엔티티를 로딩하지 않고 DB 에서 일괄 처리하며, @Version 컬럼도 함께 증가시켜(versioned)
     * 동시에 로딩된 엔티티가 있으면 낙관적 락으로 변경을 감지하도록 한다.
     *
     * @return 전환된 행 수
     */
    @Modifying(clearAutomatically = true)
    @Query("""
            update versioned TrainerSchedule s
            set s.status = com.mycom.myapp.schedule.ScheduleStatus.COMPLETED
            where s.status = com.mycom.myapp.schedule.ScheduleStatus.OPEN
              and s.endTime < :now
            """)
    int markCompletedBefore(@Param("now") LocalDateTime now);

    List<TrainerSchedule> findAllByTrainerIdAndArchivedFalseOrderByStartTimeAsc(Long trainerId);

    List<TrainerSchedule> findAllByTrainerIdAndStatusAndArchivedFalse(
            Long trainerId, ScheduleStatus status
    );

    List<TrainerSchedule> findAllByStatusAndStartTimeAfterOrderByStartTimeAsc(
            ScheduleStatus status, LocalDateTime startTime
    );

    // 관리자 수업 스케줄 화면: 트레이너 구분 없이 전체 (최신 수업 우선)
    List<TrainerSchedule> findAllByArchivedFalseOrderByStartTimeDesc();

    boolean existsByTrainerIdAndStatusNotAndStartTimeLessThanAndEndTimeGreaterThan(
            Long trainerId, ScheduleStatus status,
            LocalDateTime endTime, LocalDateTime startTime
    );

    boolean existsByTrainerIdAndStatusNotAndStartTimeLessThanAndEndTimeGreaterThanAndIdNot(
            Long trainerId, ScheduleStatus status,
            LocalDateTime endTime, LocalDateTime startTime, Long id
    );

    boolean existsByTemplateIdAndStartTimeAndEndTime(
            Long templateId, LocalDateTime startTime, LocalDateTime endTime
    );
}
