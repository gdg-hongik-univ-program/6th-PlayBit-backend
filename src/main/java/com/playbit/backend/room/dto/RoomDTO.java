package com.playbit.backend.room.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.playbit.backend.room.RoomStatus;
import lombok.Getter;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "status",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = PlayingRoomDTO.class, name = "PLAYING"),
        @JsonSubTypes.Type(value = FinishedRoomDTO.class, name = "FINISHED")
})
@Getter
public class RoomDTO {

    protected RoomStatus status;

    public RoomDTO(RoomStatus status) {
        this.status = status;
    }
}
