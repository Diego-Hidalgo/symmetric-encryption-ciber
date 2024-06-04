module FileTransfer
{
    sequence<byte> byteSeq;

    interface SymmetricKeyFileTransfer {
        string negotiateKey(string encodedClientPublicKey);
        bool sendFile(string fileName, byteSeq fileContent);
        bool verifyHash(string fileName, string fileHash );
    };
};
