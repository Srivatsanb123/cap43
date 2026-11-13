package com.cap43.storage;

import java.io.IOException;
import java.io.InputStream;

public interface EvidenceStorage {
    void put(String key, byte[] data, String contentType);

    
    InputStream get(String key) throws IOException;
}
