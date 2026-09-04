package eu.orchestrator.backend.service.security;

import eu.orchestrator.common.exception.GenericBusinessException;
import eu.orchestrator.common.exception.NotAuthorizedException;
import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.backend.transfer.IDRuleSetTO;
import eu.orchestrator.repository.dao.IDRuleDAO;
import eu.orchestrator.repository.dao.IDRuleSetDAO;
import eu.orchestrator.repository.dao.IDRuleSetInstanceDAO;
import eu.orchestrator.repository.domain.ComponentNodeInstance;
import eu.orchestrator.repository.domain.IDRule;
import eu.orchestrator.repository.domain.IDRuleSet;
import eu.orchestrator.repository.domain.IDRuleSetInstance;
import eu.orchestrator.repository.domain.User;
import eu.orchestrator.backend.service.applicationinstance.ComponentNodeInstanceService;

import com.google.gson.Gson;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;

import static eu.orchestrator.repository.domain.QIDRuleSet.iDRuleSet;

@Service
@Transactional(rollbackOn = Exception.class)
public class IdRuleSetService {

    private static final Logger logger = Logger.getLogger(IdRuleSetService.class.getName());

    @Autowired
    private IDRuleDAO idRuleDAO;

    @Autowired
    private IDRuleSetDAO idRuleSetDAO;

    @Autowired
    private IDRuleSetInstanceDAO idRuleSetInstanceDAO;

    @Autowired
    private ComponentNodeInstanceService componentNodeInstanceService;

    @Autowired
    private EntityManager entityManager;


    public IDRuleSet fetchById(Long id, User authenticatedUser) {
        Optional<IDRuleSet> idRuleSetOP = idRuleSetDAO.findById(id);
        if (idRuleSetOP.isPresent() && idRuleSetOP.get().hasEditAllowance(authenticatedUser)) {
            List<IDRule> idRules = idRuleDAO.findAllByIdRuleSetOrderByDateCreatedDesc(idRuleSetOP.get());
            idRuleSetOP.get().setiDRules(idRules);
            return idRuleSetOP.orElse(null);
        }
        throw new NotAuthorizedException(GenericMessage.ID_RULE_SET_NOT_AUTHORIZED.getCode(), GenericMessage.ID_RULE_SET_NOT_AUTHORIZED);
    }

    public List<IDRuleSetInstance> fetchAllIDRuleSetInstanceByComponentNodeInstanceOrderByDateCreatedDesc(ComponentNodeInstance componentNodeInstance) {
        return idRuleSetInstanceDAO.findAllByComponentNodeInstanceOrderByDateCreatedDesc(componentNodeInstance);
    }

    public boolean checkIfExists(String name, User authenticatedUser) {
        if (idRuleSetDAO.findByNameAndOrganization(name, authenticatedUser.getOrganization()).isPresent()) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    public Page fetchIDRuleSets(Pageable pageable, String filters, IDRuleSet fIDRuleSet, User authenticatedUser) {
        BooleanExpression predicate = iDRuleSet.eq(iDRuleSet);
        boolean checkRequestParam = null != filters && !filters.isEmpty();
        boolean proceedWithRequestBody = true;
        if (checkRequestParam) {
            try {
                IDRuleSet filterIDRuleSet = new Gson().fromJson(new String(Base64.decodeBase64(filters)), IDRuleSet.class);
                if (null != filterIDRuleSet) {
                    proceedWithRequestBody = false;
                    if (null != filterIDRuleSet.getName() && !filterIDRuleSet.getName().isEmpty()) {
                        predicate = predicate.and(iDRuleSet.name.containsIgnoreCase(filterIDRuleSet.getName()));
                    }
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
        if (proceedWithRequestBody) {
            if (null != fIDRuleSet) {
                if (null != fIDRuleSet.getName() && !fIDRuleSet.getName().isEmpty()) {
                    predicate = predicate.and(iDRuleSet.name.containsIgnoreCase(fIDRuleSet.getName()));
                }
            }
        }
        if (!authenticatedUser.isAdmin()) {
            predicate = predicate.and(iDRuleSet.organization.eq(authenticatedUser.getOrganization())).or(iDRuleSet.publicIDRuleSet.eq(true));
        }

        Page<IDRuleSet> page;
        if (pageable.getPageSize() > 100) {
            page = idRuleSetDAO.findAll(predicate, PageRequest.of(pageable.getPageNumber(), 100, pageable.getSort()));
        } else {
            page = idRuleSetDAO.findAll(predicate, pageable);
        }

        List<IDRuleSetTO> idRuleSetTOs = new ArrayList<>();
        if (null != page && null != page.getContent() && !page.getContent().isEmpty()) {
            final User loginUser = authenticatedUser;
            page.getContent().forEach(idRuleSet -> {
                IDRuleSetTO idRuleSetTO = new IDRuleSetTO();
                idRuleSetTO.setId(idRuleSet.getId());
                idRuleSetTO.setDateCreated(idRuleSet.getDateCreated());
                idRuleSetTO.setLastModified(idRuleSet.getLastModified());
                idRuleSetTO.setName(idRuleSet.getName());
                idRuleSetTO.setRulesCounter(idRuleSet.getRuleIDs().size());
                idRuleSetTO.setPublicIDRuleSet(idRuleSet.getPublicIDRuleSet());
                idRuleSetTO.setAllowEdit(idRuleSet.hasEditAllowance(loginUser));
                idRuleSetTO.setAllowDelete(idRuleSet.hasDeleteAllowance(loginUser));
                idRuleSetTO.setOrganization(idRuleSet.getOrganization().getName());
                idRuleSetTOs.add(idRuleSetTO);
            });
        }
        return new PageImpl<>(idRuleSetTOs, pageable, page.getTotalElements());
    }

    public void create(IDRuleSet idRuleSet, User authenticatedUser) {
        // Check if ID Rule Set already exists
        if (authenticatedUser.isAdmin()) {
            if (idRuleSetDAO.findByName(idRuleSet.getName()).isPresent()) {
                throw new GenericBusinessException(GenericMessage.ID_RULE_SET_ALREADY_EXISTS.getCode(), GenericMessage.ID_RULE_SET_ALREADY_EXISTS);
            }
        } else {
            if (idRuleSetDAO.findByNameAndOrganization(idRuleSet.getName(), authenticatedUser.getOrganization()).isPresent()
                    || idRuleSetDAO.findByNameAndPublicIDRuleSet(idRuleSet.getName(), true).isPresent()) {
                throw new GenericBusinessException(GenericMessage.ID_RULE_SET_ALREADY_EXISTS.getCode(), GenericMessage.ID_RULE_SET_ALREADY_EXISTS);
            }
        }
        // Check if some other user try to add public IDs
        if (!authenticatedUser.isAdmin() && idRuleSet.getPublicIDRuleSet()) {
            throw new GenericBusinessException(GenericMessage.ID_RULE_SET_CANNOT_DEFINE_AS_PUBLIC.getCode(),
                    GenericMessage.ID_RULE_SET_CANNOT_DEFINE_AS_PUBLIC);
        }
        idRuleSet.setUser(authenticatedUser);
        idRuleSet.setOrganization(authenticatedUser.getOrganization());
        idRuleSet.setDateCreated(new Date());
        idRuleSet.setLastModified(new Date());
        List<IDRule> newIDRules = idRuleSet.getiDRules();
        idRuleSet.setiDRules(null);
        idRuleSetDAO.save(idRuleSet);

        newIDRules.forEach(idRule -> {
            if (null != idRule.getName() && !idRule.getName().isEmpty()) {
                IDRule idR = new IDRule();
                idR.setName(idRule.getName());
                idR.setDateCreated(new Date());
                idR.setLastModified(new Date());
                idR.setIdRuleSet(idRuleSet);
                idRuleDAO.save(idR);
                if (idR.getName().contains("sid")) {
                    String tempSID = idR.getName().substring(idR.getName().indexOf("sid:"));
                    tempSID = tempSID.substring(0, tempSID.indexOf(";"));
                    int sid = 1000000 + idR.getRuleID().intValue();
                    idR.setName(idR.getName().replace(tempSID, "sid:" + sid));
                    idRuleDAO.save(idR);
                }
            } else {
                logger.log(Level.SEVERE, "Missing Required Fields");
            }
        });
    }

    public void update(IDRuleSet idRuleSet, User authenticatedUser) {
        IDRuleSet existingIDRuleSet = fetchById(idRuleSet.getId(), authenticatedUser);
        if (existingIDRuleSet != null && existingIDRuleSet.hasEditAllowance(authenticatedUser)) {
            if (!existingIDRuleSet.getName().equals(idRuleSet.getName())) {
                // check if name exist
                if (authenticatedUser.isAdmin()) {
                    if (idRuleSetDAO.findByName(idRuleSet.getName()).isPresent()) {
                        throw new GenericBusinessException(GenericMessage.ID_RULE_SET_ALREADY_EXISTS.getCode(), GenericMessage.ID_RULE_SET_ALREADY_EXISTS);
                    }
                } else {
                    if (idRuleSetDAO.findByNameAndOrganization(idRuleSet.getName(), authenticatedUser.getOrganization()).isPresent()
                            || idRuleSetDAO.findByNameAndPublicIDRuleSet(idRuleSet.getName(), true).isPresent()) {
                        throw new GenericBusinessException(GenericMessage.ID_RULE_SET_ALREADY_EXISTS.getCode(), GenericMessage.ID_RULE_SET_ALREADY_EXISTS);
                    }
                }
            }
            //check if isPublic
            if (!existingIDRuleSet.getPublicIDRuleSet().equals(idRuleSet.getPublicIDRuleSet())) {
                // Check if some other user try to add public IDs
                if (authenticatedUser.isAdmin() && existingIDRuleSet.getOrganization().getName().equals("Admin_Organization")) {
                    existingIDRuleSet.setPublicIDRuleSet(idRuleSet.getPublicIDRuleSet());
                } else {
                    throw new GenericBusinessException(GenericMessage.ID_RULE_SET_CANNOT_DEFINE_AS_PUBLIC.getCode(),
                            GenericMessage.ID_RULE_SET_CANNOT_DEFINE_AS_PUBLIC);
                }
            }
            existingIDRuleSet.setName(idRuleSet.getName());
            existingIDRuleSet.setLastModified(new Date());
            idRuleSetDAO.save(existingIDRuleSet);
            List<Long> existingRuleIDs = existingIDRuleSet.getRuleIDs();
            List<IDRule> newIDRules = idRuleSet.getiDRules();
            if (null != newIDRules && !newIDRules.isEmpty()) {
                newIDRules.forEach(newIDRule -> {
                    if (null != newIDRule.getRuleID() && newIDRule.getRuleID() != 0) {
                        // Existing, needs to be updated
                        Optional<IDRule> exIDRuleOP = idRuleDAO.findById(newIDRule.getRuleID());
                        if (exIDRuleOP.isPresent()) {
                            IDRule exIDRule = exIDRuleOP.get();
                            if (newIDRule.getName().contains("sid")) {
                                String tempSID = newIDRule.getName().substring(newIDRule.getName().indexOf("sid:"));
                                tempSID = tempSID.substring(0, tempSID.indexOf(";"));
                                int sid = 1000000 + exIDRule.getRuleID().intValue();
                                exIDRule.setName(newIDRule.getName().replace(tempSID, "sid:" + sid));
                            } else {
                                exIDRule.setName(newIDRule.getName());
                            }
                            exIDRule.setLastModified(new Date());
                            idRuleDAO.save(exIDRule);
                            existingRuleIDs.remove(newIDRule.getRuleID());
                        }
                    } else {
                        newIDRule.setLastModified(new Date());
                        newIDRule.setDateCreated(new Date());
                        newIDRule.setIdRuleSet(existingIDRuleSet);
                        newIDRule.setName(newIDRule.getName());
                        idRuleDAO.save(newIDRule);
                        // New, needs to be added
                        if (newIDRule.getName().contains("sid")) {
                            String tempSID = newIDRule.getName().substring(newIDRule.getName().indexOf("sid:"));
                            tempSID = tempSID.substring(0, tempSID.indexOf(";"));
                            int sid = 1000000 + newIDRule.getRuleID().intValue();
                            newIDRule.setName(newIDRule.getName().replace(tempSID, "sid:" + sid));
                            idRuleDAO.save(newIDRule);
                        }
                    }
                });
            }
            if (!existingRuleIDs.isEmpty()) {
                existingRuleIDs.forEach(existingRuleID -> {
//              idRuleDAO.deleteById(existingRuleID);
//              idRuleDAO.delete(idRuleDAO.findById(existingRuleID).get());
                    Query q = entityManager.createNativeQuery("DELETE FROM id_rule WHERE id = ?");
                    q.setParameter(1, existingRuleID);
                    entityManager.joinTransaction();
                    q.executeUpdate();
                });
            }
        } else {
            throw new NotAuthorizedException(GenericMessage.ID_RULE_SET_NOT_AUTHORIZED.getCode(), GenericMessage.ID_RULE_SET_NOT_AUTHORIZED);
        }
    }

    public void delete(Long id, User authenticatedUser) {
        IDRuleSet existingIDRuleSet = fetchById(id, authenticatedUser);
        if (existingIDRuleSet != null && existingIDRuleSet.hasDeleteAllowance(authenticatedUser)) {
            List<ComponentNodeInstance> componentNodeInstances = componentNodeInstanceService.fetchAllComponentNodeInstances();
            if (null != componentNodeInstances && !componentNodeInstances.isEmpty()) {
                for (ComponentNodeInstance componentNodeInstance : componentNodeInstances) {
                    if (null != componentNodeInstance.getiDRuleSetInstances() && !componentNodeInstance.getiDRuleSetInstances().isEmpty()) {
                        if (!componentNodeInstance.getiDRuleSetInstances().stream().filter(tIDRuleSetInstance -> tIDRuleSetInstance.getIdRuleSet().getId()
                                .equals(existingIDRuleSet.getId())).collect(Collectors.toList()).isEmpty()) {
                            throw new GenericBusinessException(GenericMessage.ID_RULE_SET_USED_IN_COMPONENT_NODE_INSTANCE.getCode(),
                                    GenericMessage.ID_RULE_SET_USED_IN_COMPONENT_NODE_INSTANCE);
                        }
                    }
                }
            }
            idRuleSetDAO.delete(existingIDRuleSet);
        } else {
            throw new NotAuthorizedException(GenericMessage.ID_RULE_SET_NOT_AUTHORIZED.getCode(), GenericMessage.ID_RULE_SET_NOT_AUTHORIZED);
        }
    }

    public void deleteAllIDRuleSetInstances(List<IDRuleSetInstance> idRuleSetInstances) {
        idRuleSetInstanceDAO.deleteAll(idRuleSetInstances);
    }
}
