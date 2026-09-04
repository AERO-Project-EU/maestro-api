package eu.orchestrator.backend.rest.support.external;

import eu.orchestrator.common.exception.NotFoundException;
import eu.orchestrator.backend.service.oss.slice.SliceResponseStatus;
import eu.orchestrator.transfer.entities.oss.Slice;
import eu.orchestrator.transfer.entities.physiognomica.PhysiognomicaResultModel;
import eu.orchestrator.backend.service.support.external.CallbacksService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/v1/callback")
public class CallbacksController {

    private static final Logger logger = Logger.getLogger(CallbacksController.class.getName());

    @Autowired
    private CallbacksService callbacksService;

    @PostMapping(value = "/slice/{applicationInstanceID}/{status}")
    public ResponseEntity<Serializable> sliceFromOss(@PathVariable(value = "applicationInstanceID") Long applicationInstanceId,
            @PathVariable(value = "status") String status, @RequestBody(required = false) Slice slice) {
        try {
            final SliceResponseStatus sliceResponseStatus = SliceResponseStatus.fromString(status);
            callbacksService.sliceFromOss(applicationInstanceId, slice, sliceResponseStatus);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (NotFoundException exception) {
            logger.log(Level.SEVERE, exception.getMessage(), exception);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException exception) {
            logger.log(Level.SEVERE, exception.getMessage(), exception);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            logger.log(Level.SEVERE, String.format("Failure while handling slice for application instance [id='%s'] from OSS", applicationInstanceId),
                    exception);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value = "/undeployment/{applicationInstanceID}/{status}")
    public ResponseEntity<Serializable> undeploymentStatusFromOrchestrator(@PathVariable(value = "applicationInstanceID") Long applicationInstanceId,
            @PathVariable(value = "status") String status) {
        try {
            callbacksService.undeploymentStatusFromOrchestrator(applicationInstanceId, status);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping(value = "/policy/{policyID}/{status}")
    public ResponseEntity<Serializable> policyStatusFromPolicyEngine(@PathVariable(value = "policyID") Long policyId,
            @PathVariable(value = "status") String status) {
        try {
            callbacksService.policyStatusFromPolicyEngine(policyId, status);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping(value = "/policy/soc/manager/{policyHexID}/{status}")
    public ResponseEntity<Serializable> socPolicyStatusFromManager(@PathVariable(value = "policyHexID") String policyHexId,
            @PathVariable(value = "status") String status) {
        try {
            callbacksService.socPolicyStatusFromManager(policyHexId, status);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping(value = "/policy/soc/drools/{policyHexID}/{status}")
    public ResponseEntity<Serializable> socPolicyStatusFromDrools(@PathVariable(value = "policyHexID") String policyHexId,
            @PathVariable(value = "status") String status) {
        try {
            callbacksService.socPolicyStatusFromDrools(policyHexId, status);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping(value = "/profile/{profileHexID}")
    public ResponseEntity<String> profilerStatusFromPhysiognomica(@PathVariable(value = "profileHexID") String profileHexId,
            @RequestBody PhysiognomicaResultModel physiognomicaResultModel) {
        try {
            return new ResponseEntity<>(callbacksService.profilerStatusFromPhysiognomica(profileHexId, physiognomicaResultModel), HttpStatus.OK);
        } catch (NotFoundException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
        return new ResponseEntity<>(profileHexId, HttpStatus.EXPECTATION_FAILED);
    }
}
