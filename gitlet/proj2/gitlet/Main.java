package gitlet;

import java.io.File;

/**
 * Driver class for Gitlet, a subset of the Git version-control system.
 *
 * @author Swetha Karthikeyan
 */
public class Main {

    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND1> <OPERAND2> ...
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            return;
        }
        String firstArg = args[0];
        Repository repo = new Repository();
        switch (firstArg) {
            case "init":
                if (args.length == 1) {
                    repo.init(args);
                }
                break;
            case "add":
                if ((args.length == 2) && initializedGitlet()) {
                    repo.add(args);
                }
                break;
            case "commit":
                if ((args.length == 2) && initializedGitlet()) {
                    repo.commit(args);
                }
                break;
            case "log":
                if ((args.length == 1) && initializedGitlet()) {
                    repo.log();
                }
                break;
            case "checkout":
                if (initializedGitlet()) {
                    repo.checkout(args);
                }
                break;
            case "find":
                if (initializedGitlet()) {
                    repo.find(args);
                }
                break;
            case "status":
                if ((args.length == 1) && initializedGitlet()) {
                    repo.status();
                }
                break;
            case "rm":
                if ((args.length == 2) && initializedGitlet()) {
                    repo.rm(args);
                }
                break;
            case "global-log":
                if (initializedGitlet()) {
                    repo.globallog();
                }
                break;
            case "branch":
                if ((args.length == 2) && initializedGitlet()) {
                    repo.branch(args);
                }
                break;
            case "rm-branch":
                if (initializedGitlet()) {
                    repo.rmbranch(args);
                }
                break;
            case "reset":
                if (initializedGitlet()) {
                    repo.reset(args);
                }
                break;
            case "merge":
                if ((args.length == 2) && initializedGitlet()) {
                    repo.merge(args);
                }
                break;
            default :
                System.out.println("No command with that name exists.");
                return;
        }
    }


    /**
     * Checks to see if Gitlet has been initialized.
     * @return if gitlet is initialized
     */
    static boolean initializedGitlet() {
        File gitlet = new File(".gitlet");
        if (!gitlet.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return false;
        }
        return true;
    }


}
