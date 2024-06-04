import com.zeroc.Ice.Current;
import javax.crypto.*;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class DHFileTransfer implements FileTransfer.SymmetricKeyFileTransfer{

    private KeyPair serverKeyPair;
    private SecretKey aesKey;

    /**
     * DHFileTransfer constructor
     */
    public DHFileTransfer() {
    }

    /**
     * Returns the server public key created with parameter of the client public key.
     * Creates an AES-256 decryption key using the server private key and SHA-256 hash.
     * @param encodedClientPublicKey Client public key encoded as a Base64 string
     * @param current com.zeroc.Ice.Current
     * @return Server public key encoded as a Base64 string
     */
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
            this.aesKey = new SecretKeySpec(aesKeyBytes, 0, 32, "AES"); //32 bytes 256 bits

            //Return encoded server public key to client
            return encodeServerPublicKey(this.serverKeyPair.getPublic());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Decodes the given encoded client public key.
     * @param encodedClientPublicKey Client public key encoded as a Base64 String.
     * @return PublicKey object representing the given encoded client public key
     * @throws InvalidKeySpecException  If the given key is invalid for DH.
     * @throws NoSuchAlgorithmException If the KeyFactory DH algorithm does not exists.
     */
    private PublicKey decodeClientPublicKey(String encodedClientPublicKey) throws InvalidKeySpecException, NoSuchAlgorithmException {
        KeyFactory keyFactory = KeyFactory.getInstance("DH");
        X509EncodedKeySpec pkSpec = new X509EncodedKeySpec(Base64.getDecoder().decode(encodedClientPublicKey));
        return keyFactory.generatePublic(pkSpec);
    }

    /**
     * Initializes the server key pair given the client public key parameters.
     * @param clientPublicKeyParams Client public key parameters.
     * @throws NoSuchAlgorithmException If the DH algorithm does not exist for KeyPairGenerator.
     * @throws InvalidAlgorithmParameterException If the given parameters are not appropriate for DH.
     */
    private void initializeServerKeyPair(DHParameterSpec clientPublicKeyParams) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("DH");
        kpg.initialize(clientPublicKeyParams);
        this.serverKeyPair = kpg.generateKeyPair();
    }

    /**
     * Encodes the server PublicKey as a Base64 string
     * @param serverPublicKey Server public key as PublicKey object.
     * @return server public key encoded as a Base64 string.
     */
    private String encodeServerPublicKey(PublicKey serverPublicKey){
        return Base64.getEncoder().encodeToString(serverPublicKey.getEncoded());
    }

    /**
     * Receives the file from the client.
     * @param fileName The name of the file.
     * @param fileContent The file content as byte[].
     * @param current com.zeroc.Ice.Current
     * @return True if the file was received and saved successfully. False if an exception was thrown.
     */
    @Override
    public boolean sendFile(String fileName, byte[] fileContent, Current current) {

        try {
            IvParameterSpec ivParameterSpec = new IvParameterSpec("aaaabbbbccccdddd".getBytes("ASCII"));
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, aesKey,ivParameterSpec);
            byte[] decryptedFile = cipher.doFinal(fileContent);

            Files.write(Paths.get("received_" + fileName), decryptedFile);

            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
            //return false;
        }
    }

    /**
     * Verifies the hash of a file after it was sent.
     * @param fileName The name of the file.
     * @param fileHash The hash of the file (client)
     * @param current com.zeroc.Ice.Current
     * @return True if the hash provided by the client is equal to the one calculated from the server. Else, false.
     */
    @Override
    public boolean verifyHash(String fileName, String fileHash, Current current) {
        try {
            byte[] receivedFile = Files.readAllBytes(Paths.get("received_" + fileName));
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] serverFileHash = digest.digest(receivedFile);

            String serverFileHashString = Base64.getEncoder().encodeToString(serverFileHash);

            return serverFileHashString.equals(fileHash);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
