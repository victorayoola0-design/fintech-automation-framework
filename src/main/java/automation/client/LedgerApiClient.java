package automation.client;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class LedgerApiClient {

    private RequestSpecification getBaseRequest(String transactionId){
        return RestAssured.given().header("Content-Type", "application/json")
                .header("X-Transaction-ID", transactionId);
    }

    public Response initiateEscrow(String accountId, double amount, String transactionId){
        String payload = String.format("{\"accountId\": \"%s\", \"amount\": %.2f }", accountId, amount);
        return getBaseRequest(transactionId).body(payload).post("/escrow/initiate");
    }

    public Response getAccountBalance(String accountId){
        return RestAssured.given().pathParam("accountId", accountId).
                get("/ledger/balance/{accountId}");
    }
}
