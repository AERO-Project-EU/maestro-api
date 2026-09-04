package eu.orchestrator.backend.service.support.helper;

import eu.orchestrator.backend.service.component.InterfaceService;
import eu.orchestrator.repository.domain.Interface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.transaction.Transactional;

@Service
@Transactional(rollbackOn = Exception.class)
public class AutocompleteService {

    private static final Logger logger = Logger.getLogger(AutocompleteService.class.getName());

    @Autowired
    private InterfaceService interfaceService;


    public String fetchInterfaces(String q, String type) {
        List<Interface> interfaces;
        if (q != null && q != "") {
            interfaces = interfaceService.fetchFirst10ByNameIsLikeOrderByNameAsc(q);
        } else {
            interfaces = interfaceService.fetchFirst10ByOrderByNameAsc();
        }
        JSONArray interfacesBatch = new JSONArray();
        interfaces.forEach(t -> {
            try {
                JSONObject interfaceObj = new JSONObject();
                interfaceObj.put("id", t.getInterfaceID());
                String text = t.getName() + " (Type: " + t.getInterfaceType() + ", Transmission Protocol: ";
                if (null != t.getTransmissionProtocol() && !t.getTransmissionProtocol().isEmpty()) {
                    text += t.getTransmissionProtocol() + ")";
                } else {
                    text += "N/A)";
                }
                interfaceObj.put("text", text);
                interfacesBatch.put(interfaceObj);
            } catch (JSONException ex) {
                ex.printStackTrace();
                logger.log(Level.SEVERE, ex.getMessage(), ex);
            }
        });
        return interfacesBatch.toString();
    }
}
