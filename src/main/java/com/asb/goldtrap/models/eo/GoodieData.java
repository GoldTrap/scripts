package com.asb.goldtrap.models.eo;

import com.asb.goldtrap.models.states.enums.GoodiesState;
import com.google.gson.annotations.Expose;
import lombok.Builder;
import lombok.Data;

/**
 * Created by arjun on 26/10/15.
 */
@Data
@Builder
public class GoodieData {
    @Expose
    private GoodiesState type;
    @Expose
    private int count;
}
