package endpoint.cdshooks.services.crd;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class ServicesDiscoveryEndpointTest {

  @LocalServerPort
  private int port;

  @Autowired
  private TestRestTemplate restTemplate;

  @Test
  public void shouldReturnServiceList() {
    JsonNode cdsServiceInformation = restTemplate.getForObject(
        "http://localhost:" + port + "/cds-services/", JsonNode.class);
    JsonNode orderReviewCrd = cdsServiceInformation.get("services").get(0);
    JsonNode medicationPrescribeCrd = cdsServiceInformation.get("services").get(1);

    assertEquals(orderReviewCrd.get("id").textValue(), "order-review-crd");
    assertEquals(orderReviewCrd.get("hook").textValue(), "order-review");

    assertEquals(medicationPrescribeCrd.get("id").textValue(), "medication-prescribe-crd");
    assertEquals(medicationPrescribeCrd.get("hook").textValue(), "medication-prescribe");
  }
}

