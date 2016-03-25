package nl.vpro.amaraimporter;


import nl.vpro.rs.media.MediaRestClient;

public class Importer {


	public static void main(String[] argv) {
		MediaRestClient client = new MediaRestClient();
		client.setTrustAll(true);
		client.setUserName("vpro-mediatools");
		client.setPassword("HAAL WATCHWOORD UIT CONFIG FILE");
		client.setUrl("https://api-test.poms.omroep.nl/");
		client.setThrottleRate(50);
		client.setWaitForRetry(true);
	}
}
