module FileTransfer
{
    sequence<byte> byteSeq;

    interface Transfer {
        string negotiateKey(string clientPublicKey);
        string sendFile(string fileName, byteSeq fileContent);
        string receiveHash(string fileHash, string fileName);
    };
};
