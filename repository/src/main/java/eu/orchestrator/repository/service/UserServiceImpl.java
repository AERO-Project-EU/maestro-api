package eu.orchestrator.repository.service;

import eu.orchestrator.repository.api.IUserService;
import eu.orchestrator.repository.dao.UserDAO;
import eu.orchestrator.repository.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import jakarta.transaction.Transactional;
import java.util.Optional;

@Component
@Transactional
public class UserServiceImpl implements IUserService<User> {

  @Autowired
  UserDAO userDAO;

  @Override
  public Optional<User> findByUsername(String username) {
    return userDAO.findByUsername(username);
  }

}
