package com.easymall.entity.enums;

import com.easymall.utils.StringTools;

import java.util.Arrays;
import java.util.Optional;

public enum SearchFieldTypeEnum {

    COMPOSITE("composite", "_score", "综合"),
    SALE("sale", "totalSale", "销量"),
    PRICE("price", "minPrice", "价格");

    private String fieldType;
    private String field;
    private String desc;

    SearchFieldTypeEnum(String fieldType, String field, String desc) {
        this.fieldType = fieldType;
        this.field = field;
        this.desc = desc;
    }

    public String getFieldType() {
        return fieldType;
    }

    public String getDesc() {
        return desc;
    }

    public String getField() {
        return field;
    }

    public static SearchFieldTypeEnum getByFieldType(String fieldType) {
        if (StringTools.isEmpty(fieldType)) {
            return null;
        }
        Optional<SearchFieldTypeEnum> result = Arrays.stream(SearchFieldTypeEnum.values()).filter(value -> value.getFieldType().equals(fieldType)).findFirst();
        return result == null ? null : result.get();
    }
}
