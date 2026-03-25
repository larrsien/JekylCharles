package lars.com.monitoring;

import lars.com.PetController;
import lars.com.reactions.DualResponseLibrary;
import oshi.SystemInfo;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

import javax.swing.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class InternalProcessMonitoring {

    private final PetController petController;
    private final OperatingSystem operatingSystem;
    private final ScheduledExecutorService scheduler;
    private Set<String> previousProcesses;

    private static final String[] KEYWORDS = {
            "discord", "telegram", "clipstudio", "sai2", "steam",
            "chrome", "opera", "bloodborne", "shadps4", "peak",
            "warframe", "roblox", "phasmophobia", "nightreign",
            "blender", "bandicam", "obs64", "torrent", "amneziavpn",
            "minecraft", "kaspersky"
    };

    public InternalProcessMonitoring(PetController petController) {
        this.petController   = petController;
        SystemInfo systemInfo = new SystemInfo();
        this.operatingSystem = systemInfo.getOperatingSystem();
        this.scheduler       = Executors.newScheduledThreadPool(1);

        // Снимок текущих процессов, чтобы не реагировать на уже запущенное
        this.previousProcesses = new HashSet<>();
        for (OSProcess p : operatingSystem.getProcesses()) {
            previousProcesses.add(p.getName().toLowerCase());
        }
    }

    public void start() {
        scheduler.scheduleAtFixedRate(this::scanProcesses, 5, 5, TimeUnit.SECONDS);
    }

    public void close() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(3, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }

    private void scanProcesses() {
        try {
            Set<String> currentProcesses = new HashSet<>();
            List<OSProcess> processes = operatingSystem.getProcesses();

            for (OSProcess osProcess : processes) {
                String processName = osProcess.getName().toLowerCase();
                currentProcesses.add(processName);

                if (!previousProcesses.contains(processName) && isCompatible(processName)) {
                    notifyNewProcess(processName);
                }
            }

            for (String oldProcess : previousProcesses) {
                if (!currentProcesses.contains(oldProcess) && isCompatible(oldProcess)) {
                    notifyProcessClosed(oldProcess);
                }
            }

            previousProcesses = currentProcesses;

        } catch (Exception e) {
            System.out.println("Ошибка при попытке отсканировать процессы");
        }
    }

    private void notifyNewProcess(String processName) {
        System.out.println("Обнаружен новый процесс: " + processName);

        String category = mapProcessToCategory(processName);
        if (category != null) {
            SwingUtilities.invokeLater(() -> petController.react(category));
        }
    }

    private void notifyProcessClosed(String processName) {
        System.out.println("Процесс закрыт: " + processName);
        // Можно добавить категории типа "discord_closed" если нужно
    }

    private boolean isCompatible(String processName) {
        for (String keyWord : KEYWORDS) {
            if (processName.contains(keyWord)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Маппит имя процесса на категорию из DualResponseLibrary.
     * Возвращает null, если категория не определена.
     */
    private String mapProcessToCategory(String processName) {
        String lower = processName.toLowerCase();

        if (lower.contains("telegram"))      return "telegram";
        if (lower.contains("discord"))       return "discord";
        if (lower.contains("steam"))         return "steam";
        if (lower.contains("chrome")
                || lower.contains("opera"))         return "browser";
        if (lower.contains("clipstudio")
                || lower.contains("sai2"))          return "drawing";
        if (lower.contains("blender"))       return "blender";
        if (lower.contains("obs64")
                || lower.contains("bandicam"))      return "recording";
        if (lower.contains("torrent"))       return "torrent";
        if (lower.contains("amneziavpn"))    return "vpn";
        if (lower.contains("kaspersky"))     return "antivirus";
        if (lower.contains("minecraft"))     return "minecraft";

        // Игры
        if (lower.contains("bloodborne")
                || lower.contains("shadps4")
                || lower.contains("warframe")
                || lower.contains("roblox")
                || lower.contains("phasmophobia")
                || lower.contains("nightreign")
                || lower.contains("peak"))          return "gaming";

        return "default";
    }
}
