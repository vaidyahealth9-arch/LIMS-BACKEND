package com.halo.lims.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * JPA AttributeConverter to encrypt and decrypt sensitive String attributes automatically.
 */
@Converter
@Component // Make it a Spring component to allow injection of AesGcmEncryptionUtil
public class JpaConverterJsonCipher implements AttributeConverter<String, String> {

    // Autowired instance of the encryption utility
    // Note: Spring will inject this. During JPA's internal startup, if the converter
    // is instantiated before the component scan completes, it might be null.
    // Ensure proper Spring context initialization or lazy access if issues arise.
    private static AesGcmEncryptionUtil encryptionUtil;

    @Autowired
    public void setEncryptionUtil(AesGcmEncryptionUtil encryptionUtil) {
        JpaConverterJsonCipher.encryptionUtil = encryptionUtil;
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null || attribute.isEmpty() || encryptionUtil == null) {
            return attribute;
        }
        return encryptionUtil.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty() || encryptionUtil == null) {
            return dbData;
        }
        return encryptionUtil.decrypt(dbData);
    }
}