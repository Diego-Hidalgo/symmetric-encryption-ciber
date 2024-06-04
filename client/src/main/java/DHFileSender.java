import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class DHFileSender {

    private final FileTransfer.SymmetricKeyFileTransferPrx proxy;
    private SecretKey aesKey;

    /**
     * FileSender constructor
     * @param transferPrx Ice proxy object to allow communication to server.
     */
    public DHFileSender(FileTransfer.SymmetricKeyFileTransferPrx transferPrx) {
        this.proxy = transferPrx;
    }

    /**
     * Negotiates new Symmetric AES-256 Key with server using Diffie-Hellman key-exchange algorithm.
     */
    public void negotiateKey() {
        try {
            System.out.println("Negotiating AES key...");
            //Generate client key pair.
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("DH");
            kpg.initialize(4096);
            KeyPair clientKeyPair = kpg.generateKeyPair();

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
            this.aesKey = new SecretKeySpec(aesKeyBytes, 0, 32, "AES"); //32 bytes = 256 bits
            System.out.println("AES key negotiated");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Encode client public key as a Base64 String
     * @param clientPublicKey Client key
     * @return Base64 String representing encoded key.
     */
    private String encodeClientPublicKey(PublicKey clientPublicKey) {
        return Base64.getEncoder().encodeToString(clientPublicKey.getEncoded());
    }

    /**
     * Decode server public key to PublicKey object
     * @param encodedServerPublicKey Server public key encoded as a Base64 string
     * @return PublicKey object representing server public key
     * @throws NoSuchAlgorithmException If the algorithm for KeyFactory is not valid.
     * @throws InvalidKeySpecException If the given encoded key is invalid.
     */
    private PublicKey decodeServerPublicKey(String encodedServerPublicKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory kf = KeyFactory.getInstance("DH");
        X509EncodedKeySpec pkSpec = new X509EncodedKeySpec(Base64.getDecoder().decode(encodedServerPublicKey));
        return kf.generatePublic(pkSpec);
    }

    /**
     * Encrypts and sends the given file to the server.
     * @param fileName Name of the file to send.
     */
    public void sendFile(String fileName) {
        if(aesKey == null) {
            negotiateKey();
        }
        try {
            //Read file
            System.out.println("Reading file content ...");
            byte[] fileContent = Files.readAllBytes(Paths.get(fileName));

            System.out.println("Encrypting and sending file " + fileName + " ...");
            //Encrypt file
            byte[] encryptedFile = encryptFile(fileContent);

            //Send encrypted file and print status
            boolean fileSentSuccessfully = proxy.sendFile(fileName, encryptedFile);

            if(fileSentSuccessfully)
                System.out.println("File sent successfully");
            else{
                System.out.println("Something went wrong while sending the file. Please try again");
                return;
            }

            //Calculate hash and verify with server
            String fileHashString = calculateHash(fileContent);
            boolean hashesMatch = proxy.verifyHash(fileName, fileHashString);

            if(hashesMatch)
                System.out.println("File hashes successfully verified");
            else
                System.out.println("Something went wrong while verifying the file hashes. Please try again");

        }catch (IOException e){
            System.out.println("The given file does not exist or there was a problem reading its content.");
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Encrypts the given fileContent using AES-256 encryption.
     * @param fileContent The content of the file as a byte[].
     * @return Encrypted file content as byte[]
     * @throws IllegalBlockSizeException cipher.doFinal error.
     * @throws BadPaddingException cipher.doFinal error.
     * @throws InvalidKeyException If the aes key is invalid
     * @throws NoSuchPaddingException  Cipher error
     * @throws NoSuchAlgorithmException If the algorithm for cipher does not exist.
     */
    private byte[] encryptFile(byte[] fileContent) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, UnsupportedEncodingException, InvalidAlgorithmParameterException {
        IvParameterSpec ivParameterSpec = new IvParameterSpec("aaaabbbbccccdddd".getBytes("ASCII"));
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, this.aesKey,ivParameterSpec);
        return cipher.doFinal(fileContent);
    }

    /**
     * Calculates the hash of a given file using SHA-256
     * @param fileContent The content of the file
     * @return String representing the hash of the file.
     * @throws NoSuchAlgorithmException If the algorithm of MessageDigest does not exist.
     */
    private String calculateHash(byte[] fileContent) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] fileHash = digest.digest(fileContent);
        return Base64.getEncoder().encodeToString(fileHash);
    }

}
