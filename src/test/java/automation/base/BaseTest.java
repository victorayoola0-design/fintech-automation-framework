package automation.base;

import automation.config.EnvironmentConfig;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public class BaseTest {
    protected static WireMockServer wireMockServer;

    @BeforeAll
    public static void globalSetup() {
        RestAssured.baseURI = "http://localhost:8081/api/v1";

        wireMockServer = new WireMockServer(wireMockConfig().port(8081));
        wireMockServer.start();
    }

    @BeforeEach
    public void resetEnvironmentState() {
        try (Connection conn = DriverManager.getConnection(EnvironmentConfig.DB_URL,EnvironmentConfig.DB_USER,EnvironmentConfig.DB_PASSWORD);
             Statement stmt = conn.createStatement()){
            stmt.execute("UPDATE accounts SET balance = 150.00 WHERE account_id = ACC123");
            stmt.execute("DELETE FROM transaction_logs WHERE account_id = 'ACC123';");
        }
        catch (Exception e){
            System.out.println("Database seeding skipped or table not initialized yet:" + e.getMessage());
        }
        wireMockServer.resetAll();
    }
    @AfterAll
    public static void globalTeardown(){
        if (wireMockServer != null && wireMockServer.isRunning()){
            wireMockServer.stop();
        }
    }
}


