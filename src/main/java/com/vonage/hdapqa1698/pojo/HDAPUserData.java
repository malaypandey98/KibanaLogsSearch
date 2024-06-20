package com.vonage.hdapqa1698.pojo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.vonage.hdapqa1698.deserializers.HDAPUserDataDeserializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@JsonDeserialize(using = HDAPUserDataDeserializer.class)
@Builder
public class HDAPUserData {

    private LocalDateTime loggedTime;

    private String username;

    private String source;

    private String endpoint;

    private String logMessage;

}
