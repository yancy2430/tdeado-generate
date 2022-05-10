package ${package};

import com.baomidou.mybatisplus.annotation.EnumValue;

public enum ${name} {
<#list enums as info>
    ${info.name}(${info.value}, "${info.label}")<#sep>,
</#list>;
    @EnumValue
    private final int value;
    private final String label;
    ${name}(int value, String label) {
        this.value = value;
        this.label = label;
    }
}
