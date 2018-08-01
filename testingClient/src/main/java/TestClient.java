import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;

import java.util.Calendar;
import java.util.List;

import org.hl7.davinci.CrdRequestCreator;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.EligibilityRequest;
import org.hl7.fhir.r4.model.EligibilityResponse;
import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




public class TestClient {
  static final Logger logger = LoggerFactory.getLogger(TestClient.class);

  /**
   * Sets up the context and client and runs the test.
   * @param args main function args
   */
  public static void main(String[] args) {
    // Create a client to talk to the server


    FhirContext ctx = FhirContext.forR4();
    IGenericClient client = ctx.newRestfulGenericClient("http://localhost:8080/fhir/");
    client.registerInterceptor(new LoggingInterceptor(true));

    runCrd(client);
  }

  /**
   * Runs the sample request of the fhir server.
   * @param client the client to make the request
   */
  public static void runCrd(IGenericClient client) {
    // build the parameters for the CRD
    Calendar cal = Calendar.getInstance();
    cal.set(1948, Calendar.JULY, 4);
    Parameters crdParams = CrdRequestCreator
        .createRequest(Enumerations.AdministrativeGender.MALE, cal.getTime());

    // call the CRD operation
    Parameters retParams = client.operation()
        .onServer()
        .named("$coverage-requirements-discovery")
        .withParameters(crdParams)
        .returnResourceType(Parameters.class)
        .execute();

    // make sure the return parameters are valid
    if (retParams == null) {
      System.out.println("ERROR: retParams is null");
      return;
    }

    // parse the return parameters to get each object
    EligibilityResponse eligibilityResponse = null;
    Practitioner returnProvider = null;
    EligibilityRequest returnEligibilityRequest = null;
    Organization returnInsurer = null;
    Coverage returnCoverage = null;
    Endpoint returnEndpoint = null;

    printResource(retParams);

    List<Parameters.ParametersParameterComponent> paramList = retParams
        .getParameter()
        .get(0)
        .getPart();

    for (Parameters.ParametersParameterComponent part : paramList) {
      switch (part.getName()) {
        case "eligibilityResponse":
          eligibilityResponse = (EligibilityResponse) part.getResource();
          System.out.println("CRD: got response.eligibilityResponse");
          break;
        case "requestProvider":
          returnProvider = (Practitioner) part.getResource();
          System.out.println("CRD: got response.requestProvider");
          break;
        case "request":
          returnEligibilityRequest = (EligibilityRequest) part.getResource();
          System.out.println("CRD: got response.request");
          break;
        case "insurer":
          returnInsurer = (Organization) part.getResource();
          System.out.println("CRD: got response.insurer");
          break;
        case "coverage":
          returnCoverage = (Coverage) part.getResource();
          System.out.println("CRD: got response.coverage");
          break;
        case "endPoint":
          returnEndpoint = (Endpoint) part.getResource();
          System.out.println("CRD: got response.endpoint");
          break;
        case "service":
          ResourceType serviceType = part.getResource().getResourceType();
          switch (serviceType) {
            case Procedure:
              System.out.println("CRD: got response.service of type Procedure");
              break;
            case HealthcareService:
              System.out.println("CRD: got response.service of type HealthcareService");
              break;
            case ServiceRequest:
              System.out.println("CRD: got response.service of type ServiceRequest");
              break;
            case MedicationRequest:
              System.out.println("CRD: got response.service of type MedicationRequest");
              break;
            case Medication:
              System.out.println("CRD: got response.service of type Medication");
              break;
            case Device:
              System.out.println("CRD: got response.service of type Device");
              break;
            case DeviceRequest:
              System.out.println("CRD: got response.service of type DeviceRequest");
              break;
            default:
              System.out.println("Warning: unexpected response.service type");
              break;
          }
          break;
        default:
          System.out.println("Warning: unexpected parameter part: " + part.getName());
          break;
      }
    }

    System.out.println("returned from CRD call!");
    if (eligibilityResponse != null) {
      System.out.println("CRD Disposition: " + eligibilityResponse.getDisposition());
    } else {
      System.out.println("ERROR: eligibilityResponse is null");
    }
  }

  static void printResource(Resource obj) {
    FhirContext ctx = FhirContext.forR4();
    String encoded = ctx.newXmlParser().encodeResourceToString(obj);
    System.out.println("\n" + encoded + "\n");
  }
}