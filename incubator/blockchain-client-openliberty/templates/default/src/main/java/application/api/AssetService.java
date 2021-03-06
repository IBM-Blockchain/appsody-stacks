package application.api;

import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.ExternalDocumentation;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.GatewayException;
import org.hyperledger.fabric_ca.sdk.exception.IdentityException;

import application.api.exceptions.AssetException;
import application.api.exceptions.AssetNotFoundException;
import application.utils.ConnectionManager;

@Path("/myassets")
@OpenAPIDefinition(info = @Info(title = "Blockchain MyAsset Web Service", version = "0.1", description = "Restful Web Service for blockchain transactions.", contact = @Contact(url = "https://www.ibm.com/blockchain")), externalDocs = @ExternalDocumentation(description = "https://www.ibm.com/blockchain", url = "https://www.ibm.com/blockchain"))
public class AssetService {

    @Context
    private HttpHeaders headers = null;

    private static final Logger LOGGER = Logger.getLogger(AssetService.class.getName());

    @GET
    @Path("/{id}")
    @Produces("application/json")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "MyAsset", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MyAsset.class))),
        @APIResponse(responseCode = "400", description = "Error reading MyAsset", content = @Content(mediaType = "application/json")),    
        @APIResponse(responseCode = "404", description = "MyAsset not found", content = @Content(mediaType = "application/json")) }) 
    @Operation(summary = "Retrieve MyAsset from the blockchain", description = "Retrieves the MyAsset from the blockchain.")
    @Tag(name = "MyAssets")
    public Response getMyAsset(@PathParam("id") String id) throws AssetNotFoundException, AssetException, IdentityException, GatewayException{
        LOGGER.info("AssetId : "+ id);
        String fabricId = headers.getHeaderString("X-FABRIC-IDENTITY");
        Contract contract = ConnectionManager.getContract(fabricId);
        MyAssetController controller = new MyAssetController(); 
        byte[] result = controller.getMyAsset(contract, id);
        String responseString = new String(result, StandardCharsets.UTF_8);
        LOGGER.info("Transaction response : " + responseString);
        return Response.ok().entity(responseString).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes("application/json")
    @Produces("application/json")
    @APIResponses(value = {
        @APIResponse(responseCode = "204", description = "MyAsset updated"),
        @APIResponse(responseCode = "400", description = "Error reading MyAsset", content = @Content(mediaType = "application/json")),    
        @APIResponse(responseCode = "404", description = "MyAsset not found", content = @Content(mediaType = "application/json")) }) 
    @Operation(summary = "Update MyAsset on the blockchain", description = "Updates an MyAsset on the blockchain.")
    @Tag(name = "MyAssets")
    public Response updateMyAsset(@PathParam("id") String id, MyAsset asset) throws IdentityException, GatewayException, AssetException{
        LOGGER.info("Asset : " + asset.toString());
        // The path value is required.  
        if(asset.getMyAssetId() == null || asset.getMyAssetId().isEmpty()){
            asset.setMyAssetId(id);
        } 
        // If the data is inconsistent fail. 
        else if (!asset.getMyAssetId().equals(id)){
            LOGGER.severe("Data is incosistent - ID : "+ id + " Asset : " + asset);
            throw new AssetException("Not able to update asset, conflicting id values passed.");
        }
        String fabricId = headers.getHeaderString("X-FABRIC-IDENTITY");
        Contract contract = ConnectionManager.getContract(fabricId);
        MyAssetController controller = new MyAssetController(); 
        controller.updateMyAsset(contract, asset);
        return Response.noContent().build();
    }

    @DELETE
    @Path("/{id}")
    @Produces("application/json")
    @APIResponses(value = {
        @APIResponse(responseCode = "204", description = "MyAsset Deleted"),
        @APIResponse(responseCode = "400", description = "Error reading MyAsset", content = @Content(mediaType = "application/json")),    
        @APIResponse(responseCode = "404", description = "MyAsset not found", content = @Content(mediaType = "application/json")) }) 
    @Operation(summary = "Deletes MyAsset from the blockchain", description = "Deletes MyAsset from the blockchain.")
    @Tag(name = "MyAssets")
    public Response deleteMyAsset(@PathParam("id") String id) throws IdentityException, GatewayException {
        LOGGER.info("AssetId  : " + id);
        String fabricId = headers.getHeaderString("X-FABRIC-IDENTITY");
        Contract contract = ConnectionManager.getContract(fabricId);
        MyAssetController controller = new MyAssetController(); 
        controller.deleteMyAsset(contract, id);
        return Response.noContent().build();
    }    

    @POST
    @Consumes("application/json")
    @Produces("application/json")
    @APIResponses(value = {
        @APIResponse(responseCode = "204", description = "MyAsset Created"),
        @APIResponse(responseCode = "400", description = "Error reading MyAsset", content = @Content(mediaType = "application/json")),    
        @APIResponse(responseCode = "404", description = "MyAsset not found", content = @Content(mediaType = "application/json")) }) 
    @Operation(summary = "Create MyAsset on the blockchain", description = "Create MyAsset on the blockchain.")
    @Tag(name = "MyAssets")
    public Response createMyAsset(MyAsset asset) throws IdentityException, GatewayException {
        LOGGER.info("Asset : " + asset.toString());
        String fabricId = headers.getHeaderString("X-FABRIC-IDENTITY");
        Contract contract = ConnectionManager.getContract(fabricId);
        MyAssetController controller = new MyAssetController(); 
        controller.createMyAsset(contract, asset);
        return Response.noContent().build();
    }
}
