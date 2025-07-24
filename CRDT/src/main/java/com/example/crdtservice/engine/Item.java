package com.example.codeeditorservice.engine;

import lombok.Builder;
import lombok.Data;
import java.io.Serializable;
import java.util.UUID;

@Data
@Builder
public class Item implements Serializable, Comparable<Item> {
     private static final long serialVersionUID = 1L;

     private String id; // Unique identifier for the item
     private String content; // Actual content of the item
     private String userId; // User who created this item
     private long timestamp; // Timestamp of creation
     private boolean isDeleted; // Flag to mark if item is deleted
     private boolean isBold; // Formatting: bold
     private boolean isItalic; // Formatting: italic
     private String operation; // Operation type (insert, delete, etc.)

     // Positional references
     private transient Item left; // Reference to the item on the left
     private transient Item right; // Reference to the item on the right

     public Item getLeft() {
          return left;
     }

     public Item getRight() {
          return right;
     }

     // Static method to generate a unique ID
     public static String generateId(String userId) {
          return userId + "@" + System.currentTimeMillis() + "@" + UUID.randomUUID();
     }

     public String getOperation() {
          return operation;
     }

     public void setOperation(String operation) {
          this.operation = operation;
     }

     @Override
     public int compareTo(Item other) {
          // Compare based on timestamp and unique identifier
          int timestampComparison = Long.compare(this.timestamp, other.timestamp);
          if (timestampComparison != 0)
               return timestampComparison;
          return this.id.compareTo(other.id);
     }

     // Deep copy method
     public Item copy() {
          return Item.builder()
                    .id(this.id)
                    .content(this.content)
                    .userId(this.userId)
                    .timestamp(this.timestamp)
                    .isDeleted(this.isDeleted)
                    .isBold(this.isBold)
                    .isItalic(this.isItalic)
                    .build();
     }
}