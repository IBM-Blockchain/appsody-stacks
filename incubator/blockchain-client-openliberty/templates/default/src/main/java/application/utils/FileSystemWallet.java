package application.utils;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import org.hyperledger.fabric.gateway.Wallet;
import org.hyperledger.fabric_ca.sdk.exception.IdentityException;
import org.json.JSONException;
import org.json.JSONObject;

public class FileSystemWallet {
    public final static Logger LOGGER = Logger.getLogger(FileSystemWallet.class.getName());

    public Wallet getWallet() throws IdentityException {
        Wallet wallet = null;
        String walletPath = null;
        JSONObject walletProfile = null;
        String walletProfileString = ConnectionConfiguration.getWalletProfile();
        if (walletProfileString == null || walletProfileString.isEmpty()) {
            LOGGER.severe("Wallet profile was not specified... cannot retrieve wallet.");
            throw new IdentityException("Wallet profile not found.");
        }

        try {
            walletProfile = new JSONObject(walletProfileString);
        } catch (JSONException e) {
            LOGGER.severe("Could not parse the wallet profile: " + e.toString());
            throw new IdentityException("Error parsing wallet profile.", e);
        }

        try {
            JSONObject options = walletProfile.getJSONObject("options");
            walletPath = options.getString("path");
            if (walletPath == null || walletPath.isEmpty()) {
                LOGGER.severe("No path to the file system wallet was provided. Cannot retrieve the wallet.");
                throw new IdentityException("Invalid wallet path provided.");
            }
        } catch (JSONException e) {
            LOGGER.severe("Could not parse the wallet options and retrieve the wallet.");
            throw new IdentityException("Exception parsing wallet options.");
        }

        try {
            LOGGER.info("Wallet path attribute: " + walletPath);
            URI walletURI = FileSystemWallet.class.getClassLoader().getResource(walletPath).toURI();
            LOGGER.info("Wallet Path URI: " + walletURI);
            Path basePath = Paths.get(walletURI); 
            wallet = Wallet.createFileSystemWallet(basePath);
            if (wallet == null) {
                LOGGER.severe("Could not retrieve the wallet from the path specified: " + walletPath);
                throw new IdentityException("Invalid wallet provided.");
            }
        } catch (Exception e) {
            LOGGER.severe("Exception processing wallet path: " + e.toString());
            throw new IdentityException("Exception processing wallet path.", e);
        }
        return wallet;
    }
}