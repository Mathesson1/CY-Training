package org.cytraining.backend.utils;

import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import org.slf4j.Logger;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder.BCryptVersion;

/**
 * Class used to hash strings in a cryptographic secure way.
 * It uses the implementation of BCrypt from the SpringSecurityCrypto.
 */
public class Hasher {
    // singleton class
    private static final Hasher that = new Hasher();

    private final BCryptPasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom;

    // create a logger
    private static final Logger log = Log.createLogger(Hasher.class);

    private Hasher() {
        // TODO have a .env part to change those parameters?
        this.secureRandom = new SecureRandom();
        // the greater the strength, the better the password will be hashed, but the
        // higher the CPU load will be
        this.passwordEncoder = new BCryptPasswordEncoder(BCryptVersion.$2B, 12, this.secureRandom);
    }

    /**
     * Encode a string into a cryptographic secure hash.
     * This use multiple iterations, and is the most secure. It is also CPU
     * intensive.
     *
     * @param str The string to hash.
     * @return The hashed string.
     * @see #hash(String, Boolean, int)
     */
    public static String hash(String str) {
        // it is aknow limitation that long strings can be truncated if they exceed a
        // certain lentgh
        // if it is the case, we will pre hash the string, before encoding it
        if (str.getBytes(StandardCharsets.UTF_8).length > 72) {
            str = hash(str, true, 16);
        }

        // secure hash
        return that.passwordEncoder.encode(str);
    }

    /**
     * Encode a string into a cryptographic secure hash.
     *
     * @param str  The string to hash.
     * @param fast Whether this should be iterated over for more security, but it
     *             more CPU intensive.
     * @return The hashed string.
     */
    public static String hash(String str, Boolean fast) {
        return hash(str, fast, 32);
    }

    /**
     * Encode a string into a cryptographic secure hash.
     *
     * @param str    The string to hash.
     * @param fast   Whether this should be iterated over for more security, but it
     *               more CPU intensive.
     * @param length If {@code fast} is {@code false}, it is the length of the
     *               salt used to digest the string.
     * @return The hashed string.
     */
    public static String hash(String str, Boolean fast, int length) {
        if (length < 1) {
            throw new InvalidParameterException("\"length\" must be at least one");
        }
        if (fast == false) {
            return hash(str);
        }

        // create a salt
        byte[] salt = new byte[length];
        that.secureRandom.nextBytes(salt);
        // prepare the hash
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            Log.fatal(log, "No algorithm found", e);
            return null;
        }
        md.update(salt);
        byte[] hashed = md.digest(str.getBytes(StandardCharsets.UTF_8));
        // transform byte array to string
        return Base64.getEncoder().encodeToString(hashed);
    }

    /**
     * Check if the provided password matched the hash.
     *
     * @param str  Plain text string.
     * @param hash Hashed version of the string
     * @return True if the string match the hash, false otherwise.
     */
    public static boolean matches(String str, String hash) {
        return that.passwordEncoder.matches(str, hash);
    }
}