package com.playbit.backend.member;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "사용자 엔티티")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    @Column(nullable = false, unique = true)
    private String googleSub;

    @Column(nullable = false)
    private String email;

    @Column(unique = true)
    private String nickname;

    @Column(name = "total_mission_success")
    private int totalMissionSuccess;

    @Column(name = "consecutive_mission_streak")
    private int consecutiveMissionStreak;

    @Column(name = "last_mission_success_date")
    private LocalDate lastMissionSuccessDate;

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void incrementMissionSuccess() {
        this.totalMissionSuccess++;
    }

    public void updateMissionStreak() {
        LocalDate today = LocalDate.now();
        if (lastMissionSuccessDate != null && lastMissionSuccessDate.plusDays(1).isEqual(today)) {
            this.consecutiveMissionStreak++;
        } else {
            this.consecutiveMissionStreak = 1;
        }
        this.lastMissionSuccessDate = today;
    }
}