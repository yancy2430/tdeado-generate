package com.tdeado.generate.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class VueBean {
    String name;
    String apis;
    String permissions;
    String path;
    String component;
    String code;
}
