package com.vonage.hdapqa1698.helpers;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Order {
    ASC("asc"), DESC("desc");

    private final String type;
}
