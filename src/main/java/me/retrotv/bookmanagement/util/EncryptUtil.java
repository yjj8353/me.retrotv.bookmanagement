package me.retrotv.bookmanagement.util;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import me.retrotv.bookmanagement.exception.CommonServerErrorException;

public class EncryptUtil {
    
    // 인스턴스화 방지
    private EncryptUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public class SHA {

        // 인스턴스화 방지
        private SHA() {
            throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
        }

        public static String sha256(String input) {
            String toReturn = null;

            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                digest.reset();
                digest.update(input.getBytes(StandardCharsets.UTF_8));
                toReturn = String.format("%064x", new BigInteger(1, digest.digest()));
            } catch (NoSuchAlgorithmException e) {
                throw new CommonServerErrorException("");
            }
            
            return toReturn;
        }
    }
}
