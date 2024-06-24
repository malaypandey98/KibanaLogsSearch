package com.vonage.hdapqa1698.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class KibanaResponse {

    @JsonProperty("rawResponse")
    private RawResponse rawResponse;

}
