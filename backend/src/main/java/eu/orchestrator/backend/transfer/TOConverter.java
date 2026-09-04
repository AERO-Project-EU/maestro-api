package eu.orchestrator.backend.transfer;

import eu.orchestrator.repository.domain.Organization;
import eu.orchestrator.repository.domain.User;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TOConverter<O, TO> implements Serializable {

    private static final Logger logger = Logger.getLogger(TOConverter.class.getName());

    private static final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final Page<O> page;
    private final Pageable pageable;
    private final String classFromName;
    private User authenticatedUser;

    public TOConverter(String classFromName, Page<O> page, Pageable pageable) {
        this.page = page;
        this.pageable = pageable;
        this.classFromName = classFromName;
    }

    public TOConverter(String classFromName, Page<O> page, Pageable pageable,
            User authenticatedUser) {
        this.page = page;
        this.pageable = pageable;
        this.classFromName = classFromName;
        this.authenticatedUser = authenticatedUser;
    }

    public Page<TO> convertToTO() {

        try {

            if (null != page) {

                List<TO> listTOs = new ArrayList<>();

                Class toClass = Class.forName("eu.orchestrator.backend.transfer." + classFromName
                        .substring(classFromName.lastIndexOf(".") + 1) + "TO");

                if (null != page.getContent() && !page.getContent().isEmpty()) {

                    page.getContent().forEach(obj -> {

                        try {

                            Object targetObj = toClass.newInstance();

                            BeanUtils.copyProperties(obj, targetObj);

                            listTOs.add((TO) targetObj);

                        } catch (Exception e) {
                            logger.log(Level.SEVERE, e.getMessage(), e);
                        }

                    });

                }
                return new PageImpl<>(listTOs, pageable, page.getTotalElements());
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }

        return new PageImpl<>(new ArrayList<>(), pageable, page.getTotalElements());
    }

    public Page<TO> convertToTOWithPermissions() {

        try {

            if (null != page) {

                List<TO> listTOs = new ArrayList<>();

                Class toClass = Class.forName("eu.orchestrator.backend.transfer." + classFromName
                        .substring(classFromName.lastIndexOf(".") + 1) + "TO");

                Class fromClass = Class.forName(classFromName);

                Class userClass = Class.forName("eu.orchestrator.repository.domain.User");

                if (null != page.getContent() && !page.getContent().isEmpty()) {

                    page.getContent().forEach(obj -> {
                        try {

                            Object targetObj = toClass.newInstance();

                            //logic for editable entity
                            Method hasEditAllowanceMethod = fromClass
                                    .getDeclaredMethod("hasEditAllowance", userClass);
                            boolean editFlag = (boolean) hasEditAllowanceMethod.invoke(obj, authenticatedUser);

                            Method setAllowEditMethod = toClass.getDeclaredMethod("setAllowEdit", Boolean.class);
                            setAllowEditMethod.invoke(targetObj, editFlag);

                            //logic for delete entity
                            Method hasDeleteAllowanceMethod = fromClass
                                    .getDeclaredMethod("hasDeleteAllowance", userClass);
                            boolean deleteFlag = (boolean) hasDeleteAllowanceMethod
                                    .invoke(obj, authenticatedUser);

                            Method setAllowDeleteMethod = toClass
                                    .getDeclaredMethod("setAllowDelete", Boolean.class);
                            setAllowDeleteMethod.invoke(targetObj, deleteFlag);

                            // Logic for set organization
                            try {

                                Method getOrganizationMethod = fromClass.getDeclaredMethod("getOrganization");
                                Organization organization = (Organization) getOrganizationMethod.invoke(obj);

                                Method setOrganizationTOMethod = toClass
                                        .getDeclaredMethod("setOrganization", String.class);
                                setOrganizationTOMethod.invoke(targetObj, organization.getName());

                            } catch (NoSuchMethodException e) {
                                // Organization doesn't exist
                            }

                            BeanUtils.copyProperties(obj, targetObj);

                            listTOs.add((TO) targetObj);

                        } catch (Exception e) {
                            logger.log(Level.SEVERE, e.getMessage(), e);
                        }

                    });

                }
                return new PageImpl<>(listTOs, pageable, page.getTotalElements());
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }

        return new PageImpl<>(new ArrayList<>(), pageable, page.getTotalElements());
    }

}
