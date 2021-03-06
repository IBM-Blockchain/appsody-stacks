package application.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.logging.Logger;

import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.Gateway;
import org.hyperledger.fabric.gateway.GatewayException;
import org.hyperledger.fabric.gateway.GatewayRuntimeException;
import org.hyperledger.fabric.gateway.Network;
import org.hyperledger.fabric.gateway.Wallet;
import org.hyperledger.fabric.gateway.Wallet.Identity;
import org.hyperledger.fabric_ca.sdk.exception.IdentityException;

public class ConnectionManager {
    public static final Logger LOGGER = Logger.getLogger(ConnectionManager.class.getName());
    private static HashMap<String, Gateway> gateways = new HashMap<String, Gateway>();

    private static Gateway getGateway(String fabricId) throws IdentityException, GatewayException {
        Gateway gateway = null;

        synchronized (gateways) {
            gateway = gateways.get(fabricId);
            Gateway.Builder builder = null;

            if (gateway == null) {
                LOGGER.info("Creating a new gateway...");

                String connectionProfileValue = ConnectionConfiguration.getConnectionProfile();
                boolean isJSONDocument = isJSONDocument(connectionProfileValue);
                InputStream connProfileIS;

                if (isJSONDocument){
                    //get connection profile straight from variable
                    connProfileIS = new ByteArrayInputStream(connectionProfileValue.getBytes());
                } else {
                    //get connection profile from file 
                    connProfileIS = ConnectionManager.class.getClassLoader().getResourceAsStream(connectionProfileValue);
                }

                Wallet wallet = new WalletManager().getWallet();

                //Verify identity is in wallet. 
                try {
                    Identity id = wallet.get(fabricId);
                    if(id == null){
                        throw new IdentityException("Fabric Identity : " + fabricId + " not found in wallet.");
                    }
                } catch (IOException e) {
                    LOGGER.severe("Error accessing wallet: "+e.toString());
                    throw new IdentityException("Fabric Identity : " + fabricId + " not found in wallet.", e);
                }
                
                try {
                    builder = Gateway.createBuilder().identity(wallet, fabricId).networkConfig(connProfileIS).discovery(true);
                } catch (IOException e) {
                    LOGGER.severe("Could not construct gateway...exception: "+e.toString());
                    throw new GatewayException("Error constructing gateway.", e);
                }
            
                gateway = builder.connect();
                gateways.put(fabricId, gateway);
                LOGGER.info("Gateway created and cached.");
            } 
        }
        return gateway;
    }

    public static Contract getContract(String fabricId) throws IdentityException, GatewayException {
        String channel = ConnectionConfiguration.getChannel();
        String chaincodeId = ConnectionConfiguration.getContractId();

        Gateway gateway = getGateway(fabricId);
        Network network = null;
        Contract contract = null;
        try {
            network = gateway.getNetwork(channel);
            contract = network.getContract(chaincodeId);
        } catch (GatewayRuntimeException e) {
            LOGGER.severe("Error retrieving contract: "+e.toString());
            throw new GatewayException("Error retrieving contract.", e);
        }
        return contract;
    }

    private static boolean isJSONDocument(String document) {
        return document.trim().startsWith("{") && document.trim().endsWith("}");
    }
}