package gitlet;

import java.io.*;
import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Represents a gitlet staging area.
 *  does at a high level.
 *
 * @author Swetha Karthikeyan
 */
public class Staging implements Serializable {

    /**
     * TreeMap of all blobs staged to be added. Contains blob name and id.
     */
    private TreeMap<String, String> addedBlob;
    /**
     * ArrayList of all blobs staged to be removed. Contains blob name.
     */
    private ArrayList<String> removedBlob;


    /**
     * Empty staging constructor.
     */
    public Staging() {
        addedBlob = new TreeMap<>();
        removedBlob = new ArrayList<>();

    }

    /**
     * Reads staging object from file.
     * @return stageFile
     */
    public static Staging readStage() {
        File stageFile = Utils.join(Repository.STAGING_AREA, "stage.txt");
        return Utils.readObject(stageFile, Staging.class);
    }

    /**
     * Adds given blob to addedblob treemap.
     * @param name String
     * @param id String
     */
    public void add(String name, String id) {
        addedBlob.put(name, id);
    }

    /**
     * Adds given blob to removedblob arraylist.
     * @param name String
     */
    public void remove(String name) {
        removedBlob.add(name);
    }

    /**
     * Clears out addedblob and removedblob.
     */
    public void clear() {
        addedBlob = new TreeMap<>();
        removedBlob = new ArrayList<>();
    }

    /**
     * Turns the staging object to file with fileoutput stream
     * and object output stream with stage content, saves to file.
     */
    public void save() {
        File newStage = Utils.join(Repository.STAGING_AREA, "stage.txt");
        try {
            FileOutputStream newS = new FileOutputStream(newStage);
            ObjectOutputStream newSObject = new ObjectOutputStream(newS);
            newSObject.writeObject(this);
            newSObject.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks if addedblob and removedblob are empty.
     * @return if both addedblob and removed blob are clear
     */
    public boolean isClear() {
        return (addedBlob.isEmpty() && removedBlob.isEmpty());
    }

    /**
     * Returns the staging id.
     * @return stage id
     */
    public String getID() {
        return Utils.sha1(Utils.serialize(this));
    }

    /**
     * Returns the addedblob treemap.
     * @return addedblob
     */
    public TreeMap<String, String> getAdded() {
        return addedBlob;
    }

    /**
     * Returns the removedblob arraylist.
     * @return removedblob
     */
    public ArrayList<String> getRemoved() {
        return removedBlob;
    }
}

