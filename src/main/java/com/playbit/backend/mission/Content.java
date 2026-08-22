package com.playbit.backend.mission;

import com.playbit.backend.room.Category;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Content {

    // 📚 1. 공부 (STUDY)
    STUDY_1(Category.STUDY, "책이나 업무 관련 서적을 20페이지 이상 읽고 인상 깊은 부분 기록하기"),
    STUDY_2(Category.STUDY, "관심 있는 분야의 아티클이나 리포트 2개를 읽고 핵심 내용 정리하기"),
    STUDY_3(Category.STUDY, "외국어 단어나 표현 20개를 공부하고 직접 예문 만들어보기"),
    STUDY_4(Category.STUDY, "업무에 활용할 수 있는 새로운 기능이나 도구 하나를 배우고 직접 사용해보기"),
    STUDY_5(Category.STUDY, "자격증 또는 자기계발 공부 한 단원을 끝내고 핵심 내용 정리하기"),
    STUDY_6(Category.STUDY, "경제·시사 콘텐츠 하나를 보고 새롭게 알게 된 내용 5가지 기록하기"),
    STUDY_7(Category.STUDY, "평소 궁금했던 주제 하나를 30분 이상 조사하고 한 페이지로 정리하기"),
    STUDY_8(Category.STUDY, "미뤄두었던 강의 하나를 수강하고 배운 내용 3가지 기록하기"),
    STUDY_9(Category.STUDY, "이번 주에 배운 내용 중 하나를 골라 다른 사람에게 설명할 수 있게 정리하기"),

    // 🏃 2. 운동 (WORKOUT)
    WORKOUT_1(Category.WORKOUT, "30분 이상 산책하거나 러닝하기"),
    WORKOUT_2(Category.WORKOUT, "전신 스트레칭을 15분 이상 하기"),
    WORKOUT_3(Category.WORKOUT, "스쿼트 50회 완료하기"),
    WORKOUT_4(Category.WORKOUT, "줄넘기 500회 완료하기"),
    WORKOUT_5(Category.WORKOUT, "플랭크 총 3분 완료하기"),
    WORKOUT_6(Category.WORKOUT, "팔굽혀펴기 총 30회 완료하기"),
    WORKOUT_7(Category.WORKOUT, "계단 10층 이상 오르기"),
    WORKOUT_8(Category.WORKOUT, "오늘 하루 8,000보 이상 걷기"),
    WORKOUT_9(Category.WORKOUT, "홈트레이닝 영상을 따라 20분 이상 운동하기"),

    // 🧠 3. 건강 (HEALTH)
    HEALTH_1(Category.HEALTH, "하루 동안 물 1.5L 이상 마시기"),
    HEALTH_2(Category.HEALTH, "엘리베이터 대신 계단 이용하기"),
    HEALTH_3(Category.HEALTH, "오늘 하루 스마트폰 사용 시간을 4시간 이하로 유지하기"),
    HEALTH_4(Category.HEALTH, "햇빛을 쬐며 20분 이상 산책하기"),
    HEALTH_5(Category.HEALTH, "아침 또는 저녁에 영양제 챙겨 먹기"),
    HEALTH_6(Category.HEALTH, "카페인 음료 대신 물이나 디카페인 음료 마시기"),
    HEALTH_7(Category.HEALTH, "하루 8,000보 이상 걷기"),
    HEALTH_8(Category.HEALTH, "건강한 재료로 직접 한 끼 만들어 먹기"),
    HEALTH_9(Category.HEALTH, "하루 한 번 10분 동안 맨몸 스트레칭하기"),

    // 🎧 4. 취미 (HOBBY) — 하영피티 ver
    HOBBY_1(Category.HOBBY, "책을 20페이지 이상 읽고 인상 깊은 부분에 표시하기"),
    HOBBY_2(Category.HOBBY, "평소 해보지 않았던 새로운 운동 종목에 도전하기"),
    HOBBY_3(Category.HOBBY, "직접 요리하거나 베이킹해서 한 가지 메뉴 완성하기"),
    HOBBY_4(Category.HOBBY, "평소 가보지 않았던 곳으로 드라이브하기"),
    HOBBY_5(Category.HOBBY, "가보지 않았던 공원이나 동네를 30분 이상 걸으며 둘러보기"),
    HOBBY_6(Category.HOBBY, "전시회나 박물관을 방문해 작품 관람하기"),
    HOBBY_7(Category.HOBBY, "새로운 보드게임이나 카드게임 한 판 해보기"),
    HOBBY_8(Category.HOBBY, "평소 가보고 싶었던 카페에 방문해 30분 이상 여유 즐기기"),
    HOBBY_9(Category.HOBBY, "공방이나 원데이 클래스에서 새로운 취미 체험하기"),

    // 🏠 5. 일상생활 (LIFE)
    LIFE_1(Category.LIFE, "쌓여 있는 설거지를 모두 끝내고 싱크대 정리하기"),
    LIFE_2(Category.LIFE, "거실과 공용 공간 바닥을 청소기로 전체 청소하기"),
    LIFE_3(Category.LIFE, "화장실 변기와 세면대를 깨끗하게 청소하기"),
    LIFE_4(Category.LIFE, "음식물 쓰레기를 버리고 음식물 쓰레기통 정리하기"),
    LIFE_5(Category.LIFE, "세탁기를 돌리고 빨래를 널거나 건조하기"),
    LIFE_6(Category.LIFE, "마른 빨래를 모두 개서 제자리에 정리하기"),
    LIFE_7(Category.LIFE, "냉장고 안을 정리하고 선반을 깨끗하게 닦기"),
    LIFE_8(Category.LIFE, "집 안의 재활용품을 모아 분리수거하기"),
    LIFE_9(Category.LIFE, "창문을 열어 20분 이상 환기하고 공용 공간을 깔끔하게 정리하기");

    private final Category category;
    private final String description;
}