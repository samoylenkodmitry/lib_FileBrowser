package com.samart.filebrowser;


import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class FileBrowser {


    private final List<File> files = new ArrayList<File>();
    private final FilenameFilter fileFilter;
    private final boolean isDirsOnly;
    private final List<File> parentDirs = new ArrayList<File>();
    private File currentDir;
    private File lastDir;

    public FileBrowser(final File startDir, final boolean dirsOnly, final String[] filter) {

        this.isDirsOnly = dirsOnly;
        fileFilter = new FilenameFilter() {

            @Override
            public boolean accept(File dir, String filename) {
                final File file = new File(dir, filename);
                if (file.isHidden()) {
                    return false;
                }
                boolean isDirectory = file.isDirectory();
                if (isDirsOnly) {
                    return isDirectory;
                } else {
                    boolean isFiltered = isDirectory;
                    if (null != filter && !isDirectory) {
                        for (String filterString : filter) {
                            if (filename.contains(filterString)) {
                                isFiltered = true;
                                break;
                            }
                        }
                    }
                    return isFiltered;
                }
            }
        };
        goDir(startDir);
    }

    public FileBrowser(final File startDir, final boolean dirsOnly) {
        this(startDir, dirsOnly, null);
    }

    public String getPath() {
        return currentDir.getAbsolutePath();
    }

    public boolean createNewDir(String filename) {
        final File file =
                new File(currentDir, filename);
        return file.mkdir();
    }

    public List<File> getFiles() {
        return this.files;
    }

    /**
     * use this only from root directory
     *
     * @param dir directory to navigate from root
     */
    public void navigateTo(File dir) {
        if (null == dir || !dir.exists() && !dir.isDirectory()) {
            return;
        }
        LinkedList<File> queue = new LinkedList<File>();
        while (dir != null) {
            queue.add(dir);
            dir = dir.getParentFile();
        }
        while (!queue.isEmpty()) {
            dir = queue.removeLast();
            if (!navDir(dir)) {
                break;
            }
        }
    }

    public boolean update() {
        if (null == currentDir) {
            return false;
        }
        File[] filesList = currentDir.listFiles(fileFilter);
        if (null == filesList) {
            return false;
        }
        if (currentDir != null) {
            lastDir = currentDir;
        }

        files.clear();
        Collections.addAll(files, filesList);
        Collections.sort(files);
        return true;
    }

    private boolean goDir(final File newDir) {
        if (null == newDir) {
            return false;
        }
        File[] filesList = newDir.listFiles(fileFilter);
        if (null == filesList) {
            return false;
        }
        if (currentDir != null) {
            lastDir = currentDir;
        }

        this.currentDir = newDir;
        files.clear();
        Collections.addAll(files, filesList);
        Collections.sort(files);
        return true;
    }

    public boolean goLastDir() {
        return goDir(lastDir);
    }

    public boolean goUpDir() {
        final String parentDirName = currentDir.getParent();
        if (null == parentDirName) {
            return false;
        } else {
            final File parent = new File(parentDirName);
            return goDir(parent);
        }
    }

    public File getCurrentDir() {
        return currentDir;
    }

    public File getLastDir() {
        return lastDir;
    }

    public boolean goToParent(final int parentIndex) {
        if (parentIndex < 0) {
            return false;
        }
        File dir = null;
        int index = parentDirs.size();
        while (--index >= parentIndex) {
            dir = parentDirs.remove(index);
        }
        return null != dir && goDir(dir);
    }

    public List<File> getParentDirs() {
        return parentDirs;
    }

    public boolean goDir(int position) {
        if (position < 0) {
            return false;
        }
        final File dir = files.get(position);
        final File currDir = currentDir;
        final boolean success = goDir(dir);
        if (success) {
            parentDirs.add(currDir);
        }
        return success;
    }

    public boolean navDir(final File file) {
        if (null == file) {
            return false;
        }
        final File currDir = currentDir;
        if (currDir.compareTo(file) == 0) {
            return true;
        }
        final boolean success = goDir(file);
        if (success) {
            parentDirs.add(currDir);
        }
        return success;
    }

    public String getAbsolutePath(int position) {
        return files.get(position).getAbsolutePath();
    }

    public String getName(int position) {
        return files.get(position).getName();
    }

    public File getFile(int position) {
        if (position < 0) {
            return null;
        } else {
            return files.get(position);
        }
    }
}
