package com.tdeado.generate.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class VueAuth {
    String code;
    String name;
    List<String> apis;
}
