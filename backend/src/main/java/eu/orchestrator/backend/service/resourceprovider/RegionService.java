package eu.orchestrator.backend.service.resourceprovider;

import eu.orchestrator.common.exception.GenericBusinessException;
import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.backend.transfer.RegionTO;
import eu.orchestrator.backend.transfer.TOConverter;
import eu.orchestrator.repository.dao.RegionDAO;
import eu.orchestrator.repository.domain.Provider;
import eu.orchestrator.repository.domain.Region;

import com.google.gson.Gson;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.transaction.Transactional;

import static eu.orchestrator.repository.domain.QRegion.region;

@Service
@Transactional(rollbackOn = Exception.class)
public class RegionService {

    private static final Logger logger = Logger.getLogger(RegionService.class.getName());

    @Autowired
    private RegionDAO regionDAO;

    @Value("${oss.tac.url}")
    private String ossTacURL;


    public Region fetchById(Long id) {
        Optional<Region> regionOP = regionDAO.findById(id);
        return regionOP.orElse(null);
    }

    public List<Region> fetchAllByProviderOrderByNameAsc(Provider provider) {
        return regionDAO.findAllByProviderOrderByNameAsc(provider);
    }

    public void saveRegion(Region region) {
        regionDAO.save(region);
    }

    public void deleteRegion(Region region) {
        regionDAO.delete(region);
    }

    public Page fetchRegions(Pageable pageable, String filters, Region fRegion) {
        BooleanExpression predicate = region.eq(region);
        boolean checkRequestParam = null != filters && !filters.isEmpty();
        boolean proceedWithRequestBody = true;
        if (checkRequestParam) {
            try {
                Region filterRegion = new Gson().fromJson(new String(Base64.decodeBase64(filters)), Region.class);
                if (null != filterRegion) {
                    proceedWithRequestBody = false;
                    if (null != filterRegion.getName() && !filterRegion.getName().isEmpty()) {
                        predicate = predicate.and(region.name.containsIgnoreCase(filterRegion.getName()));
                    }
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
        if (proceedWithRequestBody) {
            if (null != fRegion) {
                if (null != fRegion.getName() && !fRegion.getName().isEmpty()) {
                    predicate = predicate.and(region.name.containsIgnoreCase(fRegion.getName()));
                }
            }
        }
        Page<Region> page;
        if (pageable.getPageSize() > 100) {
            page = regionDAO.findAll(predicate, PageRequest.of(pageable.getPageNumber(), 100, pageable.getSort()));
        } else {
            page = regionDAO.findAll(predicate, pageable);
        }
        return new TOConverter(Region.class.getName(), page, pageable).convertToTO();
    }

    // TODO move to oss
    public List<RegionTO> fetchOSSRegions() {
        RestTemplate restTemplate = new RestTemplate();
        List<RegionTO> regionTOList = new ArrayList<>();
/*
    RegionTO regionTO = new RegionTO();
    regionTO.setDateCreated(new Date());
    regionTO.setLastModified(new Date());
    regionTO.setName("gr-athens");
    regionTO.setRegionID(new Long(1));
    regionTOList.add(regionTO);


    RegionTO regionTO2 = new RegionTO();
    regionTO2.setDateCreated(new Date());
    regionTO2.setLastModified(new Date());
    regionTO2.setName("genoa");
    regionTO2.setRegionID(new Long(2));
    regionTOList.add(regionTO2);
*/
        if (ossTacURL.isEmpty()) {
            RegionTO regionTO2 = new RegionTO();
            regionTO2.setDateCreated(new Date());
            regionTO2.setLastModified(new Date());
            regionTO2.setName("no region found");
            regionTO2.setRegionID(new Long(-10));
            regionTOList.add(regionTO2);
            return regionTOList;
        } else {
            try {
                ResponseEntity<String> responseEntity = restTemplate.exchange(ossTacURL, HttpMethod.GET, null, String.class);
                if (null != responseEntity && responseEntity.getStatusCode() == HttpStatus.OK) {
                    String json = responseEntity.getBody();
                    JSONArray returnArrayObject = new JSONArray(json);
                    returnArrayObject.forEach(jObject -> {
                        JSONObject jsonObject = (JSONObject) jObject;
                        RegionTO regionTO = new RegionTO();
                        regionTO.setDateCreated(new Date());
                        regionTO.setLastModified(new Date());
                        regionTO.setName(jsonObject.get("name").toString());
                        regionTO.setRegionID(Long.valueOf(jsonObject.get("tac").toString()));
                        regionTOList.add(regionTO);
                    });
                }
                return regionTOList;

            } catch (JSONException e) {
                logger.log(Level.SEVERE, "Something happend with the json parsing: " + e.getMessage());
                throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
            } catch (RestClientException e) {
                logger.log(Level.SEVERE, "Rest client exception at fetchOSSRegions method: " + e.getMessage());
                RegionTO regionTO2 = new RegionTO();
                regionTO2.setDateCreated(new Date());
                regionTO2.setLastModified(new Date());
                regionTO2.setName("no region found");
                regionTO2.setRegionID(new Long(-10));
                regionTOList.add(regionTO2);

                return regionTOList;
            } catch (Exception e){
                logger.log(Level.SEVERE, "Rest client exception at fetchOSSRegions method: " + e.getMessage());
                RegionTO regionTO2 = new RegionTO();
                regionTO2.setDateCreated(new Date());
                regionTO2.setLastModified(new Date());
                regionTO2.setName("no region found");
                regionTO2.setRegionID(new Long(-10));
                regionTOList.add(regionTO2);

                return regionTOList;
            }

        }
    }
}
