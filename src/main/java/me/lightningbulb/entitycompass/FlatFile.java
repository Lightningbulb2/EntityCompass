package me.lightningbulb.entitycompass;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.util.Collection;

public class FlatFile {
    public static <T extends Serializable> boolean save(String filePath, T object) {
        try {
            BukkitObjectOutputStream out = new BukkitObjectOutputStream(new GZIPOutputStream(new FileOutputStream(filePath)));


           // ArrayList<EntityList> entityList = (ArrayList<EntityList>) object;

           // System.out.println(entityList.get(0).getEntityListName());

            out.writeObject(object);
            out.close();

            //System.out.println("[EntityCompass] save successful");

            return true;
        } catch (IOException e) {
            System.out.println("[EntityCompass] save failed");
            //System.out.println(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    public static <T extends Serializable> T load(String filePath){
        try {
            BukkitObjectInputStream in = new BukkitObjectInputStream(new GZIPInputStream(new FileInputStream(filePath)));
            T object = (T) in.readObject();
            in.close();
            System.out.println("[EntityCompass] load successful");
            return object;
        } catch (ClassNotFoundException | IOException e) {
            System.out.println("[EntityCompass] load failed");
            System.out.println(e.getMessage());
            return null;
        }
    }
}