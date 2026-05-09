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
package org.eulerframework.data.convert.encrypt;

import jakarta.persistence.AttributeConverter;
import org.eulerframework.security.crypto.DataCipher;
import org.springframework.util.Assert;

/**
 * Template base for any JPA {@link AttributeConverter} that wants to
 * transparently encrypt a domain value as a single {@link String} column.
 * <p>
 * Subclasses only need to implement the domain-specific
 * {@code T <-> plaintext String} mapping via {@link #serialize(Object)} and
 * {@link #deserialize(String)}. Encryption, envelope framing and cipher
 * dispatch are fully handled by the injected {@link DataCipher}.
 * <p>
 * The {@link DataCipher} bean consumed here is wired uniformly for the whole
 * persistence layer by the {@code euler.data.jpa.encryption} auto-configuration,
 * so every subclass of this converter shares the same key-rotation and
 * algorithm-fallback semantics regardless of which entity attribute it encrypts.
 *
 * @param <T> the entity attribute type
 */
public abstract class AbstractEncryptedAttributeConverter<T> implements AttributeConverter<T, String> {

    private final DataCipher dataCipher;

    protected AbstractEncryptedAttributeConverter(DataCipher dataCipher) {
        Assert.notNull(dataCipher, "dataCipher is required");
        this.dataCipher = dataCipher;
    }

    /** Serialize the domain value into its plaintext {@link String} form. */
    protected abstract String serialize(T value);

    /** Inverse of {@link #serialize(Object)}. */
    protected abstract T deserialize(String plaintext);

    @Override
    public final String convertToDatabaseColumn(T attribute) {
        if (attribute == null) {
            return null;
        }
        return this.dataCipher.encrypt(serialize(attribute));
    }

    @Override
    public final T convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return deserialize(this.dataCipher.decrypt(dbData));
    }
}
