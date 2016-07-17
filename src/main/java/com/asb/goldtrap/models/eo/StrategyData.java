package com.asb.goldtrap.models.eo;

import com.google.gson.annotations.Expose;
import lombok.Builder;
import lombok.Data;

/**
 * Created by arjun on 31/10/15.
 */
@Data
@Builder
public class StrategyData {
    @Expose
    private String type;
    @Expose
    private int value;
}
