package net.eulerframework.web.module.authentication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.eulerframework.web.module.authentication.entity.BasicUserProfile;

/**
 * @author cFrost
 *
 */
@Repository
public interface BasicUserProfileRepository extends JpaRepository<BasicUserProfile, String> {
}
