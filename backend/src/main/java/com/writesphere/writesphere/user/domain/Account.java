package com.writesphere.writesphere.user.domain;

import java.text.Normalizer;
import java.util.Locale;
import java.util.UUID;

public record Account(UUID id, String email, String penName, String penNameKey,
                      String passwordHash, Role role) {
    public enum Role { MEMBER, MODERATOR }

    public static String normalize(String value) {
        return Normalizer.normalize(value.strip(), Normalizer.Form.NFKC).toLowerCase(Locale.ROOT);
    }
}
