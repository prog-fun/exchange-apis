package org.progfun.gemini;

import org.progfun.connector.Parser;

/**
 * Parses JSON updates from Gemini API
 */
public class GeminiParser implements Parser {

    @Override
    public void onMessage(String message) {
        System.out.println("Received: " + message);
        // TODO
    }

    @Override
    public void onError(Exception excptn) {
        System.out.println("Error: " + excptn);
    }
}
