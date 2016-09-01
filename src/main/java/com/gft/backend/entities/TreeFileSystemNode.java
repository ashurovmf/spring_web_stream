package com.gft.backend.entities;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by miav on 2016-08-31.
 */
public class TreeFileSystemNode<T> implements Iterable<TreeFileSystemNode<T>> {

    T data = null;
    boolean isDirectory = false;
    TreeFileSystemNode<T> parent = null;
    List<TreeFileSystemNode<T>> children = new LinkedList<TreeFileSystemNode<T>>();
    int level = 0;

    public TreeFileSystemNode() {
    }

    public TreeFileSystemNode(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public void setDirectory(boolean directory) {
        isDirectory = directory;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public TreeFileSystemNode<T> addChild(TreeFileSystemNode<T> childNode) {
        childNode.parent = this;
        childNode.level = this.level + 1;
        this.children.add(childNode);
        return childNode;
    }

    public int childCount(){
        return  children.size();
    }

    @Override
    public Iterator<TreeFileSystemNode<T>> iterator() {
        return children.listIterator();
    }

}
