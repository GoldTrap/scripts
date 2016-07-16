package com.asb.goldtrap.models.eo;

import com.google.gson.annotations.Expose;
import lombok.Builder;
import lombok.Data;

/**
 * Created by arjun on 07/11/15.
 */
@Data
@Builder
public class Task {
    @Expose
    private TaskType taskType;
    @Expose
    private int count;
    @Expose
    private int points;
}
