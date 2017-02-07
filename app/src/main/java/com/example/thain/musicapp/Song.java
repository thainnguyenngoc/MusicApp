package com.example.thain.musicapp;

import java.io.Serializable;

public class Song implements Serializable {

    private String mTitle;
    private String mArtist;
    private String mPath;

    private long mId;
    private int mDuration;

    public Song(long id, String title, String artist, String path, int duration) {
        mId = id;
        mTitle = title;
        mArtist = artist;
        mPath = path;
        mDuration = duration;
    }

    public long getId() {
        return mId;
    }

    public String getArtist() {
        return mArtist;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getPath() {
        return mPath;
    }

    public int getDuration() {
        return mDuration;
    }

    @Override
    public String toString() {
        return "Song{" + "mId=" + getId() +
                ", mTitle=" + getTitle() +
                ", mArtist=" + getArtist() +
                ", mPath=" + getPath() +
                ", mDuration=" + getDuration() + "}";
    }

//    @Override
//    public boolean equals(Object o) {
//        if (this == o)
//            return true;
//
//        if (o == null)
//            return false;
//
//        return this.toString().equals(o.toString());
//    }
}
