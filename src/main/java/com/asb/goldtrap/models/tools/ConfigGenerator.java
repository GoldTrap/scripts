package com.asb.goldtrap.models.tools;

import com.asb.goldtrap.models.eo.*;
import com.asb.goldtrap.models.states.enums.GoodiesState;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Config Generator.
 * Created by arjun on 16/07/16.
 */
public class ConfigGenerator {

    public void generateConfigs() {
        final File dir = getOutputDirectory();
        parseRecords()
                .stream()
                .map(this::transform)
                .forEach(level -> {
                    File file = new File(dir, level.getLevelCode() + ".json");
                    Gson gson = new GsonBuilder()
                            .excludeFieldsWithoutExposeAnnotation()
                            .create();
                    try (JsonWriter jsonWriter = gson.newJsonWriter(
                            new BufferedWriter(new FileWriter(file)))) {
                        jsonWriter.setIndent("  ");
                        gson.toJson(level, Level.class, jsonWriter);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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
                .build();
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
            tasksBuilder.count(Integer.valueOf(
                    record.get("T_" + taskType.name())));
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
            System.err.println("Illegal record " + key);
        }
        return valid;
    }

}
