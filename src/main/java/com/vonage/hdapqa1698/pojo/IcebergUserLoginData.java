package com.vonage.hdapqa1698.pojo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.vonage.hdapqa1698.deserializers.IcebergUserLoginDataDeserializer;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@JsonDeserialize(using = IcebergUserLoginDataDeserializer.class)
public class IcebergUserLoginData {

    private LocalDateTime localDateTime;

    private Long accountId;

    private String username;

    @Override
    public int hashCode(){
        return 31*accountId.hashCode() + username.hashCode();
    }
}
