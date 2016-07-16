package com.asb.goldtrap;

import com.asb.goldtrap.models.tools.ConfigGenerator;

/**
 * Main class
 * Created by arjun on 16/07/16.
 */
public class Main {
    public static void main(String[] args) {
        ConfigGenerator configGenerator = new ConfigGenerator();
        configGenerator.generateConfigs();
    }
}
