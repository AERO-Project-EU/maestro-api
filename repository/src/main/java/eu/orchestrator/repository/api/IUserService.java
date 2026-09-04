package eu.orchestrator.repository.api;

import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public interface IUserService<U> {

  Optional<U> findByUsername(String username);
}
