package eu.orchestrator.backend.service.sshkey;

import eu.orchestrator.common.exception.GenericBusinessException;
import eu.orchestrator.common.exception.NotAuthorizedException;
import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.backend.transfer.TOConverter;
import eu.orchestrator.repository.dao.SSHKeyDAO;
import eu.orchestrator.repository.domain.ComponentNodeInstance;
import eu.orchestrator.repository.domain.SSHKey;
import eu.orchestrator.repository.domain.User;
import eu.orchestrator.backend.service.applicationinstance.ComponentNodeInstanceService;

import com.google.gson.Gson;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.transaction.Transactional;

import static eu.orchestrator.repository.domain.QSSHKey.sSHKey;

@Service
@Transactional(rollbackOn = Exception.class)
public class SSHKeyService {

    private static final Logger logger = Logger.getLogger(SSHKeyService.class.getName());

    @Autowired
    private SSHKeyDAO sshKeyDAO;

    @Autowired
    private ComponentNodeInstanceService componentNodeInstanceService;


    public SSHKey fetchSshKeyById(Long id) {
        Optional<SSHKey> sshKeyOptional = sshKeyDAO.findById(id);
        return sshKeyOptional.orElse(null);
    }

    public Page fetchSshKeys(Pageable pageable, String filters, SSHKey sshKey, User authenticatedUser) {
        BooleanExpression predicate = sSHKey.eq(sSHKey);
        boolean checkRequestParam = null != filters && !filters.isEmpty();
        boolean proceedWithRequestBody = true;
        if (checkRequestParam) {
            try {
                SSHKey filterSSHKey = new Gson().fromJson(new String(Base64.decodeBase64(filters)), SSHKey.class);
                if (null != filterSSHKey) {
                    proceedWithRequestBody = false;
                    if (null != filterSSHKey.getFriendlyName() && !filterSSHKey.getFriendlyName().isEmpty()) {
                        predicate = predicate.and(sSHKey.friendlyName.containsIgnoreCase(filterSSHKey.getFriendlyName())
                                .or(sSHKey.sshKey.containsIgnoreCase(filterSSHKey.getFriendlyName())));
                    }
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
        if (proceedWithRequestBody) {
            if (null != sshKey) {
                if (null != sshKey.getFriendlyName() && !sshKey.getFriendlyName().isEmpty()) {
                    predicate = predicate.and(sSHKey.friendlyName.containsIgnoreCase(sshKey.getFriendlyName())
                            .or(sSHKey.sshKey.containsIgnoreCase(sshKey.getFriendlyName())));
                }
            }
        }
        //same for all user role
        predicate = predicate.and(sSHKey.user.eq(authenticatedUser));
        Page<SSHKey> page;
        if (pageable.getPageSize() > 100) {
            page = sshKeyDAO
                    .findAll(predicate, PageRequest.of(pageable.getPageNumber(), 100, pageable.getSort()));
        } else {
            page = sshKeyDAO.findAll(predicate, pageable);
        }
        return new TOConverter(SSHKey.class.getName(), page, pageable, authenticatedUser).convertToTOWithPermissions();
    }

    public SSHKey fetchById(Long id, User authenticatedUser) {
        SSHKey sshKey = fetchSshKeyById(id);
        if (sshKey == null) {
            throw new GenericBusinessException(GenericMessage.SSH_KEY_NOT_EXIST.getCode(), GenericMessage.SSH_KEY_NOT_EXIST);
        }
        if (sshKey.getUser().getId().equals(authenticatedUser.getId())) {
            return sshKey;
        } else {
            throw new NotAuthorizedException(GenericMessage.SSH_KEY_NOT_AUTHORIZED.getCode(), GenericMessage.SSH_KEY_NOT_AUTHORIZED);
        }
    }

    public void create(SSHKey sshKey, User authenticatedUser) {
        if (sshKeyDAO.findByFriendlyNameAndUser(sshKey.getFriendlyName(), authenticatedUser).isPresent()) {
            throw new GenericBusinessException(GenericMessage.SSH_KEY_ALREADY_EXISTS.getCode(), GenericMessage.SSH_KEY_ALREADY_EXISTS);
        }
        if (sshKey.getDefaultSSH().booleanValue()) {
            // Checks if other ssh key is default
            Optional<SSHKey> existingDefaultSSHKeyOP = sshKeyDAO.findByUserAndDefaultSSH(authenticatedUser, true);
            if (existingDefaultSSHKeyOP.isPresent()) {
                SSHKey existingDefaultSSHKey = existingDefaultSSHKeyOP.get();
                existingDefaultSSHKey.setLastModified(new Date());
                existingDefaultSSHKey.setDefaultSSH(false);
                sshKeyDAO.save(existingDefaultSSHKey);
            }
        }
        sshKey.setUser(authenticatedUser);
        sshKey.setDateCreated(new Date());
        sshKey.setLastModified(new Date());
        sshKeyDAO.save(sshKey);
    }

    public void update(SSHKey sshKey, User authenticatedUser) {
        SSHKey existingSSHKey = fetchSshKeyById(sshKey.getId());
        if (existingSSHKey == null) {
            throw new GenericBusinessException(GenericMessage.SSH_KEY_NOT_EXIST.getCode(), GenericMessage.SSH_KEY_NOT_EXIST);
        }
        if (existingSSHKey.getUser().getId().equals(authenticatedUser.getId())) {
            if (!existingSSHKey.getFriendlyName().equals(sshKey.getFriendlyName())) {
                if (sshKeyDAO.findByFriendlyNameAndUser(sshKey.getFriendlyName(), authenticatedUser).isPresent()) {
                    throw new GenericBusinessException(GenericMessage.SSH_KEY_ALREADY_EXISTS.getCode(), GenericMessage.SSH_KEY_ALREADY_EXISTS);
                }
            }
            if (!existingSSHKey.getDefaultSSH().booleanValue() && sshKey.getDefaultSSH().booleanValue()) {
                // Checks if other ssh key is default
                Optional<SSHKey> existingDefaultSSHKeyOP = sshKeyDAO.findByUserAndDefaultSSH(existingSSHKey.getUser(), true);
                if (existingDefaultSSHKeyOP.isPresent()) {
                    SSHKey existingDefaultSSHKey = existingDefaultSSHKeyOP.get();
                    existingDefaultSSHKey.setLastModified(new Date());
                    existingDefaultSSHKey.setDefaultSSH(false);
                    sshKeyDAO.save(existingDefaultSSHKey);
                }
            }
            existingSSHKey.setFriendlyName(sshKey.getFriendlyName());
            existingSSHKey.setSshKey(sshKey.getSshKey());
            existingSSHKey.setDefaultSSH(sshKey.getDefaultSSH().booleanValue());
            existingSSHKey.setLastModified(new Date());
            sshKeyDAO.save(existingSSHKey);
        } else {
            throw new GenericBusinessException(GenericMessage.SSH_KEY_NOT_AUTHORIZED.getCode(), GenericMessage.SSH_KEY_NOT_AUTHORIZED);
        }
    }

    public void changeDefaultStatus(Long id, User authenticatedUser) {
        SSHKey existingSSHKey = fetchSshKeyById(id);
        if (existingSSHKey != null && existingSSHKey.getUser().getId().equals(authenticatedUser.getId())) {
            if (!existingSSHKey.getDefaultSSH().booleanValue()) {
                // Check if it is another one default
                Optional<SSHKey> existingDefaultSSHKeyOP = sshKeyDAO.findByUserAndDefaultSSH(existingSSHKey.getUser(), true);
                if (existingDefaultSSHKeyOP.isPresent()) {
                    SSHKey existingDefaultSSHKey = existingDefaultSSHKeyOP.get();
                    existingDefaultSSHKey.setLastModified(new Date());
                    existingDefaultSSHKey.setDefaultSSH(false);
                    sshKeyDAO.save(existingDefaultSSHKey);
                }
                existingSSHKey.setDefaultSSH(true);
            } else {
                existingSSHKey.setDefaultSSH(false);
            }
            existingSSHKey.setLastModified(new Date());
            sshKeyDAO.save(existingSSHKey);
        } else {
            throw new GenericBusinessException(GenericMessage.SSH_KEY_NOT_AUTHORIZED.getCode(), GenericMessage.SSH_KEY_NOT_AUTHORIZED);
        }
    }

    public void delete(Long id, User authenticatedUser) {
        SSHKey existingSSHKey = fetchSshKeyById(id);
        if (existingSSHKey == null) {
            throw new GenericBusinessException(GenericMessage.SSH_KEY_NOT_EXIST.getCode(), GenericMessage.SSH_KEY_NOT_EXIST);
        }
        if (existingSSHKey.getUser().getId().equals(authenticatedUser.getId())) {
            // Check if ssh key is the default one
            if (existingSSHKey.getDefaultSSH().booleanValue()) {
                throw new GenericBusinessException(GenericMessage.SSH_KEY_DEFAULT.getCode(), GenericMessage.SSH_KEY_DEFAULT);
            }
            // Check if ssh key is used
            List<ComponentNodeInstance> componentNodeInstances = componentNodeInstanceService.fetchAllComponentNodeInstancesBySshKey(existingSSHKey);
            if (null != componentNodeInstances && !componentNodeInstances.isEmpty()) {
                throw new GenericBusinessException(GenericMessage.SSH_KEY_USED.getCode(), GenericMessage.SSH_KEY_USED);
            }
            sshKeyDAO.delete(existingSSHKey);
        } else {
            throw new GenericBusinessException(GenericMessage.SSH_KEY_NOT_AUTHORIZED.getCode(), GenericMessage.SSH_KEY_NOT_AUTHORIZED);
        }
    }

}
