package com.playbit.backend.mission;


import com.playbit.backend.room.Category;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Content {

    // 공부
    STUDY_1(Category.STUDY, "30분 집중 공부하기"),
    STUDY_2(Category.STUDY, "책 5페이지 읽기"),
    STUDY_3(Category.STUDY, "단어 10개 암기하기"),
    STUDY_4(Category.STUDY, "복습 노트 정리"),
    STUDY_5(Category.STUDY, "문제집 1장 풀기"),
    STUDY_6(Category.STUDY, "인강 1강 듣기"),
    STUDY_7(Category.STUDY, "오답노트 작성"),
    STUDY_8(Category.STUDY, "다음날 계획 세우기"),
    STUDY_9(Category.STUDY, "스터디 인증샷 찍기"),

    // 운동
    WORKOUT_1(Category.WORKOUT, "물 500mL 마시기"),
    WORKOUT_2(Category.WORKOUT, "팔굽혀펴기 10회 하기"),
    WORKOUT_3(Category.WORKOUT, "스쿼트 15회 하기"),
    WORKOUT_4(Category.WORKOUT, "제자리 뛰기 1분"),
    WORKOUT_5(Category.WORKOUT, "스트레칭 5분 하기"),
    WORKOUT_6(Category.WORKOUT, "계단 2층 오르기"),
    WORKOUT_7(Category.WORKOUT, "산책 10분 하기"),
    WORKOUT_8(Category.WORKOUT, "목·어깨 스트레칭 하기"),
    WORKOUT_9(Category.WORKOUT, "플랭크 30초 하기"),

    // 건강
    HEALTH_1(Category.HEALTH, "비타민 챙겨 먹기"),
    HEALTH_2(Category.HEALTH, "물 한 컵 마시기"),
    HEALTH_3(Category.HEALTH, "눈 휴식 5분 하기"),
    HEALTH_4(Category.HEALTH, "바른 자세 유지하기"),
    HEALTH_5(Category.HEALTH, "양치하기"),
    HEALTH_6(Category.HEALTH, "과일 한 조각 먹기"),
    HEALTH_7(Category.HEALTH, "심호흡 10번 하기"),
    HEALTH_8(Category.HEALTH, "일어나서 몸 풀기"),
    HEALTH_9(Category.HEALTH, "카페인 대신 물 마시기"),

    // 취미
    HOBBY_1(Category.HOBBY, "좋아하는 음악 1곡 듣기"),
    HOBBY_2(Category.HOBBY, "사진 한 장 찍기"),
    HOBBY_3(Category.HOBBY, "낙서 5분 하기"),
    HOBBY_4(Category.HOBBY, "좋아하는 영상 10분 보기"),
    HOBBY_5(Category.HOBBY, "책 5페이지 읽기"),
    HOBBY_6(Category.HOBBY, "새로운 노래 찾아보기"),
    HOBBY_7(Category.HOBBY, "퍼즐 한 판 하기"),
    HOBBY_8(Category.HOBBY, "취미 활동 10분 하기"),
    HOBBY_9(Category.HOBBY, "오늘 기분 한 줄 적기"),

    // 일상생활
    DAILYLIFE_1(Category.DAILYLIFE, "침대 정리하기"),
    DAILYLIFE_2(Category.DAILYLIFE, "책상 정리 5분 하기"),
    DAILYLIFE_3(Category.DAILYLIFE, "쓰레기 버리기"),
    DAILYLIFE_4(Category.DAILYLIFE, "창문 열어 환기하기"),
    DAILYLIFE_5(Category.DAILYLIFE, "컵 하나 설거지하기"),
    DAILYLIFE_6(Category.DAILYLIFE, "오늘 할 일 3개 적기"),
    DAILYLIFE_7(Category.DAILYLIFE, "방 한 곳 정리하기"),
    DAILYLIFE_8(Category.DAILYLIFE, "휴대폰 화면 닦기"),
    DAILYLIFE_9(Category.DAILYLIFE, "가방 정리하기");


    private final Category category;
    private final String description;
}
