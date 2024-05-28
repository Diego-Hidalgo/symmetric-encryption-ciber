import com.zeroc.Ice.Current;

public class TransferI implements FileTransfer.Transfer{

    @Override
    public String negotiateKey(String clientPublicKey, Current current) {
        System.out.println(clientPublicKey);
        return "";
    }

    @Override
    public void sendFile(String fileName, byte[] fileContent, Current current) {

    }

    @Override
    public String receiveHash(String fileHash, Current current) {
        return "";
    }

}
