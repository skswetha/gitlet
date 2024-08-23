Main

- run all gitlet commands Staging Area
- store files after add, a list of the blobs added+removed Commit
- info on each file in commit (id, time, message, parents, blobmap)
  Blob
- all file versions stored name of file contents of file file id Repository
- has file for head, has hashmap of commits,
- has all the gitlet commands init add commit rm log global-log find status checkout branch rm-branch reset merge

________

# Gitlet Design Doc

**Name**: Swetha Karthikeyan

## Classes and Data Structures

### Commit

#### Instance Variables

* message - has message of a commit
* time - time at which commit was created
* blobmap - hashmap with all blobs
* parents - parent commit of a commit object
* id - sha1 of commit




