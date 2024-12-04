package me.aviso.ArmyAntiCheat.ReportSystem;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.aviso.ArmyAntiCheat.Main;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ReportSystem {
    private final File reportFile;
    private final Map<UUID, PlayerReportData> reportDataMap;
    private final Gson gson;

    public ReportSystem() {
        this.reportFile = new File(Main.getInstance().getDataFolder(), "reports.json");
        this.reportDataMap = new HashMap<>();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        loadReports();
    }

    public void loadReports() {
        if (!reportFile.exists()) {
            try {
                reportFile.createNewFile();
            } catch (IOException e) {
                Main.logger.error(e.getMessage());
            }
        } else {
            try (Reader reader = new FileReader(reportFile)) {
                JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
                for (String key : jsonObject.keySet()) {
                    UUID uuid = UUID.fromString(key);
                    PlayerReportData data = gson.fromJson(jsonObject.get(key), PlayerReportData.class);
                    reportDataMap.put(uuid, data);
                }
            } catch (IOException e) {
                Main.logger.error(e.getMessage());
            }
        }
    }

    public void saveReports() {
        JsonObject jsonObject = new JsonObject();
        for (Map.Entry<UUID, PlayerReportData> entry : reportDataMap.entrySet()) {
            jsonObject.add(entry.getKey().toString(), gson.toJsonTree(entry.getValue()));
        }
        try (Writer writer = new FileWriter(reportFile)) {
            gson.toJson(jsonObject, writer);
        } catch (IOException e) {
            Main.logger.error(e.getMessage());
        }
    }

    public void updateReport(UUID playerUUID, String username, boolean isAnticheatReport) {
        PlayerReportData data = reportDataMap.getOrDefault(playerUUID, new PlayerReportData(username));
        if (isAnticheatReport) {
            data.incrementAnticheatReports();
            data.setLastAnticheatReport(System.currentTimeMillis());
        } else {
            data.incrementPlayerReports();
        }
        reportDataMap.put(playerUUID, data);
        saveReports();
    }

    public PlayerReportData getReportData(UUID playerUUID) {
        return reportDataMap.get(playerUUID);
    }
}
