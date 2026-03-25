package lars.com.monitoring;

import lars.com.PetController;
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
    private final Set<String> alreadyReacted = new HashSet<>();

    private static final String[] KEYWORDS = {
            "microsoftedge", "deadbydaylight", "repo",
            "minecraft", "discord", "telegram", "steam",
            "word", "paint", "hades", "peak",
            "warframe", "kebabchief", "liarsbar", "nightreign",
            "enigmatrials", "wherewindsmeet"
    };

    public InternalProcessMonitoring(PetController petController) {
        this.petController = petController;
        SystemInfo systemInfo = new SystemInfo();
        this.operatingSystem = systemInfo.getOperatingSystem();
        this.scheduler = Executors.newScheduledThreadPool(1);

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
        String category = mapProcessToCategory(processName);

        // Уже реагировали на эту категорию — пропускаем
        if (alreadyReacted.contains(category)) return;
        alreadyReacted.add(category);

        System.out.println("Обнаружен новый процесс: " + processName + " → " + category);
        SwingUtilities.invokeLater(() -> petController.react(category));
    }

    private void notifyProcessClosed(String processName) {
        System.out.println("Процесс закрыт: " + processName);
    }

    private boolean isCompatible(String processName) {
        for (String keyWord : KEYWORDS) {
            if (processName.contains(keyWord)) {
                return true;
            }
        }
        return false;
    }

    private String mapProcessToCategory(String processName) {
        String lower = processName.toLowerCase();

        if (lower.contains("telegram")) return "telegram";
        if (lower.contains("discord")) return "discord";
        if (lower.contains("steam")) return "steam";
        if (lower.contains("microsoftedge")) return "browser";
        if (lower.contains("paint")) return "painting";
        if (lower.contains("minecraft")) return "minecraft";
        if (lower.contains("deadbydaylight")) return "deadbydaylight";
        if (lower.contains("repo")) return "repo";
        if (lower.contains("word")) return "word";
        if (lower.contains("hades")) return "hades";
        if (lower.contains("peak")) return "peak";
        if (lower.contains("warframe"))return "warframe";
        if (lower.contains("kebabchief"))return "kebabchief";
        if (lower.contains("liarsbar"))return "liarsbar";
        if (lower.contains("nightreign"))return "nightreign";
        if (lower.contains("enigmatrials"))return "enigmatrials";
        if (lower.contains("wherewindsmeet"))return "wherewindsmeet";

        return "default";
    }
}
