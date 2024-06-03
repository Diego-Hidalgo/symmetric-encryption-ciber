module FileTransfer
{
    sequence<byte> byteSeq;

    interface Transfer {
        string negotiateKey(string encodedClientPublicKey);
        string sendFile(string fileName, byteSeq fileContent);
        string receiveHash(string fileHash, string fileName);
    };
};
