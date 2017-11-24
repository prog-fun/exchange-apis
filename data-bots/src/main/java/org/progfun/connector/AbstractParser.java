package org.progfun.connector;

import org.progfun.Market;

/**
 * Abstract API command parser
 */
public abstract class AbstractParser implements Parser {

    protected Market market;

    @Override
    public void setMarket(Market market) {
        this.market = market;
    }

    /**
     * Prints out errors from the API
     *
     * @param excptn The incoming exception
     */
    @Override
    public void onError(Exception excptn) {
        System.out.println(excptn.toString());
    }
}
