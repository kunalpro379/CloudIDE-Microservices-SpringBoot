import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
package com.example.codeeditorservice.engine;
import java.util.concurrent.ConcurrentMap;
import com.example.codeeditorservice.entities.Doc;
import com.example.codeeditorservice.repository.DocRepository;

@Service
public class CRDTManagerService{
    @Autowired
    private DocRepository docRepository;
    private ConcurrentHashMap<Long, Crdt>CRDTMap;
    CRDTMap=new ConcurrentHashMap<>();
    public Crdt getCrdt(Long docId){
        Crdt crdt=CRDTMap.getOrDefault(docId, null);
        if(crdt==null)return null;
        synchronized (crdt){
            return crdt;
        }
    }
    public void createCrdt(Long docId){
        if(crdtMap.containsKey(docId))return;//check weather crdt already exists for the docId Document
//        not then create
        Crdt crdt=new Crdt();
        CRDTMap.put(docId, crdt);
        synchronized (crdt){
            byte[] crdtContent=docRepository.getDocById().get().getContent();
            crdt.initCrdt(crdtContent);
            CRDTMap.put(docId, crdt);
        }
    }
    private void deleteCrdt(Long docId){
        crdtMap.remove(docId);
    }
    //save and delete crdt objects
    public synchronized void saveAndDeleteCrdt(Long docId){
        if(!crdtMap.containsKey(docId)) {
            return; // No CRDT exists for this document
        }//Makes the whole method synchronized so only one thread can save and delete at a time.
        docRepository.findById(docId).ifPresent(
                doc->{//Fetches the Doc from DB.
                    doc.setContent(
                            CRDTMap.get(docId).getSerializedCrdt()
                    );//Updates its content by serializing the CRDT (getSerializedCrdt()).

                    docRepository.save(doc);
                };
        );
        deleteCrdt(docId);

    }

}