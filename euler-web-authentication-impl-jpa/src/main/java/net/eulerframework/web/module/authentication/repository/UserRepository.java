package net.eulerframework.web.module.authentication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.eulerframework.web.module.authentication.entity.User;

/**
 * @author cFrost
 *
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

    public User findUserById(String userId);
    public User findUserByUsernameIgnoreCase(String username);
    public User findUserByEmailIgnoreCase(String email);
    public User findUserByMobileIgnoreCase(String mobile);
}
