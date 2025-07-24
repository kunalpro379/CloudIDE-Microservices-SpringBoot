package com.example.codeeditorservice.engine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.example.codeeditorservice.entities.Doc;
import com.example.codeeditorservice.repository.DocRepository;
import com.example.codeeditorservice.engine.CRDT;

@Service
public class CRDTManagerService {
    @Autowired
    private DocRepository docRepository;
    private ConcurrentHashMap<Long, CRDT> CRDTMap = new ConcurrentHashMap<>();

    public CRDT getCrdt(Long docId) {
        CRDT crdt = CRDTMap.getOrDefault(docId, null);
        if (crdt == null)
            return null;
        synchronized (crdt) {
            return crdt;
        }
    }

    public void createCrdt(Long docId) {
        if (CRDTMap.containsKey(docId))
            return; // check whether crdt already exists for the docId Document
        CRDT crdt = new CRDT();
        CRDTMap.put(docId, crdt);
        synchronized (crdt) {
            byte[] crdtContent = docRepository.getDocById(docId).get().getContent();
            crdt.InitCRDT(crdtContent);
            CRDTMap.put(docId, crdt);
        }
    }

    private void deleteCrdt(Long docId) {
        CRDTMap.remove(docId);
    }

    // save and delete crdt objects
    public synchronized void saveAndDeleteCrdt(Long docId) {
        if (!CRDTMap.containsKey(docId)) {
            return; // No CRDT exists for this document
        }
        // Makes the whole method synchronized so only one thread can save and delete at
        // a time.
        docRepository.findById(docId).ifPresent(doc -> {
            // Fetches the Doc from DB.
            doc.setContent(CRDTMap.get(docId).getSerializedCrdt());
            // Updates its content by serializing the CRDT (getSerializedData()).
            docRepository.save(doc);
        });
        deleteCrdt(docId);
    }
}