package com.playbit.backend.player.dto;


public record PlayerJoinResponse(
        Long playerId,
        Long memberId,
        String Role
){
}
