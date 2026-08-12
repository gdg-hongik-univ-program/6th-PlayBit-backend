package com.playbit.backend.member;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
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
    private java.time.LocalDate lastMissionSuccessDate;

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    public void incrementMissionSuccess() {
        this.totalMissionSuccess++;
    }

    public void updateMissionStreak() {
        java.time.LocalDate today = java.time.LocalDate.now();
        if (lastMissionSuccessDate != null && lastMissionSuccessDate.plusDays(1).isEqual(today)) {
            this.consecutiveMissionStreak++;
        } else {
            this.consecutiveMissionStreak = 1;
        }
        this.lastMissionSuccessDate = today;
    }

}