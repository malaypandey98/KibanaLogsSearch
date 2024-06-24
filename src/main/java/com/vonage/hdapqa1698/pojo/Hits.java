package com.vonage.hdapqa1698.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class Hits {

    @JsonProperty("total")
    private Integer total;

    @JsonProperty("hits")
    private List<NestedHits> hits;

}
