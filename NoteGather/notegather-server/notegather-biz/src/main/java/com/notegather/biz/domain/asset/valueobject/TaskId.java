package com.notegather.biz.domain.asset.valueobject;

import lombok.Value;

/**
 * 任务ID值对象
 */
@Value(staticConstructor = "of")
public class TaskId {
    Long value;
    
    public static TaskId create() {
        return null; // 由基础设施层生成
    }
}
