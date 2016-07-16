package com.asb.goldtrap.models.eo;


import com.google.gson.annotations.Expose;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Created by arjun on 25/10/15.
 */
@Data
@Builder
public class Level {
    private String levelCode;
    @Expose
    private String solver;
    @Expose
    private int rows;
    @Expose
    private int cols;
    @Expose
    private int blocked;
    @Expose
    private List<Task> tasks;
    @Expose
    private List<GoodieData> goodies;
    @Expose
    private List<DynamicGoodieData> dynamicGoodies;
    @Expose
    private PlayerType firstPlayer;
    private String adType;
    @Expose
    private List<Complication> complications;
    @Expose
    private List<Unlocks> unlocks;
}
