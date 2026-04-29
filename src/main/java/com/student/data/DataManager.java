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
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataManager {
    private static final String DATA_DIR = System.getProperty("user.home") + "/.studyhub";
    private static final String LEGACY_DATA_FILE = DATA_DIR + "/data.json";
    private static final String DB_FILE = DATA_DIR + "/studyhub.db";
    private static final String BACKUP_DIR = DATA_DIR + "/backups";
    private static final String DB_URL = "jdbc:sqlite:" + DB_FILE;
    private static final int APP_STATE_ID = 1;

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .setPrettyPrinting()
            .create();

    private static AppData appData;

    static {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
            Files.createDirectories(Paths.get(BACKUP_DIR));
            Class.forName("org.sqlite.JDBC");
            initializeDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static synchronized AppData loadData() {
        if (appData != null) {
            return appData;
        }

        ensureDatabaseReady();
        appData = loadAppStateFromDatabase();
        if (appData == null) {
            appData = loadLegacyDataFile();
        }
        if (appData == null) {
            appData = new AppData();
        }

        normalizeAppData(appData);
        List<User> users = loadUsersFromDatabase();
        if (users.isEmpty() && !appData.getUsers().isEmpty()) {
            saveUsersToDatabase(appData.getUsers());
            users = loadUsersFromDatabase();
        }
        if (users.isEmpty()) {
            createDefaultUser();
            users = loadUsersFromDatabase();
        }
        appData.setUsers(users);

        saveData();

        return appData;
    }

    public static synchronized void saveData() {
        if (appData == null) {
            appData = new AppData();
        }

        normalizeAppData(appData);
        ensureDatabaseReady();
        saveUsersToDatabase(appData.getUsers());
        saveAppStateToDatabase(appData);
    }

    public static synchronized void createBackup() {
        String timestamp = java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
        );
        String backupFile = BACKUP_DIR + "/backup_" + timestamp + ".db";

        try {
            if (Files.exists(Paths.get(DB_FILE))) {
                Files.copy(Paths.get(DB_FILE), Paths.get(backupFile));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void restoreBackup(String backupFile) {
        try {
            Files.copy(Paths.get(backupFile), Paths.get(DB_FILE),
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
        if (findUser("admin") != null) {
            return;
        }
        User admin = new User("admin", hashPassword("1234"));
        admin.setFullName("Administrator");
        admin.setCreatedAt(System.currentTimeMillis());
        saveUserToDatabase(admin);
        if (appData != null) {
            appData.getUsers().add(admin);
        }
    }

    public static User findUser(String username) {
        if (appData == null) {
            loadData();
        }
        return appData.getUsers().stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    public static synchronized boolean addUser(String username, String password, String email, String fullName) {
        if (findUser(username) != null) {
            return false;
        }
        User user = new User(username, hashPassword(password), email, fullName);
        user.setCreatedAt(System.currentTimeMillis());
        saveUserToDatabase(user);
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

    public static synchronized void resetData() {
        appData = new AppData();
        ensureDatabaseReady();
        clearDatabase();
        createDefaultUser();
        saveData();
    }

    private static void initializeDatabase() {
        ensureDatabaseReady();
    }

    private static void ensureDatabaseReady() {
        try (Connection connection = DriverManager.getConnection(DB_URL);
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS app_state (id INTEGER PRIMARY KEY CHECK (id = 1), state_json TEXT NOT NULL, updated_at INTEGER NOT NULL)");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS users (username TEXT PRIMARY KEY, password_hash TEXT NOT NULL, email TEXT, full_name TEXT, created_at INTEGER NOT NULL)");
        } catch (SQLException e) {
            throw new RuntimeException("Unable to initialize database", e);
        }
    }

    private static AppData loadAppStateFromDatabase() {
        String sql = "SELECT state_json FROM app_state WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, APP_STATE_ID);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return gson.fromJson(resultSet.getString("state_json"), AppData.class);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static AppData loadLegacyDataFile() {
        File file = new File(LEGACY_DATA_FILE);
        if (!file.exists()) {
            return null;
        }

        try (Reader reader = new FileReader(file)) {
            return gson.fromJson(reader, AppData.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static List<User> loadUsersFromDatabase() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT username, password_hash, email, full_name, created_at FROM users ORDER BY created_at ASC";

        try (Connection connection = DriverManager.getConnection(DB_URL);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                User user = new User();
                user.setUsername(resultSet.getString("username"));
                user.setPasswordHash(resultSet.getString("password_hash"));
                user.setEmail(resultSet.getString("email"));
                user.setFullName(resultSet.getString("full_name"));
                user.setCreatedAt(resultSet.getLong("created_at"));
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;
    }

    private static void saveAppStateToDatabase(AppData data) {
        String sql = "INSERT INTO app_state (id, state_json, updated_at) VALUES (?, ?, ?) " +
                "ON CONFLICT(id) DO UPDATE SET state_json = excluded.state_json, updated_at = excluded.updated_at";

        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, APP_STATE_ID);
            statement.setString(2, gson.toJson(data));
            statement.setLong(3, System.currentTimeMillis());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Unable to save app state", e);
        }
    }

    private static void saveUsersToDatabase(List<User> users) {
        try (Connection connection = DriverManager.getConnection(DB_URL)) {
            connection.setAutoCommit(false);
            try (PreparedStatement delete = connection.prepareStatement("DELETE FROM users");
                 PreparedStatement insert = connection.prepareStatement(
                         "INSERT INTO users (username, password_hash, email, full_name, created_at) VALUES (?, ?, ?, ?, ?)")) {
                delete.executeUpdate();
                for (User user : users) {
                    insert.setString(1, user.getUsername());
                    insert.setString(2, user.getPasswordHash());
                    insert.setString(3, user.getEmail());
                    insert.setString(4, user.getFullName());
                    insert.setLong(5, user.getCreatedAt());
                    insert.addBatch();
                }
                insert.executeBatch();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Unable to save users", e);
        }
    }

    private static void saveUserToDatabase(User user) {
        String sql = "INSERT OR REPLACE INTO users (username, password_hash, email, full_name, created_at) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPasswordHash());
            statement.setString(3, user.getEmail());
            statement.setString(4, user.getFullName());
            statement.setLong(5, user.getCreatedAt());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Unable to save user", e);
        }
    }

    private static void clearDatabase() {
        try (Connection connection = DriverManager.getConnection(DB_URL);
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM app_state");
            statement.executeUpdate("DELETE FROM users");
        } catch (SQLException e) {
            throw new RuntimeException("Unable to clear database", e);
        }
    }

    private static void normalizeAppData(AppData data) {
        if (data.getExpenses() == null) data.setExpenses(new ArrayList<>());
        if (data.getTasks() == null) data.setTasks(new ArrayList<>());
        if (data.getEvents() == null) data.setEvents(new ArrayList<>());
        if (data.getUsers() == null) data.setUsers(new ArrayList<>());
        if (data.getCourses() == null) data.setCourses(new ArrayList<>());
        if (data.getAttendance() == null) data.setAttendance(new ArrayList<>());
        if (data.getExams() == null) data.setExams(new ArrayList<>());
        if (data.getNotes() == null) data.setNotes(new ArrayList<>());
        if (data.getSchedule() == null) data.setSchedule(new ArrayList<>());
        if (data.getGoals() == null) data.setGoals(new ArrayList<>());
        if (data.getSettings() == null) data.setSettings(new AppSettings());
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
