package me.aviso.ArmyAntiCheat.ReportSystem;

public class PlayerReportData {
    private String username;
    private int anticheatReports;
    private long lastAnticheatReport;
    private int playerReports;

    public PlayerReportData(String username) {
        this.username = username;
        this.anticheatReports = 0;
        this.lastAnticheatReport = 0;
        this.playerReports = 0;
    }

    public void incrementAnticheatReports() {
        this.anticheatReports++;
    }

    public void setLastAnticheatReport(long timestamp) {
        this.lastAnticheatReport = timestamp;
    }

    public void incrementPlayerReports() {
        this.playerReports++;
    }

    public String getUsername() {
        return username;
    }

    public int getAnticheatReports() {
        return anticheatReports;
    }

    public long getLastAnticheatReport() {
        return lastAnticheatReport;
    }

    public int getPlayerReports() {
        return playerReports;
    }
}