/*
 * Copyright 2013-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eulerframework.security.jackson2;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.eulerframework.security.core.EulerGrantedAuthority;
import org.eulerframework.security.core.userdetails.EulerUserDetails;
import org.springframework.security.jackson2.SecurityJackson2Modules;

public class EulerSecurityJackson2Module extends SimpleModule {

	public EulerSecurityJackson2Module() {
		super(EulerSecurityJackson2Module.class.getName(), new Version(1, 0, 0, null, null, null));
	}

	@Override
	public void setupModule(SetupContext context) {
		SecurityJackson2Modules.enableDefaultTyping(context.getOwner());
		context.setMixInAnnotations(EulerUserDetails.class, EulerUserDetailsMixin.class);
		context.setMixInAnnotations(EulerGrantedAuthority.class, EulerGrantedAuthorityMixin.class);
	}

}
