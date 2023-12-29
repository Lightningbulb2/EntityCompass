package me.lightningbulb.entitycompass;

import org.bukkit.command.TabExecutor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

public class EntityList implements Serializable {


    private String name;
    private ArrayList<TrackedEntity> trackedEntityArray = new ArrayList<>();

    //team constructor
    public EntityList(String name) {
        this.name = name;
    }


    //return the name of the team
    public String getEntityListName() {
        return name;
    }

    //add trackedEntity to array list of tracked entities
    public void addTrackedEntity(TrackedEntity trackedEntity) {
        trackedEntityArray.add(trackedEntity);
        trackedEntity.setEntityListMembership(this);
    }


    //remove trackedEntity from Array list of tracked entities
    public void removeTrackedEntity(String uuid) {

        for (TrackedEntity trackedEntity : trackedEntityArray) {
            if (trackedEntity.getUUID().toString().equals(uuid)) {
                trackedEntityArray.remove(trackedEntity);
                trackedEntity = null;
            }
        }

    }
    public void removeTrackedEntity(TrackedEntity trackedEntity) {

        /*
        for (int i = 0; i < trackedEntityArray.size(); i++) {
            if (trackedEntityArray.get(i) == trackedEntity) {
                trackedEntityArray.set(i, null);
                trackedEntityArray.remove(i);
            }
        }*/
        trackedEntityArray.remove(trackedEntity);
        //trackedEntity = null;
    }


    public void setTrackedEntityArray(ArrayList<TrackedEntity> trackedEntityArray) {
        this.trackedEntityArray = trackedEntityArray;
    }

    public ArrayList<TrackedEntity> getTrackedEntityArray() {

        return trackedEntityArray;
    }

    public boolean isMember(TrackedEntity trackedEntity) {
        for (TrackedEntity checkedEntity : trackedEntityArray) {
            if (checkedEntity == trackedEntity) {
                return true;
            }
        }
        return false;
    }

    public void setToNull() {
        for (int i = 0; i < trackedEntityArray.size(); i++) {
            trackedEntityArray.get(i).removeSelf();
            trackedEntityArray.set(i, null);
        }
    }



}
