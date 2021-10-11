package by.bsu.kb.schepovpavlovets.client.util;

import by.bsu.kb.schepovpavlovets.client.exception.NoServerException;
import lombok.SneakyThrows;
import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.UUID;

@Component
public class CryptUtility {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private static final String SERVER_ID_REGEX = "\\{server_id}";
    public static final int KEY_LEN_BYTES = 32;
    public static final int IV_LEN_BYTES = 16;
    @Value("${content.key.private.path}")
    private String privateKeyPath;
    @Value("${content.key.public.path}")
    private String publicKeyPath;
    @Value("${content.key.server.public.path}")
    private String serverPublicKeyPathTemplate;
    @Value("${content.key.folder}")
    private String keyFolder;
    @Value("${content.env-var}")
    private String contentEnvVar;
    private PrivateKey privateKey;
    private PublicKey publicKey;

    private enum EncryptMode {
        ENCRYPT, DECRYPT;
    }

    @SneakyThrows
    @PostConstruct
    private void init() {
        String contentPath = System.getenv(contentEnvVar);
        File keysFolder = new File(contentPath + keyFolder);
        if (!keysFolder.exists()) {
            Files.createDirectory(keysFolder.toPath());
        }
        File publicKeyFile = new File(contentPath + publicKeyPath);
        File privateKeyFile = new File(contentPath + privateKeyPath);
        if (!privateKeyFile.exists() || !publicKeyFile.exists()) {
            generateRSAKeyPair();
        } else {
            byte[] publicKeyBytes = Files.readAllBytes(publicKeyFile.toPath());
            byte[] privateKeyBytes = Files.readAllBytes(privateKeyFile.toPath());
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
            publicKey = keyFactory.generatePublic(publicKeySpec);
            EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            privateKey = keyFactory.generatePrivate(privateKeySpec);
        }
    }

    @SneakyThrows
    public void saveServerPublicKey(byte[] publicKeyBytes, UUID serverId) {
        String contentPath = System.getenv(contentEnvVar);
        String serverPublicKeyPath = serverPublicKeyPathTemplate.replaceFirst(SERVER_ID_REGEX, serverId.toString());
        try (FileOutputStream fos = new FileOutputStream((contentPath + serverPublicKeyPath))) {
            fos.write(publicKeyBytes);
        }
    }

    @SneakyThrows
    public String decodeBytesToBase64RSA(byte[] encodedBytes) {
        return Base64.encodeBase64String(decodeBytesRSA(encodedBytes));
    }

    @SneakyThrows
    public String decodeBytesToStringRSA(byte[] encodedBytes) {
        return new String(decodeBytesRSA(encodedBytes), StandardCharsets.UTF_8);
    }

    @SneakyThrows
    public byte[] decodeBytesRSA(byte[] encodedBytes) {
        Cipher decryptCipher = Cipher.getInstance("RSA");
        decryptCipher.init(Cipher.DECRYPT_MODE, privateKey);
        return decryptCipher.doFinal(encodedBytes);
    }

    @SneakyThrows
    public byte[] encodeBytesRSA(byte[] toEncode) {
        Cipher encryptCipher = Cipher.getInstance("RSA");
        encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return encryptCipher.doFinal(toEncode);
    }

    @SneakyThrows
    public byte[] encodeStringRSA(String toEncode) {
        return encodeBytesRSA(toEncode.getBytes(StandardCharsets.UTF_8));
    }

    @SneakyThrows
    public byte[] encodeBytesForServerRSA(byte[] toEncode, UUID serverId) {
        String contentPath = System.getenv(contentEnvVar);
        File serverPublicKeyFile = new File(contentPath + serverPublicKeyPathTemplate.replaceFirst(SERVER_ID_REGEX, serverId.toString()));
        PublicKey serverPublicKey;
        if (serverPublicKeyFile.exists()) {
            byte[] serverPublicKeyBytes = Files.readAllBytes(serverPublicKeyFile.toPath());
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(serverPublicKeyBytes);
            serverPublicKey = keyFactory.generatePublic(publicKeySpec);
        } else {
            throw new NoServerException("Not signed up for the server!");
        }
        Cipher encryptCipher = Cipher.getInstance("RSA");
        encryptCipher.init(Cipher.ENCRYPT_MODE, serverPublicKey);
        return encryptCipher.doFinal(toEncode);
    }

    @SneakyThrows
    public byte[] encodeStringForServerRSA(String toEncode, UUID serverId) {
        return encodeBytesForServerRSA(toEncode.getBytes(StandardCharsets.UTF_8), serverId);
    }

    @SneakyThrows
    public void generateRSAKeyPair() {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair pair = generator.generateKeyPair();
        privateKey = pair.getPrivate();
        publicKey = pair.getPublic();
        String contentPath = System.getenv(contentEnvVar);
        try (FileOutputStream fos = new FileOutputStream(contentPath + publicKeyPath)) {
            fos.write(publicKey.getEncoded());
        }
        try (FileOutputStream fos = new FileOutputStream(contentPath + privateKeyPath)) {
            fos.write(privateKey.getEncoded());
        }
    }

    public byte[] getPublicKeyEncoded() {
        return publicKey != null ? publicKey.getEncoded() : null;
    }

    public String encryptSerpent(String plainText, String key, String iv) {
        return encryptDecrypt(plainText, key, EncryptMode.ENCRYPT, iv);
    }


    public String decryptSerpent(String encryptedText, String key, String iv) {
        return encryptDecrypt(encryptedText, key, EncryptMode.DECRYPT, iv);
    }

    @SneakyThrows
    private String encryptDecrypt(String inputText, String encryptionKey,
            EncryptMode mode, String initVector) {
        byte[] keyBytes = new byte[KEY_LEN_BYTES];
        byte[] ivBytes = new byte[IV_LEN_BYTES];

        System.arraycopy(Base64.decodeBase64(encryptionKey), 0, keyBytes, 0, KEY_LEN_BYTES);
        System.arraycopy(Base64.decodeBase64(initVector), 0, ivBytes, 0, IV_LEN_BYTES);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "Serpent");
        IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        Cipher cipher = Cipher.getInstance("Serpent/CBC/PKCS5Padding");

        if (mode.equals(EncryptMode.ENCRYPT)) {
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            byte[] results = cipher.doFinal(inputText.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeBase64String(results);
        }
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        byte[] decodedValue = Base64.decodeBase64(inputText.getBytes(StandardCharsets.UTF_8));
        byte[] decryptedVal = cipher.doFinal(decodedValue);
        return new String(decryptedVal, StandardCharsets.UTF_8);
    }
}
