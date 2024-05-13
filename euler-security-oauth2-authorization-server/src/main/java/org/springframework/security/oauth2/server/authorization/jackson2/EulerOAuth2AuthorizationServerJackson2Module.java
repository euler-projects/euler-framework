package org.springframework.security.oauth2.server.authorization.jackson2;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.eulerframework.security.core.DefaultEulerAuthority;
import org.eulerframework.security.core.userdetails.DefaultEulerUserDetails;
import org.eulerframework.security.jackson2.EulerAuthorityMixin;
import org.eulerframework.security.jackson2.EulerUserDetailsMixin;


public class EulerOAuth2AuthorizationServerJackson2Module extends SimpleModule {

	public EulerOAuth2AuthorizationServerJackson2Module() {
		super(EulerOAuth2AuthorizationServerJackson2Module.class.getName(), new Version(1, 0, 0, null, null, null));
	}

	@Override
	public void setupModule(SetupContext context) {
		context.setMixInAnnotations(DefaultEulerUserDetails.class, EulerUserDetailsMixin.class);
		context.setMixInAnnotations(DefaultEulerAuthority.class, EulerAuthorityMixin.class);
	}

}
