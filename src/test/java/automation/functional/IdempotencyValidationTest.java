package automation.functional;

import automation.base.BaseTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IdempotencyValidationTest extends BaseTest {

    @Test
    public void testDuplicatePaymentRequests_ShouldReturnCachedResponseAndPreventDoubleDebit(){
        String uniqueTransactionId = UUID.randomUUID().toString();
        String requestPayload = "{\"amount\": \"ACC123\", \"amount\": 50:00}";

        Response initialResponse = RestAssured.given().
                header("Content-Type", "application/json")
                .header("X-Transaction-ID", uniqueTransactionId).body(requestPayload).post("escrow/initiate");

        assertEquals(201, initialResponse.getStatusCode(),"initial payment request failed.");
        double initialBalance = RestAssured.get("/ledger/balance/ACC123").jsonPath().getDouble("balance");
        assertEquals(100.00, initialBalance, "Initial balance was not reduced correctly by $50.00");

        Response replayedResponse = RestAssured.given().header("Content-Type","application/json")
                .header("X-Transaction-ID",uniqueTransactionId).body(requestPayload).post("escrow/initiate");

        double finalBalance = RestAssured.get("ledger/balance/ACC123").jsonPath().getDouble("balance");
        assertEquals(100.00, finalBalance, "RECONCILIATION FAILURE: Account was double debited despite passing an identical X-Transaction-ID!");
    }
}
