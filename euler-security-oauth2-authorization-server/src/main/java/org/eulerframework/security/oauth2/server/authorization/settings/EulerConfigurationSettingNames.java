/*
 * Copyright 2004-present the original author or authors.
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

package org.eulerframework.security.oauth2.server.authorization.settings;

/**
 * The names for all the configuration settings.
 *
 * @author Joe Grandja
 * @since 7.0
 */
public final class EulerConfigurationSettingNames {

	private static final String SETTINGS_NAMESPACE = "settings.";

	private EulerConfigurationSettingNames() {
	}

	/**
	 * The names for client configuration settings.
	 */
	public static final class Client {

		private static final String CLIENT_SETTINGS_NAMESPACE = SETTINGS_NAMESPACE.concat("client.");

		/**
		 * Set the {@code URL} for the Client's JSON Web Key Set.
		 */
		public static final String JWKS = CLIENT_SETTINGS_NAMESPACE.concat("jwks");

		/**
		 * The App Attest OAuth2 client type marker ({@code STATIC} or {@code DYNAMIC}),
		 * used at token time to distinguish per-app shared clients from per-key
		 * dynamically registered clients. A client without this setting is treated as
		 * {@code STATIC} for backward compatibility.
		 */
		public static final String APP_ATTEST_CLIENT_TYPE = CLIENT_SETTINGS_NAMESPACE.concat("app-attest.client-type");

		private Client() {
		}

	}
}
