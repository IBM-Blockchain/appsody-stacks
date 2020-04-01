package org.example;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import org.assertj.core.api.Assert;
import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.ContractException;
import org.hyperledger.fabric.gateway.Gateway;
import org.hyperledger.fabric.gateway.Network;
import org.hyperledger.fabric.gateway.Wallet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import dev.appsody.starter.BlockchainClient;


public class NixaTest {
    private Gateway mockGateway;
    private Network mockNetwork;
    private Contract mockContract;

    @BeforeEach
    public void setup() throws Exception {
        mockGateway = Mockito.mock(Gateway.class);
        mockNetwork = Mockito.mock(Network.class);
        mockContract = Mockito.mock(Contract.class);
    }

    @Test 
    public void testNewInsance() throws ContractException, TimeoutException, InterruptedException {

        Mockito.when(mockGateway.getNetwork("testchannel")).thenReturn(mockNetwork);
        Network network = mockGateway.getNetwork("testchannel");
        Assertions.assertNotNull(network);

        Mockito.when(network.getContract("testcontract")).thenReturn(mockContract);
        Contract contract = network.getContract("testcontract");
        Assertions.assertNotNull(contract);
        Mockito.verify(network).getContract("testcontract");

        Mockito.when(contract.submitTransaction("createMyAsset", "testasset", "testvalue")).thenReturn(new byte[0]);
        byte[] results = contract.submitTransaction("createMyAsset", "testasset", "testvalue");
        System.out.println("results: " + results);
        Assertions.assertNotNull(results);
        Assertions.assertArrayEquals(new byte[0], results);
        Mockito.verify(contract).submitTransaction("createMyAsset", "testasset", "testvalue");

    }
}




