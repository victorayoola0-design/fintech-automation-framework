package automation.client;

import com.github.tomakehurst.wiremock.WireMockServer;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class GatewayMockController{
    private final WireMockServer wireMockServer;

    public GatewayMockController(WireMockServer wireMockServer){
        this.wireMockServer =  wireMockServer;
    }

    public void stubHealthyGateway(){
        wireMockServer.stubFor(post(urlEqualTo("/gateway/qcb-rail")).willReturn(aResponse()
                .withStatus(200).withHeader("Content-Type","application/json").withBody("{\"status\": " +
                        "\"SUCCESS\", \"authCode\": \"QCB-9982\"}")));
    }

    public void stubTimeoutGateway(int delayMillis){
        wireMockServer.stubFor(post(urlEqualTo("/gateway/qcb-rail")).willReturn(aResponse().withStatus(200).
                withFixedDelay(delayMillis)));
    }
}