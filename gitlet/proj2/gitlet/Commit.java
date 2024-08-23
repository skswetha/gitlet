package gitlet;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Represents a gitlet commit object.
 *  does at a high level.
 *
 * @author Swetha Karthikeyan
 */
public class Commit implements Serializable {
    /**
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /**
     * The message of this Commit.
     */
    private final String message;
    /**
     * The time/date of this Commit.
     */
    private final String time;
    /**
     * Hashmap of blobs (name and blob id) in this Commit.
     */
    private final HashMap<String, String> blobmap;
    /**
     * The parent id of this Commit.
     */
    private final ArrayList<String> parents;
    /**
     * The id of this Commit.
     */
    private final String id;


    /**
     * Empty Commit constructor.
     */
    public Commit() {
        blobmap = new HashMap<String, String>();
        parents = new ArrayList<>();
        message = "initial commit";
        time = new SimpleDateFormat("EEE MMM d HH:mm:ss "
                + "yyyy Z").format(new Date(0));
        id = Utils.sha1(Utils.serialize(this));
    }

    /**
     * Commit constructor with instance variables.
     * @param m string
     * @param p string
     * @param bm hashmap
     */
    public Commit(String m, ArrayList<String> p,
                  HashMap<String, String> bm) {
        blobmap = bm;
        parents = p;
        message = m;
        time = new SimpleDateFormat("EEE MMM d HH:mm:ss "
                + "yyyy Z").format(new Date());
        id = Utils.sha1(Utils.serialize(this));

    }


    /**
     * Reads the Commit object from commits file.
     * @param id string
     * @return commitFile
     */
    public static Commit readCommit(String id) {
        File commitFile = Utils.join(Repository.COMMITS, id);
        return Utils.readObject(commitFile, Commit.class);
    }

    /**
     * Returns the id of the commit.
     * @returns id
     */
    public String getID() {
        return id;
    }

    /**
     * Turns the commit object to file with fileoutput stream
     * and object output stream with commit content, saves to file.
     */
    public void save() {
        File newCommit = Utils.join(Repository.COMMITS, id);
        try {
            FileOutputStream newC = new FileOutputStream(newCommit);
            ObjectOutputStream newCObject = new ObjectOutputStream(newC);
            newCObject.writeObject(this);
            newCObject.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the Commit message.
     * @return commit message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the Commit time.
     * @return commit time
     */
    public String getTime() {
        return time;
    }

    /**
     * Returns the Commit blobmap.
     * @return commit blobmap
     */
    public HashMap<String, String> getMap() {
        return blobmap;
    }

    /**
     * Returns the Commit parent id.
     * @return commit parent id
     */
    public ArrayList<String> getParents() {
        return parents;
    }


}

