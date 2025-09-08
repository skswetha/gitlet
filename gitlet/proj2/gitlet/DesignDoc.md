________

# Gitlet Design Doc

**Name**: Swetha Karthikeyan

________

Gitlet is a simplified version control system inspired by Git. It allows users to:
* Initialize a repository
* Track and stage files
* Create commits
* Manage branches
* Reset to earlier commits
* Merge changes with conflict handling

The system is organized into components Commit, Blob, Staging, Repository, a driver Main, and support utilities. Persistent data is stored inside a ```.gitlet``` directory.

## Classes and Data Structures

### Main
* Entry point, parses all arguments and dispatches to Repository.
* example:
```
java gitlet.Main add file.txt
```
* calls ```Repository.add()```

### Repository
* Implements all Gitlet commands.
    * init, add, commit, rm
    * log, global-log, find, status
    * checkout, branch, rm-branch
    * reset,merge
* layout:
```
.gitlet/
  commits/         # serialized Commit objects
  blobs/           # file contents, one per blob (SHA-1 named)
  branches/        # one file per branch storing a commit ID
  staging_area/    # serialized Staging object
  HEAD             # current branch name
```

### Commits
* Represents a snapshot of the repository.
* Fields:
    * message: commit message
    * time: timestamp (Date(0) for initial commit)
    * blobmap: map of filename -> blobID
    * parents: list of parent commit IDs (1 for normal commits, 2 for merges)
    * id: SHA-1 hash of serialized commit
* Methods:
    * save(): serialize and persist commit to ```.gitlet/commits/```
    * readCommit(id): load commit by ID

### Blob
* Represents a file's contents at specific version.
* Stores as plain text file inside ```.gitlet/blobs```, named by SHA-1 of contents

### Staging
* Temporary area before creating a commit.
* Fields:
    * addedL map of filename -> blobID
    * removed: set of filenames
* Workflow:
    * on add: file goes into added
    * on rm: file goes into removed
    * on commit: apply staged changes -> new commit -> clear staging

### Utils
* Helper functions to simplify file ops and hashing
* Key Functions:
    * sha1(...): generate SHA-1 hashes
    * readConents/writeConents: read/write file data safely
    * serialize/readObject: save and restore serialized objects (Commit, Staging)
    * restrictedDelete: prevents deleting files outside .gitlet
    * join: simplifies path creation
    * plainFilenamesIn: returns sorted files in a directory

 ### DumpObj
 * Debugging utility for dev
 * Reads a serialized file by Utils.writeObject, deserializes, and calls its dump() method
 * Inspects internal state of commits or staging objects during dev testing.




