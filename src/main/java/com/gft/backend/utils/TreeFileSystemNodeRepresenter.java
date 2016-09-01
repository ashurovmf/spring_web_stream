package com.gft.backend.utils;

import com.gft.backend.entities.TreeFileSystemNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by miav on 2016-09-01.
 */
public class TreeFileSystemNodeRepresenter {

    public static final int INITIAL_CAPACITY = 512;

    public static String[] convertToStringArray(TreeFileSystemNode<String> rootNode){
        List<String> buff = new ArrayList<>();
        recursiveRoundTree(rootNode, buff);

        String[] result = new String[buff.size()];
        result = buff.toArray(result);
        return result;
    }

    private static void recursiveRoundTree(TreeFileSystemNode<String> rootNode, List<String> buffer){
        StringBuilder builder = new StringBuilder(INITIAL_CAPACITY);
        for (int i = 0; i < rootNode.getLevel(); ++i) builder.append("___");
        builder.append("|->");
        if(rootNode.isDirectory())
            builder.append("[");
        builder.append(rootNode.getData());
        if(rootNode.isDirectory())
            builder.append("]");
        buffer.add(builder.toString());
        if(rootNode.childCount() > 0){
            for (TreeFileSystemNode<String> child : rootNode)
                recursiveRoundTree(child,buffer);
        }
    }
}
