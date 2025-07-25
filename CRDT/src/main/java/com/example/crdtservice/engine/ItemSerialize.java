package com.example.codeeditorservice.engine;

import lombok.Builder;
import lombok.Data;
import java.io.Serializable;

@Data
@Builder
public class ItemSerialize implements Serializable {
    private String id;
    private String content;
    private Item right;
    private Item left;
    private String operation;
    private boolean isDeleted;
    private boolean isBold;
    private boolean isItalic;

    public ItemSerialize(String id, String content) {
        this.id = id;
        this.content = content;
        this.right = null;
        this.left = null;
        this.isDeleted = false;
        this.isBold = false;
        this.isItalic = false;
    }

    public ItemSerialize(String id, String content, Item right, Item left, String operation, boolean isDeleted,
            boolean isBold, boolean isItalic) {
        this.id = id;
        this.content = content;
        this.right = right;
        this.left = left;
        this.operation = operation;
        this.isDeleted = isDeleted;
        this.isBold = isBold;
        this.isItalic = isItalic;
    }

    @Override
    public String toString() {
        return "Item{" +
                "id='" + id + '\'' +
                ", content='" + content + '\'' +
                ", right=" + right +
                ", left=" + left +
                ", operation='" + operation + '\'' +
                ", isDeleted=" + isDeleted +
                ", isBold=" + isBold +
                ", isItalic=" + isItalic +
                '}';
    }
}