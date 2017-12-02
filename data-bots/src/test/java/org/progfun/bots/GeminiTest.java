package org.progfun.bots;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import org.junit.Test;
import org.progfun.InvalidFormatException;
import org.progfun.Market;
import static org.junit.Assert.*;
import org.progfun.Decimal;
import org.progfun.bots.gemini.GeminiParser;
import org.progfun.orderbook.Book;
import org.progfun.orderbook.Order;

public class GeminiTest {

    // Test a realistic scenario with predefined messages stored in a file
    @Test
    public void testScenario() throws InvalidFormatException {
        // Get messages as a list of strings
        List<String> messages = prepareTestMessages();

        // There should be 9 test messages
        assertEquals(9, messages.size());

        // Create a marker, feed all messages to GeminiParser 
        Market market = new Market("BTC", "USD");
        GeminiParser parser = new GeminiParser();
        parser.setMarket(market);

        for (String msg : messages) {
            parser.onMessage(msg);
        }

        // Now check if the resulting orderboog corresponds to our expectations
        Book bids = market.getBids();
        Book asks = market.getAsks();

        assertEquals(2, bids.size());
        assertEquals(2, asks.size());
        Order b1 = bids.getOrderForPrice(new Decimal(701));
        Order b2 = bids.getOrderForPrice(new Decimal(703));
        assertNotNull(b1);
        assertNotNull(b2);
        assertEquals(new Decimal(20), b1.getAmount());
        assertEquals(new Decimal(45), b2.getAmount());
        Order a1 = asks.getOrderForPrice(new Decimal(741));
        Order a2 = asks.getOrderForPrice(new Decimal(751));
        assertNotNull(a1);
        assertNotNull(a2);
        assertEquals(new Decimal(0.8), a1.getAmount());
        assertEquals(new Decimal(1.5), a2.getAmount());
    }

    public static List<String> prepareTestMessages() {
        // Read the message file
        List<String> messages = new LinkedList<>();
        try {
            URL filePath = GeminiTest.class.getResource("/gemini/gemini-test-messages.txt");
            URI uri = filePath.toURI();
            System.out.println("Test data file path: " + filePath);
            byte[] encoded = Files.readAllBytes(Paths.get(uri));
            String fileContent = new String(encoded, "UTF8");

            // Split the content in messages
            String[] parts = fileContent.split("/MESAGE-SEPARATOR/");
            for (String p : parts) {
                if (p.contains("EOF-MESSAGES")) {
                    break;
                }
                messages.add(p);
            }
        } catch (IOException ex) {
            System.out.println("Error while parsing message sample file: "
                    + ex.getMessage());
            ex.printStackTrace();
        } catch (URISyntaxException ex) {
            System.out.println("Error in URI conversion: " + ex.getMessage());
        }
        return messages;
    }
}
