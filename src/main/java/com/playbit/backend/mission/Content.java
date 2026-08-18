package com.playbit.backend.mission;

import com.playbit.backend.room.Category;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Content {

    // 📚 1. 공부 (STUDY) — 10분 시작 습관
    STUDY_1(Category.STUDY, "오늘 관심 있던 주제를 10분 동안 찾아보기"),
    STUDY_2(Category.STUDY, "책이나 아티클을 10분 동안 읽기"),
    STUDY_3(Category.STUDY, "오늘 새롭게 알게 된 것 하나 기록하기"),
    STUDY_4(Category.STUDY, "미뤄두었던 공부를 10분만 시작하기"),
    STUDY_5(Category.STUDY, "업무와 관련된 내용을 하나 찾아보기"),
    STUDY_6(Category.STUDY, "읽고 싶었던 책을 5페이지 읽기"),
    STUDY_7(Category.STUDY, "오늘 배운 내용을 한 문장으로 정리하기"),
    STUDY_8(Category.STUDY, "내일 할 일을 미리 정리하기"),
    STUDY_9(Category.STUDY, "30분 동안 휴대폰 없이 집중하기"),

    // 🏃 2. 운동 (WORKOUT) — 퇴근 후 가벼운 몸 움직임
    WORKOUT_1(Category.WORKOUT, "10분 동안 가볍게 산책하기"),
    WORKOUT_2(Category.WORKOUT, "5분 동안 전신 스트레칭하기"),
    WORKOUT_3(Category.WORKOUT, "집에 도착하기 전 한 정거장 걸어보기"),
    WORKOUT_4(Category.WORKOUT, "엘리베이터 대신 계단 이용하기"),
    WORKOUT_5(Category.WORKOUT, "목과 어깨를 5분 동안 스트레칭하기"),
    WORKOUT_6(Category.WORKOUT, "집에서 스쿼트 10회 해보기"),
    WORKOUT_7(Category.WORKOUT, "1분 동안 가볍게 몸을 움직이기"),
    WORKOUT_8(Category.WORKOUT, "오늘 걸었던 시간을 확인해보기"),
    WORKOUT_9(Category.WORKOUT, "집 주변을 10분 동안 걸어보기"),

    // 🧠 3. 건강 (HEALTH) — 도파민 소비 & 스마트폰 과사용 차단
    HEALTH_1(Category.HEALTH, "잠들기 전 30분 동안 스마트폰 내려놓기"),
    HEALTH_2(Category.HEALTH, "저녁 식사 동안 스마트폰 보지 않기"),
    HEALTH_3(Category.HEALTH, "쇼츠와 릴스를 30분 동안 보지 않기"),
    HEALTH_4(Category.HEALTH, "휴대폰을 손에 들지 않고 10분 동안 쉬기"),
    HEALTH_5(Category.HEALTH, "잠깐 창문을 열고 환기하기"),
    HEALTH_6(Category.HEALTH, "물 한 컵 마시고 천천히 쉬기"),
    HEALTH_7(Category.HEALTH, "5분 동안 눈을 감고 휴식하기"),
    HEALTH_8(Category.HEALTH, "오늘 스마트폰 사용 시간을 확인해보기"),
    HEALTH_9(Category.HEALTH, "잠들기 1시간 전 알림을 꺼두기"),

    // 🎧 4. 취미 (HOBBY) — 쇼츠 대신 할 수 있는 대체 행동
    HOBBY_1(Category.HOBBY, "좋아하는 음악 한 곡을 집중해서 듣기"),
    HOBBY_2(Category.HOBBY, "좋아하는 책을 10분 동안 읽기"),
    HOBBY_3(Category.HOBBY, "사진 한 장 찍고 기록하기"),
    HOBBY_4(Category.HOBBY, "5분 동안 그림이나 낙서하기"),
    HOBBY_5(Category.HOBBY, "새로운 음악 한 곡 찾아보기"),
    HOBBY_6(Category.HOBBY, "평소 해보고 싶었던 일을 10분 동안 해보기"),
    HOBBY_7(Category.HOBBY, "좋아하는 취미를 15분 동안 즐기기"),
    HOBBY_8(Category.HOBBY, "오늘 가장 기억에 남는 순간을 기록하기"),
    HOBBY_9(Category.HOBBY, "휴대폰 없이 10분 동안 혼자만의 시간 보내기"),

    // 🏠 5. 일상생활 (LIFE) — 퇴근 후 생활 루틴 회복
    LIFE_1(Category.LIFE, "퇴근 후 바로 침대에 눕지 않기"),
    LIFE_2(Category.LIFE, "침대 정리하기"),
    LIFE_3(Category.LIFE, "책상이나 작업 공간 5분 동안 정리하기"),
    LIFE_4(Category.LIFE, "집에 있는 쓰레기 5개 버리기"),
    LIFE_5(Category.LIFE, "사용한 물건을 제자리에 돌려놓기"),
    LIFE_6(Category.LIFE, "내일 입을 옷 미리 준비하기"),
    LIFE_7(Category.LIFE, "가방을 정리하고 내일 필요한 물건 챙기기"),
    LIFE_8(Category.LIFE, "설거지나 집안일 하나 끝내기"),
    LIFE_9(Category.LIFE, "내일 해야 할 일 3가지만 적어보기");

    private final Category category;
    private final String description;
}