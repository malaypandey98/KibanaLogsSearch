package com.vonage.hdapqa1698.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class NestedHits {

    @JsonProperty("_id")
    private String id;

    @JsonProperty("_source")
    private Source source;

    @JsonProperty("sort")
    private List<Object> sort;

}
