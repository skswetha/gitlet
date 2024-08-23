package gitlet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import static gitlet.Utils.join;



/**
 * Represents a gitlet repository.
 *  does at a high level.
 *
 * @author Swetha Karthikeyan
 */
public class Repository {
    /**
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /**
     * The current working directory.
     */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /**
     * The .gitlet directory.
     */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /**
     * Stage directory has stage.txt.
     */
    public static final File STAGING_AREA = Utils.join(GITLET_DIR,
            "staging_area");
    /**
     * Commits directory has commits.txt.
     */
    public static final File COMMITS = Utils.join(GITLET_DIR,
            "commits");
    /**
     * Blobs directory has blobs.txt.
     */
    public static final File BLOBS = Utils.join(GITLET_DIR,
            "blobs");
    /**
     * Branches directory has branches and head.txt.
     */
    public static final File BRANCHES = Utils.join(GITLET_DIR, "branches");
    /**
     * HEAD.txt has the main branch name.
     */
    public static final File HEAD = Utils.join(BRANCHES, "HEAD.txt");

    /**
     Has a stage.
     */
    private Staging stage;


    /**
     * Constructor for Repository.
     **/
    public Repository() {
        if (new File(CWD.getPath()
                + "/.gitlet/staging_area/stage.txt").exists()) {
            stage = Utils.readObject((new File(CWD.getPath()
                    + "/.gitlet/staging_area/stage.txt")), Staging.class);
        }
    }


    /**
     * Creates a new Gitlet version-control system in the current directory.
     * This system will automatically start with one commit: a commit that
     * contains no files and has the commit message initial commit
     * (just like that, with no punctuation). It will have a single branch:
     * main, which initially points to this initial commit, and main will
     * be the current branch. The timestamp for this initial commit will
     * be 00:00:00 UTC, Thursday, 1 January 1970 in whatever format you
     * choose for dates (this is called “The (Unix) Epoch”, represented
     * internally by the time 0.) Since the initialcommit in all
     * repositories created by Gitlet will have exactly the same content,
     * it follows that all repositories will automatically share this
     * commit (they will all have the same UID) and all commits in all
     * repositories will trace back to it.
     * @param args string[]
     */
    public void init(String[] args) {
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists "
                    + "in the current directory.");
            return;
        }
        GITLET_DIR.mkdir();
        STAGING_AREA.mkdir();
        COMMITS.mkdir();
        BLOBS.mkdir();
        BRANCHES.mkdir();

        try {
            /* write HEAD.txt with branch name "main" contents */
            Files.write(Paths.get(CWD.getPath()
                    + "/.gitlet/branches/HEAD.txt"), ("main").getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        Commit firstCommit = new Commit();
        /* save commit */
        firstCommit.save();
        /* save branch main with commit id contents*/
        try {
            Files.write(Paths.get(CWD.getPath()
                            + "/.gitlet/branches/main"),
                    (firstCommit.getID()).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        stage = new Staging();
        /*save stage*/
        stage.save();
    }


    /**
     *  Adds a copy of the file as it currently exists to the staging area
     *  (see the description of the commit command). For this reason, adding
     *  a file is also called staging the file for addition. Staging an
     *  already-staged file overwrites the previous entry in the staging area
     *  with the new contents. The staging area should be somewhere in .gitlet.
     *  If the current working version of the file is identical to the version
     *  in the current commit, do not stage it to be added, and remove it from
     *  the staging area if it is already there (as can happen when a file is
     *  changed, added, and then changed back to it’s original version). The
     *  file will no longer be staged for removal (see gitlet rm), if it was
     *  at the time of the command.
     * @param args string[]
     */
    public void add(String[] args) {
        String fileName = args[1];
        File addFile = Utils.join(CWD, fileName);
        if (!addFile.exists()) {
            System.out.println("File does not exist.");
            return;
        }

        /* get id of blob we want to add*/
        String blobid = Utils.sha1(Utils.readContentsAsString(addFile));
        boolean commitContains = false;

        /* get the current commit using commit id and read from commits*/
        Commit curr = Commit.readCommit(getCurrentCommit());
        /* if commit blobmap already has the file we want to add*/
        if (curr.getMap().containsKey(fileName)) {
            /* if current file is identical to version in curr commit*/
            if (curr.getMap().get(fileName).equals(blobid)) {
                commitContains = true;
                /*dont stage to be added and remove from staging area if there*/
                stage.getAdded().remove(fileName);
                /* if the file is in the removed stage, get rid of it*/
                stage.getRemoved().remove(fileName);
            }
            /* if not identical, replace it in the commit*/
        }
        /* if stage has it, replace it*/
        if (!commitContains) {
            if (stage.getAdded().containsKey(fileName)) {
                stage.getAdded().replace(fileName, blobid);
            } else {
                /* if not already in commit or stage, add to stage*/
                stage.add(fileName, blobid);
            }
        }

        /* make sure it is no longer staged for removal*/
        stage.getRemoved().remove(fileName);

        /* save stage*/
        stage.save();

        /*save blob as blobid.txt*/
        File newBlob = new File((CWD.getPath()
                + "/.gitlet/blobs/" + blobid + ".txt"));
        /* add the contents of blob in saved blob*/
        Utils.writeContents(newBlob, Utils.readContents(addFile));
    }

    /**
     * Saves a snapshot of tracked files in the current commit and staging
     * area so they can be restored at a later time, creating a new commit.
     * The commit is said to be tracking the saved files. By default, each
     * commit’s snapshot of files will be exactly the same as its parent commits
     * snapshot of files; it will keep versions of files exactly as they are,
     * and not update them. A commit will only update the contents of files
     * it is tracking that have been staged for addition at the time of
     * commit, in which case the commit will now include the version of the
     * file that was staged instead of the version it got from its parent.
     * A commit will save and start tracking any files that were staged for
     * addition but weren’t tracked by its parent. Finally, files tracked in
     * the current commit may be untracked in the new commit as a result being
     * staged for removal by the rm command (below).
     * @param args string[]
     */
    public void commit(String[] args) {
        if (args[1].equals("")) {
            System.out.println("Please enter a commit message.");
            return;
        }

        String message = args[1];
        if (stage.isClear()) {
            System.out.println("No changes added to the commit.");
            return;
        }
        String commitID = getCurrentCommit();
        /*get the current commit using commit id and read from commits*/
        Commit curr = Commit.readCommit(commitID);

        /* get all the files in the current commit*/
        HashMap<String, String> currentBlobs = curr.getMap();
        /* add what needs to be added*/
        currentBlobs.putAll(stage.getAdded());
        /* get rid of blobs that need to be removed*/
        /* going through get removed*/
        for (String name : stage.getRemoved()) {
            currentBlobs.remove(name);
        }
        ArrayList<String> parent = new ArrayList<String>();
        parent.add(commitID);
        if (message.startsWith("Merged")) {
            int intoIndex = message.indexOf(" into ");
            String branchname = message.substring(7, intoIndex);
            String branchcomid = Utils.readContentsAsString(
                    new File(CWD.getPath()
                    + "/.gitlet/branches/" + branchname));
            parent.add(branchcomid);
        }
        /* make new commit with added blobs*/
        Commit newCommit = new Commit(message, parent, currentBlobs);

        /* clear stage after commit*/
        stage.clear();

        /*save everything*/
        stage.save();
        newCommit.save();

        /* add new commit to branch*/
        try {
            Files.write(Paths.get((CWD.getPath() + "/.gitlet/branches/"
                    + (getCurrBranchName()))), (newCommit.getID()).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Takes the version of the file as it exists
     * in the head commit and puts it in the working
     * directory, overwriting the version of the file
     * that’s already there if there is one. The new
     * version of the file is not staged.File does not exist in that commit
     * @param args string[]
     */
    public void checkoutFileName(String[] args) {
        String name = args[2];
        Commit curr = Commit.readCommit(getCurrentCommit());
        /* If the file does not exist in the commit, abort,
        printing the error message*/
        if (!curr.getMap().containsKey(name)) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        /* if the file is already there in CWD, delete it and write a new one
           (dont make new file with that path if it doesnt exist bc error)*/
        if ((new File(CWD.getPath() + name)).exists()) {
            (new File(CWD.getPath() + name)).delete();
        }
        /* get the blob file from current commit blobmap*/
        File newBlob = new File(CWD.getPath()
                + "/.gitlet/blobs/" + curr.getMap().get(name) + ".txt");
        /*make a new file and write the zlob contents into it*/
        File newFile = new File(CWD.getPath(), name);
        Utils.writeContents(newFile, Utils.readContents(newBlob));
    }

    /**
     * Takes all files in the commit at the head of the given branch,
     * and puts them in the working directory, overwriting the versions
     * of the files that are already there if they exist. Also, at the
     * end of this command, the given branch will now be considered the
     * current branch (HEAD). Any files that are tracked in the current branch
     * but are not present in the checked-out branch are deleted. The staging
     * area is cleared, unless the checked-out branch is the current branch
     * @param args string[]
     */
    public void checkoutBranch(String[] args) {
        String branchname = args[1];
        /*if that branch name doesnt exists*/
        if (!(new File(CWD.getPath()
                + "/.gitlet/branches/" + branchname)).exists()) {
            System.out.println("No such branch exists.");
            return;
        }
        /* if branch name is current branch*/
        if (branchname.equals(getCurrBranchName())) {
            System.out.println("No need to checkout the current branch.");
            return;
        }
        /* commit at given branch*/
        String branchcomid = Utils.readContentsAsString(new File(CWD.getPath()
                + "/.gitlet/branches/" + branchname));
        Commit branchcom = Commit.readCommit(branchcomid);
        /* current commit*/
        Commit curr = Commit.readCommit(getCurrentCommit());
        /* check if any untracked files*/
        List<String> directoryblob = Utils.plainFilenamesIn(CWD.getPath());
        for (String dirblob : directoryblob) {
            if (!curr.getMap().containsKey(dirblob)) {
                if (branchcom.getMap().containsKey(dirblob)) {
                    System.out.println("There is an untracked file in the way; "
                            + "delete it, or add and commit it first.");
                    return;
                }
            }
        }

        /*remove any files that are not in the branch in directory*/
        for (String dirblob : directoryblob) {
            if (!branchcom.getMap().containsKey(dirblob)) {
                File todelete = new File(CWD.getPath() + "/" + dirblob);
                todelete.delete();
            }
        }

        /*all files in commit at branch*/
        HashMap<String, String> blobstoadd = branchcom.getMap();

        for (String blobid : blobstoadd.keySet()) {
            /* if the file is already there in CWD,
            delete it and write a new one*/
            if ((new File(CWD.getPath() + "/" + blobid)).exists()) {
                (new File(CWD.getPath() + "/" + blobid)).delete();
            }
            /* get the blob file from commit blobmap*/
            File newBlob = new File(CWD.getPath()
                    + "/.gitlet/blobs/" + branchcom.getMap().get(blobid)
                    + ".txt");
            /* make a new file and write the blob contents into it*/
            File newFile = new File(CWD.getPath(), blobid);
            Utils.writeContents(newFile, Utils.readContents(newBlob));
        }

        /*all files in current commit*/
        HashMap<String, String> currentblobs = curr.getMap();
        for (String blobid : currentblobs.keySet()) {
            /*if checked out branch doesnt have the file*/
            if (!blobstoadd.containsKey(blobid)) {
                /* remove it*/
                File deletefile = new File(CWD.getPath()
                        + "/.gitlet/blobs/" + blobid);
                deletefile.delete();
            }
        }

        /* clear stage*/
        stage.clear();

        /* at end of command, make this branch the head*/
        try {
            Files.write((Paths.get(CWD.getPath()
                    + "/.gitlet/branches/HEAD.txt")), branchname.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Takes the version of the file as it exists in the
     * commit with the given id, and puts it in the working
     * directory, overwriting the version of the file that’s
     * already there if there is one. The new version of the
     * file is not staged.
     * @param args string[]
     */
    public void checkoutCommitFile(String[] args) {
        String commitid = args[1];
        String name = args[3];
        boolean hasid = false;

        /*search through list of commit ids*/
        List<String> listofcommits = Utils.plainFilenamesIn(CWD.getPath()
                + "/.gitlet/commits");
        for (String id : listofcommits) {
            if (id.contains(commitid)) {
                hasid = true;
                commitid = id;
            }
        }
        /*if commit doesnt exist*/
        if (!hasid) {
            System.out.println("No commit with that id exists.");
            return;
        }
        Commit checkCom = Commit.readCommit(commitid);
        /*if file does not exist in that commit*/
        if (!checkCom.getMap().containsKey(name)) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        /* if the file is already there in CWD, delete it and write a new one
        (dont make new file with that path if it doesnt exist bc error)*/
        if ((new File(CWD.getPath() + name)).exists()) {
            (new File(CWD.getPath() + name)).delete();
        }
        /* get the blob file from commit blobmap*/
        File newBlob = new File(CWD.getPath()
                + "/.gitlet/blobs/" + checkCom.getMap().get(name) + ".txt");
        /*make a new file and write the blob contents into it*/
        File newFile = new File(CWD.getPath(), name);
        Utils.writeContents(newFile, Utils.readContents(newBlob));
    }
    /**
     * Checkout is a kind of general command that can do a few
     * different things depending on what its arguments are.
     * There are 3 possible use cases. Inside the method,
     * there are descriptions of each case/
     * @param args string[]
     */
    public void checkout(String[] args) {
        /*java gitlet.Main checkout -- [file name]*/
        if (args.length == 3) {
            if (args[1].equals("--")) {
                checkoutFileName(args);
            } else {
                System.out.println("Incorrect operands.");
                return;
            }
        }
        /*java gitlet.Main checkout [commit id] -- [file name]*/
        if (args.length == 4) {
            if (args[2].equals("--")) {
                checkoutCommitFile(args);
            } else {
                System.out.println("Incorrect operands.");
                return;
            }
            /*java gitlet.Main checkout [branch name]*/
        } else if (args.length == 2) {
            checkoutBranch(args);
        }


    }

    /**
     Starting at the current head commit,
     display information about each commit backwards
     along the commit tree until the initial commit,
     following the first parent commit links, ignoring
     any second parents found in merge commits. This set
     of commit nodes is called the commit’s history. For
     every node in this history, the information it should
     display is the commit id, the time the commit was made,
     and the commit message.
     */
    public void log() {
        /* current commit*/
        Commit curr = Commit.readCommit(getCurrentCommit());
        while (curr != null) {
            System.out.println("===");
            System.out.println("commit " + curr.getID());
            if (curr.getMessage().contains("Merged")) {
                System.out.println("Merge: "
                        + curr.getParents().get(0).substring(0, 7)
                        + " " + curr.getParents().get(0).substring(0, 7));
            }
            System.out.println("Date: " + curr.getTime());
            System.out.println(curr.getMessage());
            System.out.println();

            /* as long as curr has parents*/
            if (!curr.getParents().isEmpty()) {
                /* make curr the parent*/
                String parentid = curr.getParents().get(0);
                curr = Commit.readCommit(parentid);
            } else {
                /* else exit*/
                return;
            }
        }
    }

    /**
     Unstage the file if it is currently staged for
     addition. If the file is tracked in the current
     commit, stage it for removal and remove the file
     from the working directory if the user has not
     already done so (do not remove it unless it is
     tracked in the current commit).
     @param args string[]
     */
    public void rm(String[] args) {
        String fileName = args[1];
        boolean isstagedortracked = false;

        /*get the current commit using commit id and read from commits*/
        Commit curr = Commit.readCommit(getCurrentCommit());
        /*if commit blobmap is already tracking file*/
        if (curr.getMap().containsKey(fileName)) {
            isstagedortracked = true;
            /*stage for removal*/
            stage.remove(fileName);
            /*remove it from working dir*/
            File removefile = new File(CWD.getPath(), fileName);
            if (removefile.exists()) {
                Utils.restrictedDelete(fileName);
            }
        }

        /* if currently staged for addition*/
        if (stage.getAdded().containsKey(fileName)) {
            isstagedortracked = true;
            stage.getAdded().remove(fileName);
        }

        if (!isstagedortracked) {
            System.out.println("No reason to remove the file.");
            return;
        }

        /*save stage*/
        stage.save();
    }

    /**
     * Like log, except displays information about all
     * commits ever made. The order of the commits does
     * not matter. Hint: there is a useful method in
     * gitlet.Utils that will help you iterate over files
     *  within a directory.
     */
    public void globallog() {
        List<String> allcommits = Utils.plainFilenamesIn(CWD.getPath()
                + "/.gitlet/commits");
        for (String commitid : allcommits) {
            Commit curr = Commit.readCommit(commitid);
            System.out.println("===");
            System.out.println("commit " + curr.getID());
            System.out.println("Date: " + curr.getTime());
            System.out.println(curr.getMessage());
            System.out.println();
        }
    }

    /**
     * Prints out the ids of all commits that have the given
     * commit message, one per line. If there are multiple
     * such commits, it prints the ids out on separate lines.
     * The commit message is a single operand; to indicate a
     * multiword message, put the operand in quotation marks,
     * as for the commit command below. Hint: the hint for this
     * command is the same as the one for global-log.
     * @param args string[]
     */
    public void find(String[] args) {
        String message = args[1];
        boolean hascommit = false;
        List<String> allcommits = Utils.plainFilenamesIn(CWD.getPath()
                + "/.gitlet/commits");
        for (String commitid : allcommits) {
            Commit goingthrough = Commit.readCommit(commitid);
            if ((goingthrough.getMessage()).equals(message)) {
                System.out.println(commitid);
                hascommit = true;
            }
        }
        if (!hascommit) {
            System.out.println("Found no commit with that message.");
        }
    }

    /**
     * Displays what branches currently exist, and marks the
     * current branch with a *. Also displays what files have
     * been staged for addition or removal.
     */
    public void status() {
        List<String> branchnames = Utils.plainFilenamesIn(CWD.getPath()
                + "/.gitlet/branches/");
        Set<String> stagedfiles = stage.getAdded().keySet();
        ArrayList<String> removedfiles = stage.getRemoved();

        /*branches*/
        System.out.println("=== Branches ===");
        /*go through all branches*/
        for (String branch : branchnames) {
            /*if main branch*/
            if (branch.equals("HEAD.txt")) {
                continue;
            }
            if (branch.equals(getCurrBranchName())) {
                System.out.println("*" + branch);
            } else {
                System.out.println(branch);
            }
        }
        System.out.println();

        /*stagedfiles*/
        System.out.println("=== Staged Files ===");
        /*go through all staged files*/
        for (String staged : stagedfiles) {
            System.out.println(staged);
        }
        System.out.println();

        /*removedfiles*/
        System.out.println("=== Removed Files ===");
        /*go through all removed files*/
        for (String removed : removedfiles) {
            System.out.println(removed);
        }
        System.out.println();

        /*not staged for commit*/
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();

        /*untracked*/
        System.out.println("=== Untracked Files ===\n");
        System.out.println();
    }

    /**
     * Creates a new branch with the given name, and points it at
     * the current head commit. A branch is nothing more than a name
     * for a reference (a SHA-1 identifier) to a commit node. This
     * command does NOT immediately switch to the newly created branch
     * (just as in real Git). Before you ever call branch, your code
     * should be running with a default branch called “main”.
     * @param args string[]
     */
    public void branch(String[] args) {
        String branchname = args[1];
        File branchfile = new File(CWD.getPath()
                + "/.gitlet/branches/" + branchname);
        if (branchfile.exists()) {
            System.out.println("A branch with that name already exists.");
            return;
        }
        try {
            Files.write(Paths.get(CWD.getPath()
                            + "/.gitlet/branches/" + branchname),
                    (getCurrentCommit()).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes the branch with the given name. This only means to delete
     * the pointer associated with the branch; it does not mean to delete
     * all commits that were created under the branch, or anything like that.
     * @param args string[]
     */
    public void rmbranch(String[] args) {
        String branchname = args[1];
        if (branchname.equals(getCurrBranchName())) {
            System.out.println("Cannot remove the current branch.");
            return;
        }

        if (!(new File(CWD.getPath()
                + "/.gitlet/branches/" + branchname)).exists()) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        File branchfile = new File(CWD.getPath()
                + "/.gitlet/branches/" + branchname);
        branchfile.delete();
    }

    /**
     * Checks out all the files tracked by the given commit.
     * Removes tracked files that are not present in that commit.
     * Also moves the current branch’s head to that commit node.
     * See the intro for an example of what happens to the head pointer
     * after using reset. The [commit id] may be abbreviated as for
     * checkout. The staging area is cleared. The command is
     * essentially checkout of an arbitrary commit that also changes
     * the current branch head.
     * @param args string[]
     */
    public void reset(String[] args) {
        String commitid = args[1];
        boolean hasid = false;
        /*search through list of commit ids*/
        List<String> listofcommits = Utils.plainFilenamesIn(CWD.getPath()
                + "/.gitlet/commits");
        for (String id : listofcommits) {
            if (id.contains(commitid)) {
                hasid = true;
            }
        }
        /*if commit doesnt exist*/
        if (!hasid) {
            System.out.println("No commit with that id exists.");
            return;
        }
        Commit resetcommit = Commit.readCommit(commitid);
        /* current commit*/
        Commit curr = Commit.readCommit(getCurrentCommit());

        /* check if any untracked files*/
        List<String> directoryblob = Utils.plainFilenamesIn(CWD.getPath());
        for (String dirblob : directoryblob) {
            if (!curr.getMap().containsKey(dirblob)) {
                if (resetcommit.getMap().containsKey(dirblob)) {
                    System.out.println("There is an untracked file in the way; "
                            + "delete it, or add and commit it first.");
                    return;
                }
            }
        }

        /*remove any files that are not in the branch in directory*/
        for (String dirblob : directoryblob) {
            if (!resetcommit.getMap().containsKey(dirblob)) {
                File todelete = new File(CWD.getPath() + "/" + dirblob);
                todelete.delete();
            }
        }

        /*all blobs in wanted commit*/
        HashMap<String, String> blobstoadd = resetcommit.getMap();

        for (String blobid : blobstoadd.keySet()) {
            /*if the file is already there in CWD,
            delete it and write a new one*/
            if ((new File(CWD.getPath() + "/" + blobid)).exists()) {
                (new File(CWD.getPath() + "/" + blobid)).delete();
            }
            /*get the blob file from commit blobmap*/
            File newBlob = new File(CWD.getPath()
                    + "/.gitlet/blobs/" + resetcommit.getMap().get(blobid)
                    + ".txt");
            /*make a new file and write the blob contents into it*/
            File newFile = new File(CWD.getPath(), blobid);
            Utils.writeContents(newFile, Utils.readContents(newBlob));
        }

        /*all files in current commit*/
        HashMap<String, String> currentblobs = curr.getMap();
        for (String blobid : currentblobs.keySet()) {
            /*if checked out branch doesnt have the file*/
            if (!blobstoadd.containsKey(blobid)) {
                /*remove it*/
                File deletefile = new File(CWD.getPath()
                        + "/.gitlet/blobs/" + blobid + ".txt");
                deletefile.delete();
            }
        }
        stage.clear();
        stage.save();
        /*at end of command, make this commit branch the head*/
        try {
            Files.write((Paths.get(CWD.getPath()
                    + "/.gitlet/branches/"
                    + getCurrBranchName())), commitid.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Will do stuff.
     * @param args string[]
     */
    public void merge(String[] args) {
        String branchname = args[1];
        String curBranchName = getCurrBranchName();

        if (!stage.isClear()) {
            System.out.println("You have uncommitted changes.");
            return;
        }
        if (branchname.equals(curBranchName)) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        }
        if (!(new File(CWD.getPath()
                + "/.gitlet/branches/" + branchname)).exists()) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        /* commit at given branch*/
        String branchcomid = Utils.readContentsAsString(new File(CWD.getPath()
                + "/.gitlet/branches/" + branchname));
        Commit branchcom = Commit.readCommit(branchcomid);
        /* current commit*/
        Commit curr = Commit.readCommit(getCurrentCommit());

        List<String> directoryblob = Utils.plainFilenamesIn(CWD.getPath());
        for (String dirblob : directoryblob) {
            if (!curr.getMap().containsKey(dirblob)) {
                if (branchcom.getMap().containsKey(dirblob)) {
                    System.out.println("There is an untracked file in the way; "
                            + "delete it, or add and commit it first.");
                    return;
                }
            }
        }
        String splitpointid = "";
        /* find split point */
        splitpointid = mergeSplitPoint(curr, branchcom);

        /* if splitpoint is same as given branch */
        if (splitpointid.equals(branchcomid)) {
            System.out.println("Given branch is an ancestor "
                    + "of the current branch.");
            return;
        }
        /* if splitpoint is same as curr branch */
        if (splitpointid.equals(getCurrentCommit())) {
            /* checkout curr branch */
            checkout(new String[]{"checkout", branchname});
            System.out.println("Current branch fast-forwarded.");
            return;
        }
        Commit splitpoint = Commit.readCommit(splitpointid);
        boolean isconflict = false;

        Set<String> allfiles = new HashSet<String>();
        allfiles.addAll(splitpoint.getMap().keySet());
        allfiles.addAll(curr.getMap().keySet());
        allfiles.addAll(branchcom.getMap().keySet());

        isconflict = mergeGoingThroughFiles(allfiles,
                splitpoint, branchcom, curr, branchcomid);

        commit(new String[]{"commit", ("Merged "
                + branchname + " into " + curBranchName + ".")});
        if (isconflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    /* helpers*/

    /**
     * Goes through all files for the three commits.
     * Accordingly checksout/adds/removes.
     * @param allfiles set<string>
     * @param splitpoint commit
     * @param branchcom commit
     * @param curr commit
     * @param branchcomid string
     * @return
     */
    public boolean mergeGoingThroughFiles(Set<String> allfiles,
                                          Commit splitpoint,
                                          Commit branchcom, Commit curr,
                                          String branchcomid) {
        boolean isconflict = false;
        for (String filename : allfiles) {
            /* if modified in branch but not head (in splitpoint too) */
            if (splitpoint.getMap().containsKey(filename)
                    && branchcom.getMap().containsKey(filename)
                    && curr.getMap().containsKey(filename)
                    && splitpoint.getMap().get(filename).equals(
                    curr.getMap().get(filename))
                    && !branchcom.getMap().get(filename).equals(
                    curr.getMap().get(filename))) {
                checkout(new String[]{"checkout", branchcomid, "--", filename});
                stage.add(filename, branchcom.getMap().get(filename));
                continue;
            }
            /* modified in branch and head in diff ways (in splitpoint too)*/
            if (splitpoint.getMap().containsKey(filename)
                    && branchcom.getMap().containsKey(filename)
                    && curr.getMap().containsKey(filename)
                    && !branchcom.getMap().get(filename).equals(
                    curr.getMap().get(filename))
                    && !splitpoint.getMap().get(filename).equals(
                    curr.getMap().get(filename))
                    && !splitpoint.getMap().get(filename).equals(
                    branchcom.getMap().get(filename))) {
                mergeIsConflict(curr, branchcom, filename);
                isconflict = true;
                continue;
            }
            /* modified in branch and deleted in head (in splitpoint too)*/
            if (splitpoint.getMap().containsKey(filename)
                    && branchcom.getMap().containsKey(filename)
                    && !curr.getMap().containsKey(filename)
                    && !splitpoint.getMap().get(filename).equals(
                    branchcom.getMap().get(filename))) {
                mergeIsConflict(curr, branchcom, filename);
                isconflict = true;
                continue;
            }
            /* modified in head and deleted in branch (in splitpoint too)*/
            if (splitpoint.getMap().containsKey(filename)
                    && !branchcom.getMap().containsKey(filename)
                    && curr.getMap().containsKey(filename)
                    && !splitpoint.getMap().get(filename).equals(
                    curr.getMap().get(filename))) {
                mergeIsConflict(curr, branchcom, filename);
                isconflict = true;
                continue;
            }
            /* not in split or curr but in branch */
            if (!splitpoint.getMap().containsKey(filename)
                    && !curr.getMap().containsKey(filename)
                    && branchcom.getMap().containsKey(filename)) {
                checkout(new String[]{"checkout", branchcomid, "--", filename});
                stage.add(filename, branchcom.getMap().get(filename));
                continue;
            }
            /* same in head and split but not in branch */
            if (splitpoint.getMap().containsKey(filename)
                    && curr.getMap().containsKey(filename)
                    && splitpoint.getMap().get(filename).equals(
                    curr.getMap().get(filename))
                    && !branchcom.getMap().containsKey(filename)) {
                /*  should be removed (and untracked) */
                rm(new String[]{"rm", filename});
                continue;
            }
        }
        return isconflict;
    }


    /**
     * Helper for merge to find the split point.
     * @param curr commit
     * @param branchcom commit
     * @return splitpointid
     */
    public String mergeSplitPoint(Commit curr, Commit branchcom) {
        ArrayList<String> currcommits = new ArrayList<>();
        currcommits.add(curr.getID());
        while (curr != null) {
            if (!curr.getParents().isEmpty()) {
                String parentid = curr.getParents().get(0);
                currcommits.add(parentid);
                if (curr.getParents().size() == 2) {
                    currcommits.add(curr.getParents().get(1));
                }
                curr = Commit.readCommit(parentid);
            } else {
                break;
            }
        }

        if (currcommits.contains(branchcom.getID())) {
            return branchcom.getID();
        }
        while (branchcom != null) {
            if (!branchcom.getParents().isEmpty()) {
                String parentid = branchcom.getParents().get(0);
                if (currcommits.contains(parentid)) {
                    return parentid;
                }
                if (branchcom.getParents().size() == 2) {
                    if (currcommits.contains(branchcom.getParents().get(1))) {
                        return branchcom.getParents().get(1);
                    }
                }
                branchcom = Commit.readCommit(parentid);
            } else {
                break;
            }
        }

        return "";
    }


    /**
     * If conflict, replaces contents of conflicted file with this.
     * Adds to staging.
     * @param curr commit
     * @param branchcom commit
     * @param filename string
     * @return true
     */
    public boolean mergeIsConflict(Commit curr,
                                   Commit branchcom, String filename) {
        String curFileContent = "", branchFileContent = "";
        if (curr.getMap().get(filename) != null) {
            curFileContent = Utils.readContentsAsString(new File(CWD.getPath()
                    + "/.gitlet/blobs/"
                    + curr.getMap().get(filename) + ".txt"));
        }
        if (branchcom.getMap().get(filename) != null) {
            branchFileContent = Utils.readContentsAsString(
                    new File(CWD.getPath()
                    + "/.gitlet/blobs/" + branchcom.getMap().get(filename)
                    + ".txt"));
        }
        String newcontents = "<<<<<<< HEAD\n"
                + curFileContent
                + "=======\n"
                + branchFileContent
                + ">>>>>>>\n";

        String blobid = Utils.sha1(newcontents);

        try {
            Files.write(Paths.get(CWD.getPath()
                    + "/.gitlet/blobs/" + blobid
                    + ".txt"), (newcontents).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        stage.add(filename, blobid);

        /* replace the contents of the conflicted file. Delete and add again*/
        if ((new File(CWD.getPath() + filename)).exists()) {
            (new File(CWD.getPath() + filename)).delete();
        }

        /*make a new file and write the contents into it*/
        File newFile = new File(CWD.getPath(), filename);
        Utils.writeContents(newFile, (newcontents).getBytes());

        return true;
    }

    /**
     * Returns the current commit ID as a string.
     * @return CommitID
     */
    public String getCurrentCommit() {
        String branchname = Utils.readContentsAsString(HEAD);
        File path = new File(CWD.getPath()
                + "/.gitlet/branches/" + branchname);
        return Utils.readContentsAsString(path);
    }

    /**
     * Returns the current branch name (main).
     * @return current branch name
     */
    public String getCurrBranchName() {
        String branchname = Utils.readContentsAsString(HEAD);
        return branchname;
    }

}
