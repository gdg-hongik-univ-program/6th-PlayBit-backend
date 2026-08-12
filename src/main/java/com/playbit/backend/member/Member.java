package com.playbit.backend.member;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Member {

    public Member(String memberUuid) {
        this.memberUuid = memberUuid;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    @Column(unique = true)
    private String memberUuid;

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
