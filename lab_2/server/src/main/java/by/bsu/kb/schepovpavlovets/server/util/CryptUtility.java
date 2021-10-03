package by.bsu.kb.schepovpavlovets.server.util;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

@RequiredArgsConstructor
@Component
@Slf4j
public class CryptUtility {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private enum EncryptMode {
        ENCRYPT, DECRYPT;
    }

    private static final String PUBLIC_KEY_PATH_TEMPLATE = "\\{clientId}";
    public static final int KEY_LEN_BYTES = 32;
    public static final int IV_LEN_BYTES = 16;
    @Value("${content.env-var}")
    private String contentEnvVar;
    @Value("${content.key.public.path}")
    private String publicKeyPath;

    @SneakyThrows
    private String encryptDecrypt(String inputText, String encryptionKey,
            EncryptMode mode, String initVector) {
        byte[] keyBytes = new byte[KEY_LEN_BYTES];
        byte[] ivBytes = new byte[IV_LEN_BYTES];

        System.arraycopy(encryptionKey.getBytes(StandardCharsets.UTF_8), 0, keyBytes, 0, KEY_LEN_BYTES);
        System.arraycopy(initVector.getBytes(StandardCharsets.UTF_8), 0, ivBytes, 0, IV_LEN_BYTES);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "Serpent");
        IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        Cipher cipher = Cipher.getInstance("Serpent/CBC/PKCS5Padding");

        if (mode.equals(EncryptMode.ENCRYPT)) {
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            byte[] results = cipher.doFinal(inputText.getBytes(StandardCharsets.UTF_8));
            return new String(Base64.encodeBase64(results,false));
        }
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        byte[] decodedValue = Base64.decodeBase64(inputText.getBytes(StandardCharsets.UTF_8));
        byte[] decryptedVal = cipher.doFinal(decodedValue);
        return new String(decryptedVal, StandardCharsets.UTF_8);

    }

    public String encrypt(String plainText, String key, String iv) {
        return encryptDecrypt(plainText, key, EncryptMode.ENCRYPT, iv);
    }


    public String decrypt(String encryptedText, String key, String iv) {
        return encryptDecrypt(encryptedText, key, EncryptMode.DECRYPT, iv);
    }

    public byte[] generateRandomBytes(int length) {
        SecureRandom ranGen = new SecureRandom();
        byte[] bytes = new byte[length];
        ranGen.nextBytes(bytes);
        return bytes;
    }

    @SneakyThrows
    public void savePublicKey(byte[] publicKeyBytes, String clientId) {
        String contentPath = System.getenv(contentEnvVar);
        try (FileOutputStream fos = new FileOutputStream((contentPath + publicKeyPath).replaceFirst(PUBLIC_KEY_PATH_TEMPLATE, clientId))) {
            fos.write(publicKeyBytes);
        }
    }

    @SneakyThrows
    public byte[] encodeBytes(byte[] toEncode, String clientId) {
        String contentPath = System.getenv(contentEnvVar);
        File file = new File((contentPath + publicKeyPath).replaceFirst(PUBLIC_KEY_PATH_TEMPLATE, clientId));
        byte[] publicKeyBytes = Files.readAllBytes(file.toPath());
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
        PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
        Cipher encryptCipher = Cipher.getInstance("RSA");
        encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return encryptCipher.doFinal(toEncode);
    }

    @SneakyThrows
    public byte[] encodeString(String toEncode, String clientId) {
        return encodeBytes(toEncode.getBytes(StandardCharsets.UTF_8), clientId);
    }

    @SneakyThrows
    public String encodeStringBase64(String toEncode, String clientId) {
        return Base64.encodeBase64String(encodeBytes(toEncode.getBytes(StandardCharsets.UTF_8), clientId));
    }
}
