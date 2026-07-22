package com.playbit.backend.room;

import com.playbit.backend.mission.Content;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public enum Category {
    STUDY("공부"),
    WORKOUT("운동"),
    HEALTH("건강"),
    HOBBY("취미"),
    DAILYLIFE("일상생활");

    private final String description;
}
