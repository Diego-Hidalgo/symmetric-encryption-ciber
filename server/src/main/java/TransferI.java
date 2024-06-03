import com.zeroc.Ice.Current;
import javax.crypto.*;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class TransferI implements FileTransfer.Transfer{

    private KeyPair serverKeyPair;
    private SecretKey aesKey;

    public TransferI() {
    }

    @Override
    public String negotiateKey(String encodedClientPublicKey, Current current) {
        try {
            //Decode client Public Key
            PublicKey clientPublicKey = decodeClientPublicKey(encodedClientPublicKey);

            //Create server Key Pair using client Public Key parameters
            DHParameterSpec clientPublicKeyParams = ((DHPublicKey) clientPublicKey).getParams();
            initializeServerKeyPair(clientPublicKeyParams);

            //Obtain shared secret
            KeyAgreement ka = KeyAgreement.getInstance("DH");
            ka.init(this.serverKeyPair.getPrivate());
            ka.doPhase(clientPublicKey, true);
            byte[] sharedSecret = ka.generateSecret();

            //Create AES Key for file decryption using shared secret
            MessageDigest hash = MessageDigest.getInstance("SHA-256");
            byte[] aesKeyBytes = hash.digest(sharedSecret);
            this.aesKey = new SecretKeySpec(aesKeyBytes, 0, 32, "AES");

            //Return encoded server public key to client
            return encodeServerPublicKey(this.serverKeyPair.getPublic());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private PublicKey decodeClientPublicKey(String encodedClientPublicKey) throws InvalidKeySpecException, NoSuchAlgorithmException {
        KeyFactory keyFactory = KeyFactory.getInstance("DH");
        X509EncodedKeySpec pkSpec = new X509EncodedKeySpec(Base64.getDecoder().decode(encodedClientPublicKey));
        return keyFactory.generatePublic(pkSpec);
    }

    private void initializeServerKeyPair(DHParameterSpec clientPublicKeyParams) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("DH");
        kpg.initialize(clientPublicKeyParams);
        this.serverKeyPair = kpg.generateKeyPair();
    }

    private String encodeServerPublicKey(PublicKey serverPublicKey){
        return Base64.getEncoder().encodeToString(serverPublicKey.getEncoded());
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
