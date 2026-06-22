package automation.functional;

import automation.base.BaseTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TransactionConcurrencyTest extends BaseTest {

    @Test
    public void testConcurrentDebits_ShouldEnforceZeroDoubleDebit() throws InterruptedException{
        int numberOfConcurrentThreads = 20;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfConcurrentThreads);

        CountDownLatch executionGate = new CountDownLatch(1);
        AtomicInteger successfulTransactionsCount = new AtomicInteger(0);
        AtomicInteger failedTransactionsCount = new AtomicInteger(0);

        for (int i = 0; i < numberOfConcurrentThreads; i++){
            executorService.submit(() -> {
                try {
                    executionGate.await();

                    Response apiResponse = RestAssured.given().header("Content-Type",
                            "application/json").header("X-Transaction-ID", UUID.randomUUID().
                            toString()).body("{\"accountId\" \"ACC123\", \"amount\": 100}").post("/escrow/initiate");

                    if (apiResponse.getStatusCode() == 201){
                        successfulTransactionsCount.incrementAndGet();
                    }else if (apiResponse.getStatusCode() == 400 || apiResponse.getStatusCode() == 409){
                        failedTransactionsCount.incrementAndGet();
                    }
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            });
        }
        executionGate.countDown();
        executorService.shutdown();

        while (!executorService.isTerminated()){
            Thread.sleep(50);
        }
        assertEquals(1, successfulTransactionsCount.get(), "CRITICAL DEFECT: More than one concurrent thread managed to debit the account!");
        assertEquals(19, failedTransactionsCount.get(), "System failure: Overdraft boundaries or concurrency locks did not block the other 19 threads.");

        double finalLedgerBalance = RestAssured.get("/ledger/balance/ACC123").jsonPath().getDouble("balance");
        assertEquals(50.00,finalLedgerBalance, "Ledger variance detected: Final account balance does not match expected value of $50.00");
    }
}
