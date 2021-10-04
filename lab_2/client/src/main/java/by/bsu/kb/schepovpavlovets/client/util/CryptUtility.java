package by.bsu.kb.schepovpavlovets.client.util;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.Cipher;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

@Component
public class CryptUtility {

    @Value("${content.key.private.path}")
    private String privateKeyPath;
    @Value("${content.key.public.path}")
    private String publicKeyPath;
    @Value("${content.key.server.public.path}")
    private String serverPublicKeyPath;
    @Value("${content.env-var}")
    private String contentEnvVar;
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private PublicKey serverPublicKey = null;

    @SneakyThrows
    @PostConstruct
    private void init() {
        File publicKeyFile = new File(contentEnvVar + publicKeyPath);
        File privateKeyFile = new File(contentEnvVar + privateKeyPath);
        if (!privateKeyFile.exists() || !publicKeyFile.exists()) {
            generateRSAKeyPair();
        } else {
            byte[] publicKeyBytes = getClass().getClassLoader().getResourceAsStream(publicKeyPath).readAllBytes();
            byte[] privateKeyBytes = getClass().getClassLoader().getResourceAsStream(privateKeyPath).readAllBytes();
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
            publicKey = keyFactory.generatePublic(publicKeySpec);
            EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            privateKey = keyFactory.generatePrivate(privateKeySpec);
        }
    }

    @SneakyThrows
    public void saveServerPublicKey(byte[] publicKeyBytes) {
        String contentPath = System.getenv(contentEnvVar);
        try (FileOutputStream fos = new FileOutputStream((contentPath + serverPublicKeyPath))) {
            fos.write(publicKeyBytes);
        }
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
        serverPublicKey = keyFactory.generatePublic(publicKeySpec);
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
    public byte[] encodeBytesForServerRSA(byte[] toEncode) {
        Cipher encryptCipher = Cipher.getInstance("RSA");
        encryptCipher.init(Cipher.ENCRYPT_MODE, serverPublicKey);
        return encryptCipher.doFinal(toEncode);
    }

    @SneakyThrows
    public byte[] encodeStringForServerRSA(String toEncode) {
        return encodeBytesForServerRSA(toEncode.getBytes(StandardCharsets.UTF_8));
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
}
