import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

package com.example.codeeditorservice.engine;
@Component
public class CRDT{
    private HashMap<String, Item>crdtMap;
    private Item firstItem;
    public Crdt(){
        crdtMap=new HashMap<>();
    }
//    Create an Empty CRDT
    public CRDT(byte[] bytes){
        InitCRDT(bytes);
    }
    //Creates a CRDT from serialized data
//Deserializes byte data back into CRDT structure.
    public void InitCRDT(byte[] bytes){
        List<Item>items=(List<Item>)getDeserializedData(bytes);
        CRDTMap=getCRDTMap(items);
        if(firstItem!=0){
            firstItem=items.get(0);
        }else{
            firstItem=null;
        }
    }
    public Item getItem(String id){
        return CRDTMap.getOrDefault(id, null);
    }
    /// inserting a new character of item into the CRDT without conflicting with existing items
    ///
    public void insert(String key, Item item){
        if(item.getLeft()==null){
            //there is no left pointer
            ///Gets the ID of the first item and the ID of the right item.
        /// Used for ordering checks.
//        if lefe me no item starting to insert first time
            String firstItemId=firstItem==null?null:firstItem.getId();
            String RightItemId=item.getRight()==null?null:item.getRight().getId();
            if(!Objects.equals(firstItemId, RightItemId)&&firstItem.getId().split("@")[1].compareTo(item.getId().split("@")[1])){
                item.setRight(firstItem);
            }else{
                item.setRight(firstItem);
                if(firstItem!=null){
                    firstItem.setLeft(item);
                }
                firstItem=item;
                crdtMap.put(item.getId(), item);
                return;
            }
        }
        //A ➝ B
//        A ➝ Y ➝ B
//        Now Bob inserts X, with:
//        left = A, right = B (same as Y)
//
//        But A.getRight() is now Y, not B — so loop triggers.
        /*
        Checking if there’s already someone inserted between your left and right

Then it looks at the left-of-the-left to compare identities

And based on that, moves the left pointer forward until proper position found


         */
        while(item.getLeft().getRight() !=item.getRight() //A ➝ B
        &&item.getLeft().getLeft().getId().split("@")[1].compareTo(item.getId().split("@")[1]) > 0
        ){
            item.setLeft(item.getLeft().getRight());
        }

        item.setRight(item.getLeft().getRight());
        crdtMap.put(item.getId(), item);
        item.getLeft().setRight(item);
        if (item.getRight() != null)
            item.getRight().setLeft(item);
    }
    public void delete(String key) {
        Item item = crdtMap.get(key);
        item.setIsdeleted(true);
        item.setOperation("delete");
    }
    public void format(String key, boolean bold, boolean italic) {
        Item item = crdtMap.get(key);              // Retrieve item from CRDT map by key
        item.setIsbold(bold);                      // Set bold formatting flag
        item.setIsitalic(italic);                  // Set italic formatting flag
    }
    public String toString() {
        StringBuilder sb = new StringBuilder();    // For constructing output string
        Item current = firstItem;                  // Start from the beginning of the CRDT list
        while (current != null) {
            if (!current.isIsdeleted())            // Skip deleted items
                sb.append(current.getContent());   // Add content to string
            current = current.getRight();          // Move to the next item
        }
        return sb.toString();                      // Return the concatenated string
    }
    public List<Item> getItems() {
        List<Item> items = new ArrayList<>();      // Final item list to return
        Item current = firstItem;                  // Start from the beginning
        while (current != null) {
            items.add(current);                    // Add each item (including deleted)
            current = current.getRight();          // Traverse next
        }
        return items;                              // Return full CRDT list
    }
    private List<Item>getClearData(){
        List<Item>items=new ArrayList<>();
        int cnt=0;
        Item current=firstItem;
        while(current!=null){
            if(!current.isIsdelete()){
                Item left=cnt==0?null:items.get(cnt-1);
                try {
                    Item items=Item.builder()
                            .id(cnt + "@_")
                            .left(left)
                            .isbold(current.isIsbold())
                            .isdeleted(current.isIsdeleted())
                            .isitalic(current.isIsitalic())
                            .content(current.getContent())
                            .operation(current.getOperation())
                            .right(null)
                            .build();
                    if(cnt!=0)items.get(cnt-1).setRight(items);
                    items.add(item);                      // Add to list
                    cnt++;
                } catch (java.lang.Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        current = cur
        return items;
    }

    public byte[] getSerializedData(){
        Object obj=getClearData();
        try(ByteArrayOutputStream box=new ByteArrayOutputStream(); ObjectOutputStream out =new ObjectOutputStream(box)){
            out.writeObject(obj);
            out.flush();
            return box.toByteArray();
        }catch (IOException e) {
            throw new RuntimeException("Failed to serialize object", e); // Handle error
        }
    }private HashMap<String, Item>getCRDTMap(List<Item>items){
        HashMap<String, Item>crdtMap = new HashMap<>();
        for(Item item : items) {
            crdtMap.put(item.getId(), item); // Add each item to the map
        }
    }
    private Object getDeserializedData(byte[] bytes){
        if(bytes.length==0){
            return new ArrayList<Item>(); // Return empty list if no data

        }try(
                ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                ObjectInputStream in = new ObjectInputStream(bis)
                ){
            return in.readObject(); // Deserialize the byte array
        }catch(
                IOException | ClassNotFoundException e
        ){
            throw new RuntimeException("Failed to deserialize object", e); // Handle error
        }
    }

}