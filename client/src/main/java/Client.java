import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class Client {

    private KeyPair clientKeyPair;
    private SecretKey aesKey;

    private FileTransfer.TransferPrx proxy;

    public Client(FileTransfer.TransferPrx transferPrx) {
        this.proxy = transferPrx;
        KeyPairGenerator kpg = null;
        try {
            kpg = KeyPairGenerator.getInstance("EC");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        kpg.initialize(256);
        this.clientKeyPair = kpg.generateKeyPair();
    }

    public void negotiateKey() {
        String clientPublicKey = Base64.getEncoder().encodeToString(clientKeyPair.getPublic().getEncoded());
        String serverPublicKey = proxy.negotiateKey(clientPublicKey);
        try {
            KeyFactory kf = KeyFactory.getInstance("EC");
            X509EncodedKeySpec pkSpec = new X509EncodedKeySpec(Base64.getDecoder().decode(serverPublicKey));
            PublicKey serverKey = kf.generatePublic(pkSpec);

            KeyAgreement ka = KeyAgreement.getInstance("ECDH");
            ka.init(clientKeyPair.getPrivate());
            ka.doPhase(serverKey, true);
            byte[] sharedSecret = ka.generateSecret();

            MessageDigest hash = MessageDigest.getInstance("SHA-256");
            byte[] aesKeyBytes = hash.digest(sharedSecret);
            this.aesKey = new SecretKeySpec(aesKeyBytes, 0, 32, "AES");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void sendFile(String fileName) {
        try {
            byte[] fileContent = Files.readAllBytes(Paths.get(fileName));

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, this.aesKey);
            byte[] encryptedFile = cipher.doFinal(fileContent);

            System.out.println(proxy.sendFile(fileName, encryptedFile));

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] fileHash = digest.digest(fileContent);
            String fileHashString = Base64.getEncoder().encodeToString(fileHash);

            System.out.println(proxy.receiveHash(fileHashString, fileName));
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

}
