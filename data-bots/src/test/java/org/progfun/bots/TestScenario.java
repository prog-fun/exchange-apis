package org.progfun.bots;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.progfun.InvalidFormatException;
import org.progfun.Market;
import org.progfun.websocket.Parser;
import static org.junit.Assert.*;
import org.progfun.Decimal;
import org.progfun.orderbook.Book;

/**
 * A class that can run a pre-described test scenario
 */
public class TestScenario {

    private List<String> messages;
    // The market as it is expected to be at the end of all tests
    private Market expectedMarket;

    /**
     * Load test scenario from a file
     *
     * @param resourceFilePath relative path of the file, inside the resources
     * folder
     * @return test scenario object or null if it was not found
     */
    public static TestScenario loadFromFile(String resourceFilePath) {
        try {
            URL filePath = TestScenario.class.getResource(resourceFilePath);
            URI uri = filePath.toURI();
            System.out.println("Test data file path: " + filePath);
            byte[] encoded = Files.readAllBytes(Paths.get(uri));
            String fileContent = new String(encoded, "UTF8");

            // Split the content in messages
            String[] parts = fileContent.split("/PART-SEPARATOR/");
            if (parts == null || parts.length != 2) {
                System.out.println("Error while parsing test scenario file, "
                        + " could not identify file parts correctly");
                return null;
            }
            return createFromJSON(parts[0]);
        } catch (IOException ex) {
            System.out.println("Error while parsing message sample file: "
                    + ex.getMessage());
            ex.printStackTrace();
        } catch (URISyntaxException ex) {
            System.out.println("Error in URI conversion: " + ex.getMessage());
        }
        // If we got here, an error occurred
        return null;

    }

    /**
     * Create a scenario from a JSON string
     *
     * @param part
     * @return
     */
    private static TestScenario createFromJSON(String json) {
        try {
            // Check if we got all the expected fields
            JSONObject main = new JSONObject(json);
            if (!main.has("messages")) {
                System.out.println("Test scenario JSON is missing messages!");
                return null;
            }
            if (!main.has("market")) {
                System.out.println("Test scenario JSON is missing market!");
                return null;
            }

            if (!main.has("expected_result")) {
                System.out.println("Test scenario JSON is missing expected_result!");
                return null;
            }
            JSONObject results = main.getJSONObject("expected_result");
            if (!results.has("bids")) {
                System.out.println("Test scenario JSON is missing expected_result bids!");
                return null;
            }
            if (!results.has("asks")) {
                System.out.println("Test scenario JSON is missing expected_result asks!");
                return null;
            }
            JSONObject market = main.getJSONObject("market");
            if (!market.has("base_currency")) {
                System.out.println("Test scenario JSON is missing market base_currency!");
                return null;
            }
            if (!market.has("quote_currency")) {
                System.out.println("Test scenario JSON is missing market quote_currency!");
                return null;
            }

            TestScenario scenario = new TestScenario();

            // Parse market
            scenario.expectedMarket = new Market(market.getString("base_currency"),
                    market.getString("quote_currency"));

            // Parse messages
            JSONArray messages = main.getJSONArray("messages");
            scenario.messages = new ArrayList<>();
            for (int i = 0; i < messages.length(); ++i) {
                // Message can be an array, and can be an object
                // We want to convert it back to a string
                Object m = messages.get(i);
                String msgString = null;
                if (m instanceof JSONObject) {
                    msgString = ((JSONObject) m).toString();
                } else if (m instanceof JSONArray) {
                    msgString = ((JSONArray) m).toString();
                } else if (m instanceof String) {
                    msgString = ((String) m);
                } else {
                    System.out.println("Invalid message found: " + m);
                    return null;
                }
                scenario.messages.add(msgString);
            }

            // Parse expected results
            JSONArray expectedBids = results.getJSONArray("bids");
            for (int i = 0; i < expectedBids.length(); ++i) {
                JSONObject bid = expectedBids.getJSONObject(i);
                scenario.expectedMarket.addBid(
                        new Decimal(bid.getDouble("price")),
                        new Decimal(bid.getDouble("amount")),
                        bid.getInt("count"));
            }
            JSONArray expectedAsks = results.getJSONArray("asks");
            for (int i = 0; i < expectedAsks.length(); ++i) {
                JSONObject ask = expectedAsks.getJSONObject(i);
                scenario.expectedMarket.addAsk(
                        new Decimal(ask.getDouble("price")),
                        new Decimal(ask.getDouble("amount")),
                        ask.getInt("count"));
            }

            return scenario;
        } catch (JSONException ex) {
            System.out.println("Error while parsing test scenario JSON: "
                    + ex.getMessage());
        } catch (InvalidFormatException ex) {
            System.out.println("Invalid market currencies:" + ex.getMessage());
        }
        // We get here only on error
        return null;
    }

    /**
     * Runs the test. Uses asserts to check the expected results
     *
     * @param parser
     */
    public void runTest(Parser parser) {
        assertNotNull(parser);
        try {
            // We start with an empty market, having the correct currencies
            Market market = new Market(
                    expectedMarket.getBaseCurrency(),
                    expectedMarket.getQuoteCurrency()
            );

            parser.setMarket(market);

            for (String msg : messages) {
                parser.parseMessage(msg);
            }

            // Now check if the resulting orderboog corresponds to our expectations
            Book bids = market.getBids();
            Book expectedBids = expectedMarket.getBids();
            assertEquals(expectedBids, bids);
            
            Book asks = market.getAsks();
            Book expectedAsks = expectedMarket.getAsks();
            assertEquals(expectedAsks, asks);

        } catch (InvalidFormatException ex) {
            System.out.println("Invalid market currencies: " + ex.getMessage());
        }
    }
}
