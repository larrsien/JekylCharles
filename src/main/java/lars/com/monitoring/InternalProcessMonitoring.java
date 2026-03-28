package lars.com.monitoring;

import lars.com.PetController;
import oshi.SystemInfo;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

import javax.swing.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class InternalProcessMonitoring {

    private final PetController petController;
    private final OperatingSystem operatingSystem;
    private final ScheduledExecutorService scheduler;
    private Set<String> previousProcesses;
    private final Set<String> alreadyReacted = new HashSet<>();

    //  [0] — подстрока имени процесса, [1] — категория реакции.
    private static final String[][] KEYWORD_TO_CATEGORY = {

            {"msedge", "browser"},
            {"deadbydaylight", "deadbydaylight"},
            {"repo", "repo"},
            {"minecraft", "minecraft"},
            {"discord", "discord"},
            {"peak", "peak"},
            {"steam", "steam"},
            {"photoshop", "photoshop"},
            {"sixvpn", "sixvpn"},
            {"telegram", "telegram"},
            {"winword", "winword"},
            {"mspaint", "mspaint"},
            {"nightreign", "nightreign"},
            {"kebabchefs", "kebabchefs"},
            {"warframe", "warframe"},
            {"liarsbar", "liarsbar"}
    };

    public InternalProcessMonitoring(PetController petController) {
        this.petController = petController;
        SystemInfo systemInfo = new SystemInfo();
        this.operatingSystem = systemInfo.getOperatingSystem();
        this.scheduler = Executors.newScheduledThreadPool(1);

        // Снимок текущих процессов, чтобы не реагировать на уже запущенное
        this.previousProcesses = new HashSet<>();
        for (OSProcess p : operatingSystem.getProcesses()) {
            String name = p.getName().toLowerCase();

            if (name.contains("javaw")) {
                String cmd = p.getCommandLine().toLowerCase();
                if (cmd.contains("minecraft")) {
                    name = "minecraft";
                }
            }
            previousProcesses.add(name);
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

    private String findCategory(String processName) {
        for (String[] pair : KEYWORD_TO_CATEGORY) {
            if (processName.contains(pair[0])) {
                return pair[1];
            }
        }
        return null;
    }

    private void scanProcesses() {
        try {
            Set<String> currentProcesses = new HashSet<>();
            List<OSProcess> processes = operatingSystem.getProcesses();

            for (OSProcess osProcess : processes) {
                String processName = osProcess.getName().toLowerCase();

                if (processName.contains("javaw")) {
                    String cmd = osProcess.getCommandLine().toLowerCase();
                    if (cmd.contains("minecraft")) {
                        processName = "minecraft";
                    }
                }

                currentProcesses.add(processName);

                if (!previousProcesses.contains(processName)) {
                    String category = findCategory(processName);
                    if (category != null) {
                        notifyNewProcess(processName, category);
                    }
                }
            }

            for (String oldProcess : previousProcesses) {
                if (!currentProcesses.contains(oldProcess)) {
                    String category = findCategory(oldProcess);
                    if (category != null) {
                        notifyProcessClosed(oldProcess, category);
                    }
                }
            }

            previousProcesses = currentProcesses;

        } catch (Exception e) {
            System.out.println("Ошибка при попытке отсканировать процессы");
        }
    }

    private void notifyNewProcess(String processName, String category) {
        if (alreadyReacted.contains(category)) return;
        alreadyReacted.add(category);

        System.out.println("Обнаружен новый процесс: " + processName + " → " + category);
        SwingUtilities.invokeLater(() -> petController.react(category));
    }

    private void notifyProcessClosed(String processName, String category) {
        alreadyReacted.remove(category);
        System.out.println("Процесс закрыт: " + processName + " → сброс категории " + category);
    }
}
