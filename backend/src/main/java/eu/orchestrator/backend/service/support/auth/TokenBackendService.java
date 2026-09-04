package eu.orchestrator.backend.service.support.auth;

import eu.orchestrator.common.exception.NotAuthorizedException;
import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.backend.transfer.UserTokenTO;
import eu.orchestrator.repository.dao.TokenDAO;
import eu.orchestrator.repository.domain.Token;
import eu.orchestrator.repository.domain.User;

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
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.transaction.Transactional;

import static eu.orchestrator.repository.domain.QToken.token1;

@Service
@Transactional(rollbackOn = Exception.class)
public class TokenBackendService {

    private static final Logger logger = Logger.getLogger(TokenBackendService.class.getName());

    @Autowired
    private TokenDAO tokenDAO;


    public void delete(Long id, User authenticatedUser) {
        if (!authenticatedUser.isAdmin()) {
            throw new NotAuthorizedException(GenericMessage.USER_NOT_AUTHORIZED.getCode(), GenericMessage.USER_NOT_AUTHORIZED);
        }
        Optional<Token> existingTokenOP = tokenDAO.findById(id);
        if (existingTokenOP.isPresent()) {
            Token existingToken = existingTokenOP.get();
            tokenDAO.delete(existingToken);
        } else {
            throw new NotAuthorizedException(GenericMessage.NOT_AUTHORIZED.getCode(), GenericMessage.NOT_AUTHORIZED);
        }
    }

    public Page fetchToken(Pageable pageable, String filters, Token fToken, User authenticatedUser) {
        BooleanExpression predicate = token1.eq(token1);
        boolean checkRequestParam = null != filters && !filters.isEmpty();
        boolean proceedWithRequestBody = true;
        if (checkRequestParam) {
            try {
                Token filterToken = new Gson().fromJson(new String(Base64.decodeBase64(filters)), Token.class);
                if (null != filterToken) {
                    proceedWithRequestBody = false;
                    if (null != filterToken.getName() && !filterToken.getName().isEmpty()) {
                        predicate = predicate.and(token1.name.containsIgnoreCase(filterToken.getName()));
                    }
                }
            } catch (Exception ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        if (proceedWithRequestBody) {
            if (null != fToken) {
                if (null != fToken.getName() && !fToken.getName().isEmpty()) {
                    predicate = predicate.and(token1.name.containsIgnoreCase(fToken.getName()));
                }
            }
        }
        predicate = predicate.and(token1.user.eq(authenticatedUser));
        Page<Token> page;
        if (pageable.getPageSize() > 100) {
            page = tokenDAO.findAll(predicate, PageRequest.of(pageable.getPageNumber(), 100, pageable.getSort()));
        } else {
            page = tokenDAO.findAll(predicate, pageable);
        }
        List<UserTokenTO> tokenTOs = new ArrayList<>();
        if (null != page && null != page.getContent() && !page.getContent().isEmpty()) {
            page.getContent().forEach(token -> {
                UserTokenTO tokenTO = new UserTokenTO();
                tokenTO.setToken(token.getToken());
                tokenTO.setName(token.getName());
                tokenTO.setUsername(token.getUser().getUsername());
                tokenTO.setExpirationDate(token.getExpirationDate());
                tokenTO.setId(token.getId());
                tokenTOs.add(tokenTO);
            });
        }
        return new PageImpl<>(tokenTOs, pageable, page.getTotalElements());
    }

}
