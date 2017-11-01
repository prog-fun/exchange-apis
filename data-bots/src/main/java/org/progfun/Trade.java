package org.progfun;

import java.util.Date;

/**
 * Represents one historical trade
 */
public class Trade {
    Date time;
    private float price;
    private float size;
    // Was buyer the market maker? (Bid came before ask)
    private boolean buyMaker; 
}
