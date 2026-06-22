package automation.functional;

import automation.base.BaseTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class GatewayTimeoutResiliencyTest extends BaseTest {

    @Test
    public void testExternalGateTimeout_ShouldTriggerInternalCompensatingRollback(){
        wireMockServer.stubFor(post(urlEqualTo("/gateway/qcb-rail")).willReturn(aResponse().
                withStatus(200).withFixedDelay(10000)));

        Response response = RestAssured.given().header("Content-Type", "application/json").
                header("X-Transaction-ID", java.util.UUID.randomUUID().toString()).body(
                        "{\"accountId\": \"ACC123\", \"amount\": 100.00 }").post("/escrow/initiate");

        assertEquals(504, response.getStatusCode(),"Resiliency Fault: The orchestrator did not handle downstream network with a 504 status.");

        double finalLedgerBalance = RestAssured.get("/ledger/balance/ACC123").jsonPath().getDouble("balance");
        assertEquals(150.00,finalLedgerBalance, "DATA INTEGRITY BREAKDOWN: The downstream transaction timed out, but local funds were locked or permanently debited!");
    }
}
