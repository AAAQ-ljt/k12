package com.easymall.entity.enums;

import com.easymall.utils.StringTools;
import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.Optional;

public enum SearchSortTypeEnum {

    ASC("asc", Sort.Direction.ASC, "顺序"),
    DESC("desc", Sort.Direction.DESC, "倒序");

    private String type;
    private Sort.Direction direction;
    private String desc;

    SearchSortTypeEnum(String type, Sort.Direction direction, String desc) {
        this.type = type;
        this.direction = direction;
        this.desc = desc;
    }

    public String getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }

    public Sort.Direction getDirection() {
        return direction;
    }

    public static SearchSortTypeEnum getByType(String type) {
        if (StringTools.isEmpty(type)) {
            return null;
        }
        Optional<SearchSortTypeEnum> result = Arrays.stream(SearchSortTypeEnum.values()).filter(value -> value.getType().equals(type)).findFirst();
        return result == null ? null : result.get();
    }
}
