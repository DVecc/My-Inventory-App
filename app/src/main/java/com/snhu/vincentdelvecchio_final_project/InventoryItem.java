package com.snhu.vincentdelvecchio_final_project;

// Model for inventory Item
public class InventoryItem {

    private String mItemName;
    private int mItemQTY;
    private int mItemId;

    public InventoryItem() {}

    public  InventoryItem(String name, int QTY) {
        mItemName = name;
        mItemQTY =  QTY;
    }

    public String getmItemName() {
        return mItemName;
    }

    public void setmItemName(String mItemName) {
        this.mItemName = mItemName;
    }

    public int getmItemQTY() {
        return mItemQTY;
    }

    public void setmItemQTY(int mItemQTY) {
        this.mItemQTY = mItemQTY;
    }

    public int getmItemId() {
        return mItemId;
    }

    public void setmItemId(int mItemId) {
        this.mItemId = mItemId;
    }
}
