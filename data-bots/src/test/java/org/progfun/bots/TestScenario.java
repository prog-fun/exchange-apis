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
import org.progfun.Channel;
import org.progfun.CurrencyPair;
import org.progfun.Decimal;
import org.progfun.Exchange;
import org.progfun.Subscription;
import org.progfun.Subscriptions;
import org.progfun.orderbook.Book;
import org.progfun.websocket.WebSocketHandler;

/**
 * A class that can run a pre-described test scenario
 */
public class TestScenario {

    private List<String> messages;
    // The expected exchange at the end of all tests
    private Exchange expectedExchange;
    private Subscriptions subscriptions = new Subscriptions();

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

            if (!main.has("expected_results")) {
                System.out.println("Test scenario JSON is missing expected_results!");
                return null;
            }
            JSONObject results = main.getJSONObject("expected_results");

            if (!results.has("markets")) {
                System.out.println("Test scenario JSON is missing market!");
                return null;
            }

            TestScenario scenario = new TestScenario();

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
            scenario.expectedExchange = new Exchange();
            JSONArray markets = results.getJSONArray("markets");
            for (int i = 0; i < markets.length(); ++i) {
                // Create expected market
                JSONObject m = markets.getJSONObject(i);
                if (!m.has("base_currency")) {
                    System.out.println("Test scenario JSON is missing market base_currency!");
                    return null;
                }
                if (!m.has("quote_currency")) {
                    System.out.println("Test scenario JSON is missing market quote_currency!");
                    return null;
                }
                Market market = new Market(m.getString("base_currency"),
                        m.getString("quote_currency"));
                scenario.expectedExchange.addMarket(market);
                
                // Add expected bids and asks
                if (m.has("bids")) {
                    JSONArray expectedBids = m.getJSONArray("bids");
                    for (int j = 0; j < expectedBids.length(); ++j) {
                        JSONObject bid = expectedBids.getJSONObject(j);
                        market.addBid(new Decimal(bid.getDouble("price")),
                                new Decimal(bid.getDouble("amount")),
                                bid.getInt("count"));
                    }
                }
                if (m.has("asks")) {
                    JSONArray expectedAsks = m.getJSONArray("asks");
                    for (int j = 0; j < expectedAsks.length(); ++j) {
                        JSONObject ask = expectedAsks.getJSONObject(j);
                        market.addAsk(new Decimal(ask.getDouble("price")),
                                new Decimal(ask.getDouble("amount")),
                                ask.getInt("count"));
                    }
                }
                
                // Create subscription
                Subscription s = scenario.subscriptions.add(market, Channel.ORDERBOOK);
                // Set subcription ID that will be references by the response msg
                if (m.has("subscription_id")) {
                    String subsId = m.getString("subscription_id");
                    scenario.subscriptions.setInactiveId(subsId, s);
                }
                // TODO - support different channels - trades, etc
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
     * @param handler
     */
    public void runTest(WebSocketHandler handler) {
        Parser parser = handler.createParser();
        assertNotNull(parser);
        try {
            // Prepare mock exchange
            Exchange exchange = cloneExchange(expectedExchange);
            parser.setExchange(exchange);

            // Prepare mock subscriptions
            Subscriptions subs = new Subscriptions();
            // TODO - allow to specify subscriptions in test scenario files
            for (Market m : exchange.getMarkets()) {
                Subscription s = subs.add(m, Channel.ORDERBOOK);
                String symbol = handler.getSymbolForMarket(m.getCurrencyPair());
                String subsId = Parser.getInactiveSubsSymbol(symbol, Channel.ORDERBOOK);
                subs.setInactiveId(subsId, s);
            }
            parser.setSubscriptions(subs);
            
            
            for (String msg : messages) {
                parser.parseMessage(msg);
            }

            // Now check if the resulting markets are equal
            for (Market market : exchange.getMarkets()) {
                Book bids = market.getBids();
                CurrencyPair cp = market.getCurrencyPair();
                Market expectedMarket = expectedExchange.getMarket(cp);
                Book expectedBids = expectedMarket.getBids();
                assertEquals(expectedBids, bids);
                Book asks = market.getAsks();
                Book expectedAsks = expectedMarket.getAsks();
                assertEquals(expectedAsks, asks);
            }

        } catch (InvalidFormatException ex) {
            System.out.println("Invalid market currencies: " + ex.getMessage());
        }
    }

    /**
     * Create a clone of exchange e, without real data (bids, asks, trades, etc)
     * but with the same markets
     *
     * @param originalExchange
     * @return
     */
    private Exchange cloneExchange(Exchange originalExchange) {
        Exchange e = new Exchange();
        e.setSymbol(originalExchange.getSymbol());
        for (Market m : originalExchange.getMarkets()) {
            e.addMarket(new Market(m.getCurrencyPair()));
        }
        return e;
    }
}
