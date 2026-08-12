package com.playbit.backend.room.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.playbit.backend.room.RoomStatus;
import lombok.Getter;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "status",
        visible = true)
@JsonSubTypes({
    @JsonSubTypes.Type(value = PlayingRoomDto.class, name = "PLAYING"),
    @JsonSubTypes.Type(value = FinishedRoomDto.class, name = "FINISHED")
})
@Getter
public class RoomDto {

    protected RoomStatus status;

    public RoomDto(RoomStatus status) {
        this.status = status;
    }
}
