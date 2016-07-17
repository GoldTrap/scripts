package com.asb.goldtrap.models.eo;

import com.google.gson.annotations.Expose;
import lombok.Builder;
import lombok.Data;

/**
 * Created by arjun on 25/10/15.
 */
@Data
@Builder
public class DynamicGoodieData {
    @Expose
    private int value;
    @Expose
    private int count;
}
