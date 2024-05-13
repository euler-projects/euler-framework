package org.springframework.security.oauth2.server.authorization.jackson2;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.eulerframework.security.spring.principal.EulerUserDetails;


public class EulerOAuth2AuthorizationServerJackson2Module extends SimpleModule {

	public EulerOAuth2AuthorizationServerJackson2Module() {
		super(EulerOAuth2AuthorizationServerJackson2Module.class.getName(), new Version(1, 0, 0, null, null, null));
	}

	@Override
	public void setupModule(SetupContext context) {
		context.setMixInAnnotations(EulerUserDetails.class, EulerUserDetailsMixin.class);
	}

}
