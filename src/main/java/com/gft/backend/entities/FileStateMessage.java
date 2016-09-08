package com.gft.backend.entities;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by miav on 2016-09-05.
 */
public class FileStateMessage {

    public static final String STATE_NOPE = "NOP";
    public static final String STATE_ADDED = "ADD";
    public static final String STATE_MODIFIED = "MOD";
    public static final String STATE_DELETED = "DEL";

    private String state = STATE_NOPE;

    private String fileName;

    private String hashId;

    private String[] parent;

    private boolean isDirectory = false;

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getHashId() {
        return hashId;
    }

    public void setHashId(String hashId) { this.hashId = hashId; }

    public String[] getParent() { return parent; }

    public void setParent(String[] parent) { this.parent = parent; }

    public boolean isDirectory() { return isDirectory; }

    public void setDirectory(boolean directory) { isDirectory = directory; }

    public static String getMD5Hash(String data){
        MessageDigest m = null;
        try {
            m = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        m.reset();
        m.update(data.getBytes());
        byte[] digest = m.digest();
        BigInteger bigInt = new BigInteger(1,digest);
        String hashtext = bigInt.toString(16);
        return hashtext;
    }
}
