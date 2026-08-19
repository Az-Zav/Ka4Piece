package com.ka4piece.utilities;

import java.util.UUID;

public class IdUtil {
    //Generate unique IDs with corresponding prefixes
    public static String generateId(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
