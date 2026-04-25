package com.halo.lims.service;

import org.hashids.Hashids;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class HashidService {

    @Value("${app.security.hashids.salt:halo-lims-prod-salt-7643}")
    private String salt;

    @Value("${app.security.hashids.min-length:8}")
    private int minHashLength;

    private Hashids hashids;

    @PostConstruct
    public void init() {
        this.hashids = new Hashids(salt, minHashLength);
    }

    /**
     * Encodes a numeric ID into an obfuscated hash string.
     */
    public String encode(long id) {
        return hashids.encode(id);
    }

    /**
     * Decodes an obfuscated hash string back into a numeric ID.
     * Returns -1 if decoding fails.
     */
    public long decode(String hash) {
        long[] ids = hashids.decode(hash);
        if (ids.length > 0) {
            return ids[0];
        }
        return -1;
    }

    /**
     * Helper to encode Integer IDs.
     */
    public String encode(Integer id) {
        if (id == null) return null;
        return encode(id.longValue());
    }

    /**
     * Helper to decode to Integer.
     */
    public Integer decodeToInt(String hash) {
        long decoded = decode(hash);
        return decoded == -1 ? null : (int) decoded;
    }
}
