package net.eulerframework.web.module.oauth2.service;

import net.eulerframework.web.module.oauth2.entity.EulerOAuth2ClientEntity;

/**
 * @author cFrost
 *
 */
public interface EulerOAuth2ClientEntityService {

    EulerOAuth2ClientEntity loadClientById(String clientId);
    
}
