package com.mycom.myapp.schedule;

import java.util.List;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TrainerScheduleTemplateRepository
        extends JpaRepository<TrainerScheduleTemplate, Long> {

    List<TrainerScheduleTemplate> findAllByTrainerIdOrderByDayOfWeekAscStartTimeAsc(Long trainerId);

    @Query("""
            select count(template)
            from TrainerScheduleTemplate template
            where template.trainer.id = :trainerId
              and template.dayOfWeek = :dayOfWeek
              and template.active = true
              and template.startTime < :newEndTime
              and template.endTime > :newStartTime
              and (:newEffectiveTo is null or template.effectiveFrom <= :newEffectiveTo)
              and (template.effectiveTo is null or template.effectiveTo >= :newEffectiveFrom)
            """)
    long countOverlappingActiveTemplates(
            @Param("trainerId") Long trainerId,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("newStartTime") LocalTime newStartTime,
            @Param("newEndTime") LocalTime newEndTime,
            @Param("newEffectiveFrom") LocalDate newEffectiveFrom,
            @Param("newEffectiveTo") LocalDate newEffectiveTo
    );
}
