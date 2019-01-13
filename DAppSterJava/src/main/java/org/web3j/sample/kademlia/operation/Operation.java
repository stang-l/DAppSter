package org.web3j.sample.kademlia.operation;

import java.io.IOException;
import org.web3j.sample.kademlia.exceptions.RoutingException;

/**
 * An operation in the Kademlia routing protocol
 *
 * @author Joshua Kissoon
 * @created 20140218
 */
public interface Operation
{

    /**
     * Starts an operation and returns when the operation is finished
     *
     * @throws kademlia.exceptions.RoutingException
     */
    public void execute() throws IOException, RoutingException;
}
