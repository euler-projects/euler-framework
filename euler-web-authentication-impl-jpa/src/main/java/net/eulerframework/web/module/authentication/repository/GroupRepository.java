package net.eulerframework.web.module.authentication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.eulerframework.web.module.authentication.entity.Group;

/**
 * @author cFrost
 *
 */
@Repository
public interface GroupRepository extends JpaRepository<Group, String> {

    public Group findGroupById(String groupId);

    public Group findGroupByCode(String code);
}
