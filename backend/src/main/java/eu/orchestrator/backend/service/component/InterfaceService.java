package eu.orchestrator.backend.service.component;

import eu.orchestrator.backend.transfer.InterfaceTO;
import eu.orchestrator.repository.dao.InterfaceDAO;
import eu.orchestrator.repository.domain.Interface;
import eu.orchestrator.repository.domain.User;

import com.google.gson.Gson;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.transaction.Transactional;

import static eu.orchestrator.repository.domain.QInterface.interface$;

@Service
@Transactional(rollbackOn = Exception.class)
public class InterfaceService {

    private static final Logger logger = Logger.getLogger(InterfaceService.class.getName());

    @Autowired
    InterfaceDAO interfaceDAO;

    @Autowired
    ComponentNodeService componentNodeService;


    public Interface fetchInterfaceById(Long id) {
        Optional<Interface> interfaceOptional = interfaceDAO.findById(id);
        return interfaceOptional.orElse(null);
    }

    public Interface fetchInterfaceByName(String name) {
        Optional<Interface> interfaceOptional = interfaceDAO.findByName(name);
        return interfaceOptional.orElse(null);
    }

    public PageImpl fetchInterfaces(Pageable pageable, String filters, Interface fInterface, User authenticatedUser) {
        BooleanExpression predicate = interface$.eq(interface$);
        boolean checkRequestParam = null != filters && !filters.isEmpty();
        boolean proceedWithRequestBody = true;
        if (checkRequestParam) {
            try {
                Interface filterInterface = new Gson().fromJson(new String(Base64.decodeBase64(filters)), Interface.class);
                if (null != filterInterface) {
                    proceedWithRequestBody = false;
                    if (null != filterInterface.getName() && !filterInterface.getName().isEmpty()) {
                        predicate = predicate.and(interface$.name.containsIgnoreCase(filterInterface.getName()));
                    }
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
        if (proceedWithRequestBody) {
            if (null != fInterface) {
                if (null != fInterface.getName() && !fInterface.getName().isEmpty()) {
                    predicate = predicate.and(interface$.name.containsIgnoreCase(fInterface.getName()));
                }
            }
        }
        if (!authenticatedUser.isAdmin()) {
            if (null != authenticatedUser.getOrganization()) {
                predicate = predicate.and(interface$.component.in(componentNodeService.fetchAllByOrganizationOrPublicComponent(authenticatedUser)));
            } else {
                predicate = predicate.and(interface$.component.in(componentNodeService.fetchAllByUserOrPublicComponentOrderByNameAsc(authenticatedUser)));
            }
        }
        Page<Interface> page = interfaceDAO.findAll(predicate, PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort()));
        List<InterfaceTO> interfaceTOs = new ArrayList<>();
        if (null != page && null != page.getContent() && !page.getContent().isEmpty()) {
            page.forEach(p -> {
                InterfaceTO interfaceTO = new InterfaceTO();
                interfaceTO.setComponentID(p.getComponent().getId());
                BeanUtils.copyProperties(p, interfaceTO);
                interfaceTOs.add(interfaceTO);
            });
        }
        return new PageImpl<>(interfaceTOs, pageable, page.getTotalElements());
    }

    public List<Interface> fetchFirst10ByNameIsLikeOrderByNameAsc(String q) {
        return interfaceDAO.findFirst10ByNameIsLikeOrderByNameAsc(q);
    }

    public List<Interface> fetchFirst10ByOrderByNameAsc() {
        return interfaceDAO.findFirst10ByOrderByNameAsc();
    }
}
