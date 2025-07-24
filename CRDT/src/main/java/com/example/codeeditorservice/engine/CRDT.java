package com.example.codeeditorservice.engine;

import org.springframework.stereotype.Component;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;

@Component
public class CRDT implements Serializable {
    private static final long serialVersionUID = 1L;

    // Use concurrent data structures for thread-safety
    private final ConcurrentSkipListMap<String, Item> crdtMap;
    private final String documentId;
    private transient Item firstItem;

    public CRDT() {
        this.documentId = UUID.randomUUID().toString();
        this.crdtMap = new ConcurrentSkipListMap<>();
    }

    public CRDT(String documentId) {
        this.documentId = documentId;
        this.crdtMap = new ConcurrentSkipListMap<>();
    }

    // Initialize CRDT from bytes
    public void InitCRDT(byte[] bytes) {
        List<Item> items = (List<Item>) getDeserializedCrdt(bytes);
        ConcurrentSkipListMap<String, Item> newMap = new ConcurrentSkipListMap<>(getCrdtMap(items));
        crdtMap.clear();
        crdtMap.putAll(newMap);
        if (!items.isEmpty()) {
            firstItem = items.get(0);
        } else {
            firstItem = null;
        }
    }

    public Item getItem(String id) {
        return crdtMap.getOrDefault(id, null);
    }

    // Synchronized insert method to handle concurrent edits
    public synchronized void insert(String userId, String content, int position, boolean isBold, boolean isItalic) {
        String newItemId = Item.generateId(userId);
        Item newItem = Item.builder()
                .id(newItemId)
                .content(content)
                .userId(userId)
                .timestamp(System.currentTimeMillis())
                .isBold(isBold)
                .isItalic(isItalic)
                .build();
        if (crdtMap.isEmpty()) {
            crdtMap.put(newItemId, newItem);
            firstItem = newItem;
            return;
        }
        List<Item> sortedItems = new ArrayList<>(crdtMap.values());
        sortedItems.sort(Comparator.comparing(Item::getTimestamp));
        if (position >= sortedItems.size()) {
            Item lastItem = sortedItems.get(sortedItems.size() - 1);
            newItem.setLeft(lastItem);
            lastItem.setRight(newItem);
        } else {
            Item existingItem = sortedItems.get(position);
            if (existingItem.getLeft() != null) {
                newItem.setLeft(existingItem.getLeft());
                existingItem.getLeft().setRight(newItem);
            }
            newItem.setRight(existingItem);
            existingItem.setLeft(newItem);
        }
        crdtMap.put(newItemId, newItem);
    }

    // Delete an item by key
    public void delete(String key) {
        Item item = crdtMap.get(key);
        if (item != null) {
            item.setDeleted(true);
            item.setOperation("delete");
        }
    }

    // Format an item by key
    public void format(String key, boolean bold, boolean italic) {
        Item item = crdtMap.get(key);
        if (item != null) {
            item.setBold(bold);
            item.setItalic(italic);
        }
    }

    // Get the full text content
    public String getText() {
        return crdtMap.values().stream()
                .filter(item -> !item.isDeleted())
                .map(Item::getContent)
                .collect(Collectors.joining());
    }

    // Get all items (for synchronization)
    public List<Item> getAllItems() {
        return new ArrayList<>(crdtMap.values());
    }

    // Merge another CRDT document
    public synchronized void merge(CRDT otherCRDT) {
        for (Item item : otherCRDT.getAllItems()) {
            if (!crdtMap.containsKey(item.getId())) {
                crdtMap.put(item.getId(), item);
            }
        }
    }

    // Serialize the CRDT
    public byte[] serialize() throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(this);
            return bos.toByteArray();
        }
    }

    // Deserialize the CRDT
    public static CRDT deserialize(byte[] data) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
                ObjectInputStream ois = new ObjectInputStream(bis)) {
            return (CRDT) ois.readObject();
        }
    }

    // Getters
    public String getDocumentId() {
        return documentId;
    }

    // Custom serialization for CRDT content (for DB storage)
    public byte[] getSerializedCrdt() {
        Object obj = getClearData();
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream out = new ObjectOutputStream(bos)) {
            out.writeObject(obj);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize object", e);
        }
    }

    // Helper: build a map from a list of items
    private Map<String, Item> getCrdtMap(List<Item> items) {
        Map<String, Item> crdtMap = new HashMap<>();
        for (Item item : items) {
            crdtMap.put(item.getId(), item);
        }
        return crdtMap;
    }

    // Helper: deserialize a list of items from bytes
    private Object getDeserializedCrdt(byte[] bytes) {
        if (bytes.length == 0) {
            return new ArrayList<>();
        }
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                ObjectInputStream in = new ObjectInputStream(bis)) {
            return in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to deserialize object", e);
        }
    }

    // Helper: get a clean list of items (for serialization)
    private List<Item> getClearData() {
        List<Item> items = new ArrayList<>();
        int cnt = 0;
        Item current = firstItem;
        while (current != null) {
            if (!current.isDeleted()) {
                Item left = cnt == 0 ? null : items.get(cnt - 1);
                Item item = Item.builder()
                        .id(cnt + "@_")
                        .left(left)
                        .isBold(current.isBold())
                        .isDeleted(current.isDeleted())
                        .isItalic(current.isItalic())
                        .content(current.getContent())
                        .operation(current.getOperation())
                        .right(null)
                        .build();
                if (cnt != 0)
                    items.get(cnt - 1).setRight(item);
                items.add(item);
                cnt++;
            }
            current = current.getRight();
        }
        return items;
    }
}