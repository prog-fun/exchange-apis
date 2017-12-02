package org.progfun.bots.gdax;

import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONObject;
import org.progfun.Decimal;
import org.progfun.connector.AbstractParser;

/**
 * Handles the responses from the GDAX API and fills the orderbook with
 * information from the responses
 */
public class GdaxParser extends AbstractParser {

    /**
     * Handles the incoming messages from the API and puts it in the orderbook
     * See message documentation: https://docs.gdax.com/#websocket-feed
     *
     * @param message The incoming message
     */
    @Override
    public void onMessage(String message) {
        JSONObject JSONMessage = new JSONObject(message);
        String type = JSONMessage.getString("type");
        if (type.equals("snapshot")) {
            JSONArray bids = JSONMessage.getJSONArray("bids");
            for (int i = 0; i < bids.length(); ++i) {
                JSONArray bid = bids.getJSONArray(i);
                market.addBid(bid.getString(0), bid.getString(1), 0);
            }
            JSONArray asks = JSONMessage.getJSONArray("asks");
            for (int i = 0; i < asks.length(); ++i) {
                JSONArray ask = asks.getJSONArray(i);
                market.addAsk(ask.getString(0), ask.getString(1), 0);
            }
        } else if (type.equals("l2update")) {
            JSONArray changes = JSONMessage.getJSONArray("changes");
            for (int i = 0; i < changes.length(); ++i) {
                JSONArray change = changes.getJSONArray(i);
                String side = change.getString(0);
                Decimal price = new Decimal(change.getDouble(1));
                String amountString = change.getString(2);

                // The amount is the total, not delta
                // Therefore we always remove the order first, then add
                // it back with the new amount, if necessary
                if (side.equals("buy")) {
                    market.removeBid(price);
                } else if (side.equals("sell")) {
                    market.removeAsk(price);
                }
                if (!amountString.equals("0")) {
                    Decimal amount = new Decimal(amountString);
                    if (side.equals("buy")) {
                        market.addBid(price, amount, 0);
                    } else if (side.equals("sell")) {
                        market.addAsk(price, amount, 0);
                    }
                }
            }
        }
    }

}
