package com.ozonehis.data.pipelines.utils;

public class QueryFile {

    public String fileName;

    public String content;

    public String parent;

    public QueryFile(String parent, String fileName, String content) {
        this.content = content;
        this.fileName = fileName;
        this.parent = parent;
    }
}
