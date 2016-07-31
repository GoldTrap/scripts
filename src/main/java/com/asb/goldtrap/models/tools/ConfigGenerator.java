package com.asb.goldtrap.models.tools;

import com.asb.goldtrap.models.eo.*;
import com.asb.goldtrap.models.states.enums.GoodiesState;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonWriter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Config Generator.
 * Created by arjun on 16/07/16.
 */
public class ConfigGenerator {

    private Gson dynamicGsonParser = new GsonBuilder().create();
    private Type dynamicGoodieType = new TypeToken<List<DynamicGoodieData>>() {
    }.getType();

    public void generateConfigs() {
        final File dir = getOutputDirectory();
        parseRecords()
                .stream()
                .parallel()
                .map(this::transform)
                .forEach(level -> {
                    String fileName = level.getLevelCode() + ".json";
                    System.out.println("Generating File: " + fileName + " at " +
                            LocalDateTime.now()
                                    .format(DateTimeFormatter.ISO_TIME) + " " +
                            "in the thread " + Thread.currentThread()
                            .getName());

                    Gson gson = new GsonBuilder()
                            .excludeFieldsWithoutExposeAnnotation()
                            .create();
                    try (JsonWriter jsonWriter = gson.newJsonWriter(
                            new BufferedWriter(
                                    new FileWriter(new File(dir, fileName))))) {
                        jsonWriter.setIndent("  ");
                        gson.toJson(level, Level.class, jsonWriter);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Generated File: " + fileName + " at " +
                            LocalDateTime.now()
                                    .format(DateTimeFormatter.ISO_TIME) + " " +
                            "in the thread " + Thread.currentThread()
                            .getName());
                });
    }

    private File getOutputDirectory() {
        File dir = new File("output");
        if (!dir.exists()) {
            dir.mkdir();
        }
        return dir;
    }

    private List<CSVRecord> parseRecords() {
        List<CSVRecord> records = null;
        try (Reader in = new FileReader("configurations.csv")) {
            records = CSVFormat.RFC4180
                    .withFirstRecordAsHeader()
                    .parse(in)
                    .getRecords();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return records;
    }

    private Level transform(CSVRecord record) {
        return Level.builder()
                .levelCode(String.format(Locale.US, "e%02dc%02d",
                        Long.valueOf(record.get("Episode")),
                        Long.valueOf(record.get("Level"))))
                .solver(record.get("Solver"))
                .rows(Integer.valueOf(record.get("Rows")))
                .cols(Integer.valueOf(record.get("Cols")))
                .blocked(Integer.valueOf(record.get("Blocked")))
                .firstPlayer(PlayerType.valueOf(record.get("First")))
                .tasks(getTasks(record))
                .goodies(getGoodies(record))
                .dynamicGoodies(getDynamicGoodies(record))
                .complications(getComplications(record))
                .build();
    }

    private List<Complication> getComplications(CSVRecord record) {
        return ImmutableList.<Complication>builder()
                .addAll(getPositionComplication(record))
                .addAll(getValueComplicationForDynamicGoodies(record))
                .build();
    }

    private List<Complication> getValueComplicationForDynamicGoodies(
            CSVRecord record) {
        return Arrays.asList("AP", "GP")
                .stream()
                .filter(c -> isValidRecordItem(record, c))
                .map(c -> Complication.builder()
                        .operator("DYNAMIC_GOODIE_VALUE_MODIFIER")
                        .strategy(StrategyData.builder()
                                .type(c)
                                .value(Integer.valueOf(record.get(c)))
                                .build())
                        .build())
                .collect(Collectors.toList());
    }

    private List<Complication> getPositionComplication(CSVRecord record) {
        return Arrays.asList("VERTICAL", "HORIZONTAL", "DIAGONAL")
                .stream()
                .filter(c -> isValidRecordItem(record, c))
                .map(c -> Complication.builder()
                        .operator("GOODIE_POSITION_MODIFIER")
                        .strategy(StrategyData.builder()
                                .type(c)
                                .build())
                        .build())
                .collect(Collectors.toList());
    }

    private List<DynamicGoodieData> getDynamicGoodies(CSVRecord record) {
        List<DynamicGoodieData> goodieData = null;
        if (isValidRecordItem(record, "DyP")) {
            goodieData = dynamicGsonParser.fromJson(record.get("DyP"),
                    dynamicGoodieType);
        }
        return (null == goodieData) ? Collections.emptyList() : goodieData;
    }

    private List<GoodieData> getGoodies(CSVRecord record) {
        return Arrays.asList(GoodiesState.values())
                .stream()
                .filter(goodie -> isValidRecordItem(record,
                        "P_" + goodie.name()))
                .map(goodie -> GoodieData.builder()
                        .type(goodie)
                        .count(Integer.valueOf(
                                record.get("P_" + goodie.name())))
                        .build())
                .collect(Collectors.toList());
    }

    private List<Task> getTasks(CSVRecord record) {
        return Arrays.asList(TaskType.values())
                .stream()
                .filter(taskType -> isValidRecordItem(record,
                        "T_" + taskType.name()))
                .map(taskType -> buildTask(record, taskType))
                .collect(Collectors.toList());
    }

    private Task buildTask(CSVRecord record, TaskType taskType) {
        Task.TaskBuilder tasksBuilder = Task.builder()
                .taskType(taskType);
        if (TaskType.POINTS == taskType) {
            tasksBuilder.points(Integer.valueOf(
                    record.get("T_" + taskType.name())));
        } else {
            if (TaskType.DYNAMIC_GOODIE == taskType) {
                JSONObject json = new JSONObject(
                        record.get("T_" + taskType.name()));
                tasksBuilder.count(json.getInt("count"));
                tasksBuilder.points(json.getInt("points"));
            } else {
                tasksBuilder.count(Integer.valueOf(
                        record.get("T_" + taskType.name())));
            }
        }

        return tasksBuilder.build();
    }

    private boolean isValidRecordItem(CSVRecord record, String key) {
        boolean valid = false;
        try {
            String item = record.get(key);
            valid = StringUtils.isNotEmpty(item) && !StringUtils.equals(item,
                    "-");
        } catch (IllegalArgumentException iae) {
            // System.err.println("Illegal record " + key);
        }
        return valid;
    }

}
