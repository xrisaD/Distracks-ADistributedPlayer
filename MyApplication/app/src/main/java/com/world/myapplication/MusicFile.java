package com.world.myapplication;



import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;

public class MusicFile implements Serializable {
    private byte[] musicFileExtract;
    private MusicFileMetaData metaData;
    private int numChunk;
    public BigInteger biggie;
    //constructor
    public MusicFile( MusicFileMetaData metaData ,byte[] musicFileExtract){
        this.metaData = metaData;
        this.musicFileExtract=musicFileExtract;
        numChunk=0;
        biggie=new BigInteger("0");
    }
    public MusicFile( MusicFileMetaData metaData ,byte[] musicFileExtract, BigInteger biggie){
        this(metaData,musicFileExtract);
        this.biggie=biggie;
    }

    public MusicFile(){

    }

    //setters and getters
    public byte[] getMusicFileExtract() {
        return musicFileExtract;
    }
    public void setMusicFileExtract(byte[] musicFileExtract) {
        this.musicFileExtract = musicFileExtract;
    }
    public void setMetaData(MusicFileMetaData metaData){
        this.metaData = metaData;
    }
    public MusicFileMetaData getMetaData(){
        return metaData;
    }

    public int getNumChunk() {
        return numChunk;
    }

    public void setNumChunk(int numChunk) {
        this.numChunk = numChunk;
    }
}
