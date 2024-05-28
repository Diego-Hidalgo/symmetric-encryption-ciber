module FileTransfer
{
    sequence<byte> byteSeq;

    interface Transfer {
        string negotiateKey(string clientPublicKey);
        void sendFile(string fileName, byteSeq fileContent);
        string receiveHash(string fileHash);
    };
};
