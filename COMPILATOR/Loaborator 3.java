
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.nio.file.Path;

abstract class FileDetails {
    protected String name;
    protected String createdTime;
    protected String updatedTime;

    public FileDetails(String name, String createdTime, String updatedTime) {
        this.name = name;
        this.createdTime = createdTime;
        this.updatedTime = updatedTime;
    }

    public abstract void getInfo();
}

class TextFile extends FileDetails {
    private int lineCount;
    private int wordCount;
    private int charCount;

    public TextFile(String name, String createdTime, String updatedTime, int lineCount, int wordCount, int charCount) {
        super(name, createdTime, updatedTime);
        this.lineCount = lineCount;
        this.wordCount = wordCount;
        this.charCount = charCount;
    }

    @Override
    public void getInfo() {
        System.out.println("Text File: " + name);
        System.out.println("Created Time: " + createdTime);
        System.out.println("Updated Time: " + updatedTime);
        System.out.println("Line Count: " + lineCount);
        System.out.println("Word Count: " + wordCount);
        System.out.println("Character Count: " + charCount);
    }
}

class ImageFile extends FileDetails {
    private String imageSize;

    public ImageFile(String name, String createdTime, String updatedTime, String imageSize) {
        super(name, createdTime, updatedTime);
        this.imageSize = imageSize;
    }

    @Override
    public void getInfo() {
        System.out.println("Image File: " + name);
        System.out.println("Created Time: " + createdTime);
        System.out.println("Updated Time: " + updatedTime);
        System.out.println("Image Size: " + imageSize);
    }
}

class ProgramFile extends FileDetails {
    private int lineCount;
    private int classCount;
    private int methodCount;

    public ProgramFile(String name, String createdTime, String updatedTime, int lineCount, int classCount,
            int methodCount) {
        super(name, createdTime, updatedTime);
        this.lineCount = lineCount;
        this.classCount = classCount;
        this.methodCount = methodCount;
    }

    @Override
    public void getInfo() {
        System.out.println("Program File: " + name);
        System.out.println("Created Time: " + createdTime);
        System.out.println("Updated Time: " + updatedTime);
        System.out.println("Line Count: " + lineCount);
        System.out.println("Class Count: " + classCount);
        System.out.println("Method Count: " + methodCount);
    }
}

class FolderMonitor {
    private String folderPath;
    private Map<String, FileDetails> files;
    private String snapshotTime;

    public FolderMonitor(String folderPath) {
        this.folderPath = folderPath;
        this.files = new HashMap<>();
        this.snapshotTime = "";
    }

    public void commit() {
        snapshotTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    public void info(String filename) {
        FileDetails file = files.get(filename);
        if (file != null) {
            file.getInfo();
        } else {
            System.out.println("File not found.");
        }
    }

    public void status() {
        System.out.println("Status:");
        for (Map.Entry<String, FileDetails> entry : files.entrySet()) {
            String filename = entry.getKey();
            FileDetails file = entry.getValue();
            if (!file.updatedTime.equals(snapshotTime)) {
                System.out.println(filename + " has been changed since the snapshot time.");
            } else {
                System.out.println(filename + " has not been changed since the snapshot time.");
            }
        }
    }

    public void detectChanges() {
        Map<String, FileDetails> newFiles = new HashMap<>();
        try {
            Files.walkFileTree(Paths.get(folderPath), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String filename = file.toFile().getName();
                    String createdTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                            .format(new Date(file.toFile().lastModified()));
                    String updatedTime = createdTime;

                    FileDetails fileDetails = files.get(filename);
                    if (fileDetails != null) {
                        updatedTime = fileDetails.updatedTime;
                        if (!fileDetails.updatedTime.equals(createdTime)) {
                            System.out.println("File '" + filename + "' has been modified.");
                        }
                    } else {
                        System.out.println("File '" + filename + "' is added.");
                    }

                    if (filename.endsWith(".txt")) {
                        // Logic to get line count, word count, and character count for text file
                        int lineCount = 0; // Placeholder
                        int wordCount = 0; // Placeholder
                        int charCount = 0; // Placeholder
                        newFiles.put(filename,
                                new TextFile(filename, createdTime, updatedTime, lineCount, wordCount, charCount));
                    } else if (filename.endsWith(".png") || filename.endsWith(".jpg")) {
                        // Logic to get image size for image file
                        String imageSize = ""; // Placeholder
                        newFiles.put(filename, new ImageFile(filename, createdTime, updatedTime, imageSize));
                    } else if (filename.endsWith(".py") || filename.endsWith(".java")) {
                        // Logic to get line count, class count, and method count for program file
                        int lineCount = 0; // Placeholder
                        int classCount = 0; // Placeholder
                        int methodCount = 0; // Placeholder
                        newFiles.put(filename, new ProgramFile(filename, createdTime, updatedTime, lineCount,
                                classCount, methodCount));
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (Map.Entry<String, FileDetails> entry : files.entrySet()) {
            if (!newFiles.containsKey(entry.getKey())) {
                System.out.println("File '" + entry.getKey() + "' is deleted.");
            }
        }

        files = newFiles;
    }

    public void start() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::detectChanges, 0, 5, TimeUnit.SECONDS);
    }
}
