package com.tdeado.generate;

public class EnumField {
    private String name;
    private Integer value;
    private String label;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return "EnumField{" +
                "name='" + name + '\'' +
                ", value=" + value +
                ", label='" + label + '\'' +
                '}';
    }
}
