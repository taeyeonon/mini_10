package com.mycom.myapp.schedule;

import com.mycom.myapp.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "trainer_schedule",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_schedule_template_time",
                columnNames = {"template_id", "start_time", "end_time"}
        )
)
public class TrainerSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trainer_id", nullable = false)
    private User trainer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private TrainerScheduleTemplate template;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Column(nullable = false)
    private int capacity;

    @Column(nullable = false)
    private int reservedCount;

    @Column(nullable = false)
    private boolean archived;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ScheduleStatus status;

    @Version
    private long version;

    protected TrainerSchedule() {
    }

    public TrainerSchedule(User trainer, LocalDateTime startTime, LocalDateTime endTime, int capacity) {
        this(trainer, null, startTime, endTime, capacity);
    }

    public TrainerSchedule(
            User trainer,
            TrainerScheduleTemplate template,
            LocalDateTime startTime,
            LocalDateTime endTime,
            int capacity
    ) {
        this.trainer = trainer;
        this.template = template;
        this.startTime = startTime;
        this.endTime = endTime;
        this.capacity = capacity;
        this.reservedCount = 0;
        this.archived = false;
        this.status = ScheduleStatus.OPEN;
    }

    public void update(LocalDateTime startTime, LocalDateTime endTime, int capacity) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.capacity = capacity;
    }

    public void cancel() {
        this.status = ScheduleStatus.CANCELLED;
    }

    public void restore() {
        this.status = ScheduleStatus.OPEN;
        this.archived = false;
    }

    public void archive() {
        this.archived = true;
    }

    public void increaseReservedCount() {
        if (isFull()) {
            throw new IllegalStateException("수업 정원이 모두 찼습니다.");
        }
        this.reservedCount++;
    }

    public void decreaseReservedCount() {
        if (reservedCount > 0) {
            this.reservedCount--;
        }
    }

    public boolean isFull() {
        return reservedCount >= capacity;
    }

    public int getAvailableCount() {
        return Math.max(capacity - reservedCount, 0);
    }

    public Long getId() { return id; }
    public User getTrainer() { return trainer; }
    public TrainerScheduleTemplate getTemplate() { return template; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public int getCapacity() { return capacity; }
    public int getReservedCount() { return reservedCount; }
    public ScheduleStatus getStatus() { return status; }
    public boolean isArchived() { return archived; }
}
