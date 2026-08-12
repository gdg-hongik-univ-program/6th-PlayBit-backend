package com.playbit.backend.room.dto;

import java.util.List;

public record RoomCreateResponse(String entryCode, List<CategoryItem> categories) {
    public record CategoryItem(String code, String name) {}
}
