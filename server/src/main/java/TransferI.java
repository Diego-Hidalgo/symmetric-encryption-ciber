import com.zeroc.Ice.Current;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class TransferI implements FileTransfer.Transfer{

    private KeyPair serverKeyPair;
    private SecretKey aesKey;

    public TransferI() {
        KeyPairGenerator kpg = null;
        try {
            kpg = KeyPairGenerator.getInstance("EC");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        kpg.initialize(256);
        this.serverKeyPair = kpg.generateKeyPair();
    }

    @Override
    public String negotiateKey(String clientPublicKey, Current current) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            X509EncodedKeySpec pkSpec = new X509EncodedKeySpec(Base64.getDecoder().decode(clientPublicKey));
            PublicKey clientKey = keyFactory.generatePublic(pkSpec);
            KeyAgreement ka = KeyAgreement.getInstance("ECDH");
            ka.init(this.serverKeyPair.getPrivate());
            ka.doPhase(clientKey, true);
            byte[] sharedSecret = ka.generateSecret();
            MessageDigest hash = MessageDigest.getInstance("SHA-256");
            byte[] aesKeyBytes = hash.digest(sharedSecret);
            this.aesKey = new SecretKeySpec(aesKeyBytes, 0, 32, "AES");
            return Base64.getEncoder().encodeToString(this.serverKeyPair.getPublic().getEncoded());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String sendFile(String fileName, byte[] fileContent, Current current) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, aesKey);
            byte[] decryptedFile = cipher.doFinal(fileContent);
            Files.write(Paths.get("received_" + fileName), decryptedFile);
            return "File Received";
        } catch (Exception e) {
            return "File Missed";
        }
    }

    @Override
    public String receiveHash(String fileHash, String fileName, Current current) {
        try {
            byte[] receivedFile = Files.readAllBytes(Paths.get("received_" + fileName));
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] serverFileHash = digest.digest(receivedFile);
            String serverFileHashString = Base64.getEncoder().encodeToString(serverFileHash);
            return serverFileHashString.equals(fileHash) ? "Hashes match" : "Hashes do not match";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
