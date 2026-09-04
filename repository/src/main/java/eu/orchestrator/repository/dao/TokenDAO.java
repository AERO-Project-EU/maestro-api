package eu.orchestrator.repository.dao;

import com.querydsl.core.types.dsl.StringPath;
import eu.orchestrator.repository.domain.QToken;
import eu.orchestrator.repository.domain.Token;
import eu.orchestrator.repository.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 28/1/21
 */
@Repository
@Transactional
public interface TokenDAO extends JpaRepository<Token, Long>, QuerydslPredicateExecutor<Token>,
        QuerydslBinderCustomizer<QToken> {

    @Override
    default void customize(QuerydslBindings bindings, QToken token) {

        bindings.bind(String.class).first(
                (StringPath path, String value) -> path.containsIgnoreCase(value)
        );

        bindings.excluding(token.token);
    }

    Optional<Token> findByName(String name);

    List<Token> findAllByUser(User user);

}