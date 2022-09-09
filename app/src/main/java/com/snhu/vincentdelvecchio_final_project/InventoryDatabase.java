package com.snhu.vincentdelvecchio_final_project;

        import android.content.ContentValues;
        import android.content.Context;
        import android.database.Cursor;
        import android.database.sqlite.SQLiteDatabase;
        import android.database.sqlite.SQLiteOpenHelper;
        import java.util.ArrayList;
        import java.util.List;


public class InventoryDatabase extends SQLiteOpenHelper {

    // Final variables for database information
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "inventory.db";

    // Static instance of Database for Singleton usage
    private static InventoryDatabase mUserDb;

    // Create initial database if it doesn't exist and return static instance for singleton
    public static InventoryDatabase getInstance(Context context) {
        if(mUserDb == null) {
            mUserDb = new InventoryDatabase(context);
        }
        return  mUserDb;
    }

    // Default Constructor
    public InventoryDatabase(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    // Variables for table name and column names
    private static final String TABLE = "inventoryItems";
    private static final String COL_ID = "_id";
    private static final String COL_USERNAME = "userName";
    private static final String COL_ITEM_NAME = "itemName";
    private static final String COL_QTY = "qty";

    @Override
    // Create database using variables holding table and column names
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create table " + InventoryDatabase.TABLE + " (" +
                InventoryDatabase.COL_ID + " integer primary key autoincrement, " +
                InventoryDatabase.COL_USERNAME + " text, " +
                InventoryDatabase.COL_ITEM_NAME + " text, " +
                InventoryDatabase.COL_QTY + " integer)");
    }

    @Override
    // Drop table on database upgrade
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("drop table if exists " + InventoryDatabase.TABLE);
        onCreate(sqLiteDatabase);
    }

    // Add item to database for given username
    public boolean addItem(String username, InventoryItem item) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(InventoryDatabase.COL_USERNAME, username);
        values.put(InventoryDatabase.COL_ITEM_NAME, item.getmItemName());
        values.put(InventoryDatabase.COL_QTY, item.getmItemQTY());

        long result = db.insert(InventoryDatabase.TABLE, null, values);
        return result != -1;
    }

    // Update item in database with given ID
    public void updateItem(InventoryItem item) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(InventoryDatabase.COL_ITEM_NAME, item.getmItemName());
        values.put(InventoryDatabase.COL_QTY, item.getmItemQTY());

        db.update(InventoryDatabase.TABLE, values,
                InventoryDatabase.COL_ID + " = ?", new String[] { String.valueOf(item.getmItemId()) });
    }

    // Return list of all items in database belonging to given username
    public List<InventoryItem> getItems (String username) {
        List<InventoryItem> items = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String sql = "select * from " + InventoryDatabase.TABLE + " where " + InventoryDatabase.COL_USERNAME + " = ?";
        Cursor cursor = db.rawQuery(sql, new String[] {username});

        if(cursor.moveToFirst()) {
            do {
                InventoryItem item = new InventoryItem();
                item.setmItemId(cursor.getInt(0));
                item.setmItemName(cursor.getString(2));
                item.setmItemQTY(cursor.getInt(3));
                items.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return items;
    }

    // retrieve item from database with given itemId
    public InventoryItem getItem (Integer itemId) {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "select * from " + InventoryDatabase.TABLE + " where " + InventoryDatabase.COL_ID + " = ?";
        Cursor cursor = db.rawQuery(sql, new String[] {itemId.toString()});

        if(cursor.moveToFirst()) {
                InventoryItem item = new InventoryItem();
                item.setmItemId(cursor.getInt(0));
                item.setmItemName(cursor.getString(2));
                item.setmItemQTY(cursor.getInt(3));
                cursor.close();
            return item;
        }
        return null;
    }

    // Delete item with given ID from the database
    public void deleteItem(Integer itemId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(InventoryDatabase.TABLE, InventoryDatabase.COL_ID + " = ?", new String[] {itemId.toString()});
    }

}

