import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class ClientImpl {

    private KeyPair clientKeyPair;
    private SecretKey aesKey;

    private FileTransfer.TransferPrx proxy;

    public ClientImpl(FileTransfer.TransferPrx transferPrx) {
        this.proxy = transferPrx;

        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("DH");;
            kpg.initialize(4096);
            this.clientKeyPair = kpg.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

    }

    public void negotiateKey() {
        try {
            //Encode client PublicKey and send to server. Receive encoded server Public Key
            String encodedClientPublicKey = encodeClientPublicKey(clientKeyPair.getPublic());
            String encodedServerPublicKey = proxy.negotiateKey(encodedClientPublicKey);

            //Decode server Public Key
            PublicKey serverPublicKey = decodeServerPublicKey(encodedServerPublicKey);

            //Generate shared secret
            KeyAgreement ka = KeyAgreement.getInstance("DH");
            ka.init(clientKeyPair.getPrivate());
            ka.doPhase(serverPublicKey, true);
            byte[] sharedSecret = ka.generateSecret();

            //Create AES Key for file encryption using shared secret
            MessageDigest hash = MessageDigest.getInstance("SHA-256");
            byte[] aesKeyBytes = hash.digest(sharedSecret);
            this.aesKey = new SecretKeySpec(aesKeyBytes, 0, aesKeyBytes.length, "AES");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String encodeClientPublicKey(PublicKey encodedClientPublicKey) {
        return Base64.getEncoder().encodeToString(encodedClientPublicKey.getEncoded());
    }

    private PublicKey decodeServerPublicKey(String encodedServerPublicKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory kf = KeyFactory.getInstance("DH");
        X509EncodedKeySpec pkSpec = new X509EncodedKeySpec(Base64.getDecoder().decode(encodedServerPublicKey));
        return kf.generatePublic(pkSpec);
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
