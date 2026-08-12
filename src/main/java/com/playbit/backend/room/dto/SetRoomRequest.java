package com.playbit.backend.room.dto;

import com.playbit.backend.room.Category;

public record SetRoomRequest(Category category, String roomName) {
}
