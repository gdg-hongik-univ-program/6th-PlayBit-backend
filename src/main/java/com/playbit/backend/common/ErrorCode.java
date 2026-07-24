package com.playbit.backend.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    //Auth
    AUTH_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A0001", "X-Member-Id 헤더가 필요합니다."),

    //Member
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MM001", "사용자를 찾을 수 없습니다."),

    //Mission
    MISSION_NOT_FOUND(HttpStatus.NOT_FOUND, "MS001", "미션을 찾을 수 없습니다."),
    MISSION_CANNOT_SABOTAGE_AT_YOUR_TURN(HttpStatus.BAD_REQUEST, "MS002", "자신의 차례에는 사보타주가 불가합니다."),
    MISSION_CANNOT_SABOTAGE_TO_YOUR_MISSION(HttpStatus.BAD_REQUEST, "MS003", "자신이 완료한 미션은 사보타주가 불가합니다."),
    MISSION_CANNOT_SABOTAGE_TO_UNCOMPLETED_MISSION(HttpStatus.BAD_REQUEST, "MS004", "완료되지 않은 미션에는 사보타주가 불가합니다."),

    //Room
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "R001", "방을 찾을 수 없습니다."),
    ROOM_FINISHED(HttpStatus.BAD_REQUEST, "R002", "종료된 게임입니다."),
    ROOM_NOT_YOUR_TURN(HttpStatus.BAD_REQUEST, "R003", "해당 사용자의 차례가 아닙니다."),
    ROOM_ALREADY_SABOTAGED_AT_THIS_TURN(HttpStatus.BAD_REQUEST, "R004", "이번 턴에 이미 한 번의 사보타주 기회를 사용하였습니다."),

    //Player
    PLAYER_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "플레이어를 찾을 수 없습니다."),
    PLAYER_OPPONENT_NOT_FOUND(HttpStatus.NOT_FOUND, "P002", "상대편 플레이어를 찾을 수 없습니다."),
    PLAYER_ALREADY_REGISTERED(HttpStatus.BAD_REQUEST, "P003", "이미 방에 참가한 사용자입니다."),
    PLAYER_ROOM_IS_ALREADY_FULL(HttpStatus.BAD_REQUEST, "P004", "해당 방에 이미 2명의 플레이어가 모두 입장하였습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
