package com.playbit.backend.mission;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Content {

    STUDY_1("30분 집중 공부하기"),
    STUDY_2("책 5페이지 읽기"),
    STUDY_3("단어 10개 암기하기"),
    STUDY_4("복습 노트 정리"),
    STUDY_5("문제집 1장 풀기"),
    STUDY_6("인강 1강 듣기"),
    STUDY_7("오답노트 작성"),
    STUDY_8("다음날 계획 세우기"),
    STUDY_9("스터디 인증샷 찍기"),

    DEFAULT_MISSION("기본 미션");

    private final String description;

}
