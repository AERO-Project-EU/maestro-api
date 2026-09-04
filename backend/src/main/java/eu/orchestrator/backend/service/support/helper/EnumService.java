package eu.orchestrator.backend.service.support.helper;

import eu.orchestrator.common.exception.GenericBusinessException;
import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.transfer.entities.ui.UIEnum;

import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.transaction.Transactional;

@Service
@Transactional(rollbackOn = Exception.class)
public class EnumService {

    private static final Logger logger = Logger.getLogger(EnumService.class.getName());


    public List<UIEnum> fetchEnumValues(String enumClass, String enumValue) throws ClassNotFoundException {
        if (null != enumClass && !enumClass.isEmpty() && null != enumValue && !enumValue.isEmpty()) {
            List<UIEnum> enumValues = new ArrayList<>();
            Class clazzMyEnum = Class.forName("eu.orchestrator.repository.domain." + enumClass + "$" + enumValue,
                    true, Thread.currentThread().getContextClassLoader());
            if (clazzMyEnum.isEnum()) {
                Arrays.stream(clazzMyEnum.getEnumConstants()).forEach(obj -> {
                    try {
                        UIEnum uiEnum = new UIEnum();
                        uiEnum.setName(obj.toString());
                        Method methodValueOf = clazzMyEnum.getDeclaredMethod("getFriendlyName");
                        Object oo = methodValueOf.invoke(obj);
                        uiEnum.setFriendlyName(oo.toString());
                        enumValues.add(uiEnum);
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, e.getMessage(), e);
                    }
                });
            }
            return enumValues;
        }
        throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
    }

    public UIEnum fetchEnumValue(String enumClass, String enumValue, String name) throws ClassNotFoundException {
        if (null != enumClass && !enumClass.isEmpty() && null != enumValue && !enumValue.isEmpty() && null != name && !name.isEmpty()) {
            UIEnum uiEnum = new UIEnum();
            Class clazzMyEnum = Class.forName("eu.orchestrator.repository.domain." + enumClass + "$" + enumValue,
                    true, Thread.currentThread().getContextClassLoader());
            if (clazzMyEnum.isEnum()) {
                Arrays.stream(clazzMyEnum.getEnumConstants()).filter(obj -> obj.toString().equals(name)).forEach(obj -> {
                    try {
                        uiEnum.setName(obj.toString());
                        Method methodValueOf = clazzMyEnum.getDeclaredMethod("getFriendlyName");
                        Object oo = methodValueOf.invoke(obj);
                        uiEnum.setFriendlyName(oo.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                        logger.log(Level.SEVERE, e.getMessage(), e);
                    }
                });
                return uiEnum;
            }
        }
        throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
    }
}
