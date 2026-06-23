/*
 * Copyright 2013-present the original author or authors.
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

package org.eulerframework.properties;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Base implementation for configuration properties.
 *
 * @author Joe Grandja
 * @since 7.0
 */
public abstract class AbstractProperties implements Serializable {

	private final Map<String, Object> properties;

	protected AbstractProperties(@Nonnull Map<String, Object> properties) {
		Assert.notEmpty(properties, "properties cannot be empty");
		this.properties = Collections.unmodifiableMap(new HashMap<>(properties));
	}

	/**
	 * Returns a configuration property.
	 * @param name the name of the property
	 * @param <T> the type of the property
	 * @return the value of the property, or {@code null} if not available
	 */
	@Nullable
	@SuppressWarnings("unchecked")
	public <T> T getProperty(String name) {
		Assert.hasText(name, "name cannot be empty");
		return (T) getProperties().get(name);
	}

	/**
	 * Returns a {@code Map} of the configuration properties.
	 * @return a {@code Map} of the configuration properties
	 */
	@Nonnull
	public Map<String, Object> getProperties() {
		return this.properties;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		AbstractProperties that = (AbstractProperties) obj;
		return this.properties.equals(that.properties);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.properties);
	}

	@Override
	public String toString() {
		return "AbstractProperties {" + "properties=" + this.properties + '}';
	}

	/**
	 * A builder for subclasses of {@link AbstractProperties}.
	 *
	 * @param <T> the type of object
	 * @param <B> the type of the builder
	 */
	protected abstract static class AbstractBuilder<T extends AbstractProperties, B extends AbstractBuilder<T, B>> {

		private final Map<String, Object> properties = new HashMap<>();

		protected AbstractBuilder() {
		}

		/**
		 * Sets a configuration property.
		 * @param name the name of the property
		 * @param value the value of the property
		 * @return the {@link AbstractBuilder} for further configuration
		 */
		public B property(String name, Object value) {
			Assert.hasText(name, "name cannot be empty");
			getProperties().put(name, value);
			return getThis();
		}

		/**
		 * A {@code Consumer} of the configuration properties {@code Map} allowing the
		 * ability to add, replace, or remove.
		 * @param propertiesConsumer a {@link Consumer} of the configuration properties
		 * {@code Map}
		 * @return the {@link AbstractBuilder} for further configuration
		 */
		public B properties(Consumer<Map<String, Object>> propertiesConsumer) {
			propertiesConsumer.accept(getProperties());
			return getThis();
		}

		public abstract T build();

		protected final Map<String, Object> getProperties() {
			return this.properties;
		}

		@SuppressWarnings("unchecked")
		protected final B getThis() {
			return (B) this;
		}

	}

}
