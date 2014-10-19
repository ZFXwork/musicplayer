package com.example.data;

/**
 8歌曲类包括歌曲名、艺术家、路径、时长等
 包括获取方法
 */
public class music {

    private  String musicName;
    private  String musicArtist;
    private  String musicPath;
    private  String musicDuration;

    public music(String musicName,String musicArtist,String musicPath,String musicDuration)
    {
        this.musicName=musicName;
        this.musicArtist=musicArtist;
        this.musicPath=musicPath;
        this.musicDuration=musicDuration;
    }

    public String getMusicName()
    {
        return this.musicName;
    }
    public String getMusicArtist()
    {
        return this.musicArtist;
    }
    public String getMusicPath()
    {
        return  this.musicPath;
    }
    public String getMusicDuration()
    {
        return  this.musicDuration;
    }
}
