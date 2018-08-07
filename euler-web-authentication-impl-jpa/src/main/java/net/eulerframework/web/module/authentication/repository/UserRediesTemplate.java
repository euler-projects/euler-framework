package net.eulerframework.web.module.authentication.repository;

import org.springframework.stereotype.Component;

import net.eulerframework.redis.AbstractStringKeyRedisTemplate;
import net.eulerframework.web.module.authentication.entity.User;

/**
 * @author cFrost
 *
 */
@Component
public class UserRediesTemplate extends AbstractStringKeyRedisTemplate<User> {

}
