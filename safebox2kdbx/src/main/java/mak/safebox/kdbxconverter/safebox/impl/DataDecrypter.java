/**
 * 
 */
package mak.safebox.kdbxconverter.safebox.impl;

import static java.nio.charset.StandardCharsets.UTF_8;
import static mak.safebox.kdbxconverter.safebox.impl.SafeboxNumbersHelper.BYTES_IN_INTEGER;
import static mak.safebox.kdbxconverter.safebox.impl.SafeboxNumbersHelper.bytesToInt;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.lang.Nullable;

/**
 * Safebox data decrypter.
 */
class DataDecrypter {

    /**
     * Key length in bytes (16 bytes = 256 bits).
     */
    private static final int KEY_LENGTH_IN_BYTES = 16;

    /**
     * The secret key.
     */
    private final SecretKeySpec secretKeySpec;

    /**
     * Constructor.
     * 
     * @param password
     *            the Safebox user password
     */
    DataDecrypter(final String password) {
        try {
            final MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            secretKeySpec = new SecretKeySpec(messageDigest.digest(password.getBytes(UTF_8)), "AES");
        } catch (final NoSuchAlgorithmException exception) {
            throw new RuntimeException("Unable to intanciate SHA-256 message digest", exception);
        }
    }

    /**
     * Decrypts the source byte array.
     * 
     * @param value
     *            the byte array to decrypt
     * @return the decrypted byte array
     */
    public byte[] decrypt(final byte[] value) {
        final byte[] param1 = new byte[KEY_LENGTH_IN_BYTES];
        System.arraycopy(value, 0, param1, 0, param1.length);
        final byte[] param2 = new byte[value.length - param1.length];
        System.arraycopy(value, param1.length, param2, 0, param2.length);
        final byte[] decrypted;
        try {
            final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(param1));
            decrypted = cipher.doFinal(param2);
        } catch (final Exception exception) {
            throw new RuntimeException("It seems a bad password was provided", exception);
        }

        final int len = Math.min(bytesToInt(decrypted), decrypted.length - BYTES_IN_INTEGER);
        final byte[] result = new byte[len];
        System.arraycopy(decrypted, BYTES_IN_INTEGER, result, 0, result.length);

        return result;
    }

    /**
     * Decrypts the source byte array and return the result as string.
     * 
     * @param value
     *            the byte array to decrypt
     * @return a decrypted string
     */
    @Nullable
    public String decryptString(@Nullable
    final byte[] value) {
        return value == null ? null : new String(decrypt(value), UTF_8);
    }

}
