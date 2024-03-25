

public class Main {
    public static void main(String[] args) {
        String folderPath = "C:\\folder\\Target";
        FolderMonitor monitor = new FolderMonitor(folderPath);
        monitor.start();
    }
}
