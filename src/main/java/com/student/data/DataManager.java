package com.student.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.student.model.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DataManager {
    private static final String DATA_DIR = System.getProperty("user.home") + "/.studyhub";
    private static final String DATA_FILE = DATA_DIR + "/data.json";
    private static final String BACKUP_DIR = DATA_DIR + "/backups";

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .setPrettyPrinting()
            .create();

    private static AppData appData;

    static {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
            Files.createDirectories(Paths.get(BACKUP_DIR));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static AppData loadData() {
        if (appData != null) {
            return appData;
        }

        File file = new File(DATA_FILE);
        if (file.exists()) {
            try (Reader reader = new FileReader(file)) {
                appData = gson.fromJson(reader, AppData.class);
                if (appData == null) {
                    appData = new AppData();
                }
            } catch (IOException e) {
                e.printStackTrace();
                appData = new AppData();
            }
        } else {
            appData = new AppData();
            createDefaultUser();
        }

        return appData;
    }

    public static void saveData() {
        if (appData == null) {
            appData = new AppData();
        }

        try (Writer writer = new FileWriter(DATA_FILE)) {
            gson.toJson(appData, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void createBackup() {
        String timestamp = java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
        );
        String backupFile = BACKUP_DIR + "/backup_" + timestamp + ".json";

        try {
            Files.copy(Paths.get(DATA_FILE), Paths.get(backupFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void restoreBackup(String backupFile) {
        try {
            Files.copy(Paths.get(backupFile), Paths.get(DATA_FILE),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            appData = null;
            loadData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return password;
        }
    }

    public static boolean verifyPassword(String password, String hash) {
        return hashPassword(password).equals(hash);
    }

    private static void createDefaultUser() {
        User admin = new User("admin", hashPassword("1234"));
        admin.setFullName("Administrator");
        appData.getUsers().add(admin);
        saveData();
    }

    public static User findUser(String username) {
        return appData.getUsers().stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    public static boolean addUser(String username, String password, String email, String fullName) {
        if (findUser(username) != null) {
            return false;
        }
        User user = new User(username, hashPassword(password), email, fullName);
        appData.getUsers().add(user);
        saveData();
        return true;
    }

    public static void exportToCSV(String filePath, String type) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            switch (type) {
                case "expenses":
                    writer.println("Name,Category,Amount,Date");
                    for (Expense e : appData.getExpenses()) {
                        writer.printf("%s,%s,%d,%s%n",
                                e.getName(), e.getCategory(), e.getValue(), e.getDate());
                    }
                    break;
                case "tasks":
                    writer.println("Name,Priority,Date,Status,Subject");
                    for (Task t : appData.getTasks()) {
                        writer.printf("%s,%s,%s,%s,%s%n",
                                t.getName(), t.getPriority(), t.getDate(), t.getStatus(), t.getSubject());
                    }
                    break;
                case "events":
                    writer.println("Title,Date,Location,Status,Type");
                    for (EventItem e : appData.getEvents()) {
                        writer.printf("%s,%s,%s,%s,%s%n",
                                e.getTitle(), e.getDate(), e.getLocation(), e.getStatus(), e.getType());
                    }
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static AppData getAppData() {
        if (appData == null) {
            loadData();
        }
        return appData;
    }

    public static void resetData() {
        appData = new AppData();
        createDefaultUser();
        saveData();
    }

    static class LocalDateAdapter implements com.google.gson.JsonSerializer<LocalDate>,
            com.google.gson.JsonDeserializer<LocalDate> {
        private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

        @Override
        public LocalDate deserialize(com.google.gson.JsonElement json,
                java.lang.reflect.Type typeOfT,
                com.google.gson.JsonDeserializationContext context) throws com.google.gson.JsonParseException {
            return LocalDate.parse(json.getAsString(), formatter);
        }

        @Override
        public com.google.gson.JsonElement serialize(LocalDate src,
                java.lang.reflect.Type typeOfSrc,
                com.google.gson.JsonSerializationContext context) {
            return new com.google.gson.JsonPrimitive(src.format(formatter));
        }
    }
}
