package by.bsu.kb.schepovpavlovets.server.util;

import by.bsu.kb.schepovpavlovets.server.exception.UnauthorizedException;
import by.bsu.kb.schepovpavlovets.server.model.dto.SignedMessageDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
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
import java.security.spec.ECGenParameterSpec;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.UUID;

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

    private static final String CLIENT_PUBLIC_KEY_PATH_TEMPLATE = "\\{clientId}";
    public static final int KEY_LEN_BYTES = 32;
    public static final int IV_LEN_BYTES = 16;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Value("${content.env-var}")
    private String contentEnvVar;
    @Value("${content.key.private.path}")
    private String privateKeyPath;
    @Value("${content.key.public.path}")
    private String publicKeyPath;
    @Value("${content.key.folder}")
    private String keyFolder;
    @Value("${content.key.client.public.path}")
    private String clientPublicKeyPath;
    @Value("${content.key.client.public.rsa.path}")
    private String clientPublicKeyRSAPath;
    private PrivateKey privateKey;
    private PublicKey publicKey;

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
            generateECDSAKeyPair();
        } else {
            byte[] publicKeyBytes = Files.readAllBytes(publicKeyFile.toPath());
            byte[] privateKeyBytes = Files.readAllBytes(privateKeyFile.toPath());
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
            publicKey = keyFactory.generatePublic(publicKeySpec);
            EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            privateKey = keyFactory.generatePrivate(privateKeySpec);
        }
    }

    public String encryptSerpent(String plainText, String key, String iv) {
        return encryptDecrypt(plainText, key, EncryptMode.ENCRYPT, iv);
    }


    public String decryptSerpent(String encryptedText, String key, String iv) {
        return encryptDecrypt(encryptedText, key, EncryptMode.DECRYPT, iv);
    }

    public byte[] generateRandomBytes(int length) {
        SecureRandom ranGen = new SecureRandom();
        byte[] bytes = new byte[length];
        ranGen.nextBytes(bytes);
        return bytes;
    }

    @SneakyThrows
    public void saveClientPublicKey(byte[] publicKeyBytes, String clientId) {
        String contentPath = System.getenv(contentEnvVar);
        try (FileOutputStream fos = new FileOutputStream((contentPath + clientPublicKeyPath).replaceFirst(CLIENT_PUBLIC_KEY_PATH_TEMPLATE, clientId))) {
            fos.write(publicKeyBytes);
        }
    }

    @SneakyThrows
    public void saveClientPublicKeyRSA(byte[] publicKeyBytes, String clientId) {
        String contentPath = System.getenv(contentEnvVar);
        try (FileOutputStream fos = new FileOutputStream((contentPath + clientPublicKeyRSAPath).replaceFirst(CLIENT_PUBLIC_KEY_PATH_TEMPLATE, clientId))) {
            fos.write(publicKeyBytes);
        }
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
    public byte[] encodeBytesRSA(byte[] toEncode, String clientId) {
        String contentPath = System.getenv(contentEnvVar);
        File file = new File((contentPath + clientPublicKeyRSAPath).replaceFirst(CLIENT_PUBLIC_KEY_PATH_TEMPLATE, clientId));
        byte[] publicKeyBytes = Files.readAllBytes(file.toPath());
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
        PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
        Cipher encryptCipher = Cipher.getInstance("RSA");
        encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return encryptCipher.doFinal(toEncode);
    }

    @SneakyThrows
    public byte[] encodeStringRSA(String toEncode, String clientId) {
        return encodeBytesRSA(toEncode.getBytes(StandardCharsets.UTF_8), clientId);
    }

    @SneakyThrows
    public String encodeStringBase64RSA(String toEncode, String clientId) {
        return Base64.encodeBase64String(encodeBytesRSA(toEncode.getBytes(StandardCharsets.UTF_8), clientId));
    }

    @SneakyThrows
    public String encodeBytesBase64RSA(byte[] toEncode, String clientId) {
        return Base64.encodeBase64String(encodeBytesRSA(toEncode, clientId));
    }

    @SneakyThrows
    private void generateRSAKeyPair() {
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

    @SneakyThrows
    private void generateECDSAKeyPair() {
        ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256r1");
        KeyPairGenerator g = KeyPairGenerator.getInstance("EC");
        g.initialize(ecSpec, new SecureRandom());
        KeyPair keypair = g.generateKeyPair();
        publicKey = keypair.getPublic();
        privateKey = keypair.getPrivate();
        String contentPath = System.getenv(contentEnvVar);
        try (FileOutputStream fos = new FileOutputStream(contentPath + publicKeyPath)) {
            fos.write(publicKey.getEncoded());
        }
        try (FileOutputStream fos = new FileOutputStream(contentPath + privateKeyPath)) {
            fos.write(privateKey.getEncoded());
        }
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
        byte[] decodedValue = Base64.decodeBase64(inputText);
        byte[] decryptedVal = cipher.doFinal(decodedValue);
        return new String(decryptedVal, StandardCharsets.UTF_8);

    }

    public byte[] getPublicKeyEncoded() {
        return publicKey != null ? publicKey.getEncoded() : null;
    }

    @SneakyThrows
    public <T> SignedMessageDto<T> signResponse(T content) {
        Signature ecdsaSign = Signature.getInstance("SHA256withECDSA");
        ecdsaSign.initSign(privateKey);
        String contentString = objectMapper.writeValueAsString(content);
        ecdsaSign.update(contentString.getBytes(StandardCharsets.UTF_8));
        byte[] signature = ecdsaSign.sign();
        String sig = Base64.encodeBase64String(signature);
        SignedMessageDto<T> signedMessageDto = new SignedMessageDto<>();
        signedMessageDto.setSignature(sig);
        signedMessageDto.setAlgorithm("SHA256withECDSA");
        signedMessageDto.setContent(content);
        return signedMessageDto;
    }

    @SneakyThrows
    public void verifySignature(SignedMessageDto<?> signedMessageDto, UUID clientId) {
        Signature ecdsaVerify = Signature.getInstance(signedMessageDto.getAlgorithm());
        String contentPath = System.getenv(contentEnvVar);
        File clientPublicKeyFile = new File(contentPath + clientPublicKeyPath.replaceFirst(CLIENT_PUBLIC_KEY_PATH_TEMPLATE, clientId.toString()));
        PublicKey serverPublicKey;
        if (clientPublicKeyFile.exists()) {
            byte[] serverPublicKeyBytes = Files.readAllBytes(clientPublicKeyFile.toPath());
            KeyFactory kf = KeyFactory.getInstance("EC");
            EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(serverPublicKeyBytes);
            serverPublicKey = kf.generatePublic(publicKeySpec);
        } else {
            throw new UnauthorizedException(UnauthorizedException.Status.INVALID_CLIENT_ID.name());
        }
        ecdsaVerify.initVerify(serverPublicKey);
        ecdsaVerify.update(objectMapper.writeValueAsBytes(signedMessageDto.getContent()));
        if (!ecdsaVerify.verify(Base64.decodeBase64(signedMessageDto.getSignature()))) {
            throw new UnauthorizedException(UnauthorizedException.Status.INVALID_SIGNATURE.name());
        }
    }

    @SneakyThrows
    public void verifySignature(SignedMessageDto<?> signedMessageDto, String publicKeyBase64) {
        Signature ecdsaVerify = Signature.getInstance(signedMessageDto.getAlgorithm());
        byte[] serverPublicKeyBytes = Base64.decodeBase64(publicKeyBase64);
        KeyFactory kf = KeyFactory.getInstance("EC");
        EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(serverPublicKeyBytes);
        PublicKey serverPublicKey = kf.generatePublic(publicKeySpec);
        ecdsaVerify.initVerify(serverPublicKey);
        ecdsaVerify.update(objectMapper.writeValueAsBytes(signedMessageDto.getContent()));
        if (!ecdsaVerify.verify(Base64.decodeBase64(signedMessageDto.getSignature()))) {
            throw new UnauthorizedException(UnauthorizedException.Status.INVALID_SIGNATURE.name());
        }
    }
}
