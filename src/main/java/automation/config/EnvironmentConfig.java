package automation.config;

public class EnvironmentConfig {
    public static final String BASE_URL = System.getProperty("baseUrl", "http://localhost:8080/api/v1");
    public static final int MOCK_SERVER_PORT = 8081;
    public static final String MOCK_SERVER_URL = "http://localhost:" + MOCK_SERVER_PORT;
    public static final String DB_URL = "jdbc:postgresql://localhost:5432/fintech-test-db";
    public static final String DB_USER = "postgres";
    public static final String DB_PASSWORD = "mysecretpassword";
}
