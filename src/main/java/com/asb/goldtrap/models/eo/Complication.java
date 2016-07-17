package com.asb.goldtrap.models.eo;

import com.google.gson.annotations.Expose;
import lombok.Builder;
import lombok.Data;

/**
 * Created by arjun on 26/10/15.
 */
@Data
@Builder
public class Complication {
    @Expose
    private String operator;
    @Expose
    private StrategyData strategy;
}

