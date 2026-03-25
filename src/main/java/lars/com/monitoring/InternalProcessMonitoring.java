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
    private final DualResponseLibrary dualResponseLibrary;
    private final ScheduledExecutorService scheduler;
    private Set<String> previousProcesses;
    private final SystemInfo systemInfo;

    private static final String[] KEYWORDS = {
            "discord", "telegram", "clipstudio", "sai2", "steam", "chrome", "opera", "bloodborne", "shadps4", "peak",
            "warframe", "roblox", "phasmophobia", "nightreign", "blender", "bandicam", "obs64",
            "torrent", "amneziavpn", "minecraft", "kaspersky"
    };

    public InternalProcessMonitoring(PetController petController) {
        this.petController = petController;
        this.systemInfo = new SystemInfo();
        this.operatingSystem = systemInfo.getOperatingSystem();
        this.dualResponseLibrary = new DualResponseLibrary();
        this.scheduler = Executors.newScheduledThreadPool(1);

        // Снимаем снимок текущих процессов СРАЗУ, чтобы не реагировать на то, что уже запущено
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
        System.out.println("Обнаружен новый процесс:" + processName);

        String reaction = getProcessReaction(processName);
        if (reaction != null) {
            SwingUtilities.invokeLater(() -> petController.react(category));
        }
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

    private String getProcessReaction(String process) {
        String toLower = process.toLowerCase();

        if (toLower.contains("telegram")) {
            return responseLibrary.getRandomResponses("telegram");
        } else if (toLower.contains("discord")) {
            return responseLibrary.getRandomResponses("discord");
        } else if (toLower.contains("steam")) {
            return responseLibrary.getRandomResponses("steam");
        } else if (toLower.contains("chrome") || toLower.contains("opera") || toLower.contains("firefox")) {
            return responseLibrary.getRandomResponses("browser");
        } else if (toLower.contains("clipstudio") || toLower.contains("sai2")) {
            return responseLibrary.getRandomResponses("painting");
        } else if (toLower.contains("bloodborne") || toLower.contains("shadps4")) {
            return responseLibrary.getRandomResponses("bloodborne");
        } else if (toLower.contains("peak")) {
            return responseLibrary.getRandomResponses("peak");
        } else if (toLower.contains("warframe")) {
            return responseLibrary.getRandomResponses("warframe");
        } else if (toLower.contains("roblox")) {
            return responseLibrary.getRandomResponses("roblox");
        } else if (toLower.contains("phasmophobia")) {
            return responseLibrary.getRandomResponses("phasmophobia");
        } else if (toLower.contains("nightreign")) {
            return responseLibrary.getRandomResponses("nightreign");
        } else if (toLower.contains("blender")) {
            return responseLibrary.getRandomResponses("blender");
        } else if (toLower.contains("bandicam") || toLower.contains("obs64")) {
            return responseLibrary.getRandomResponses("recording");
        } else if (toLower.contains("torrent")) {
            return responseLibrary.getRandomResponses("torrent");
        } else if (toLower.contains("amneziavpn")) {
            return responseLibrary.getRandomResponses("amnezia");
        } else if (toLower.contains("minecraft")) {
            return responseLibrary.getRandomResponses("minecraft");
        } else if (toLower.contains("kaspersky")) {
            return responseLibrary.getRandomResponses("kaspersky");
        }



        else {
            return responseLibrary.getRandomResponses("default");
        }

    }
}
