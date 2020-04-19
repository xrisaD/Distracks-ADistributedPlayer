package com.world.myapplication;


import java.util.*;
import java.util.LinkedHashMap;
public class SynchronizedLRUCache<K,V> extends LinkedHashMap<K,V>  {

    long misses;
    long lookups;
    long cacheSize;
    long maxSize;
    public SynchronizedLRUCache(int n){
        super(n,0.25f,true);
        lookups = 0;
        misses = 0;
        maxSize = n;
        cacheSize = 0;
    }
    public synchronized V lookUp(K key){
        lookups +=1;
        V result = get(key);
        if(result == null) misses +=1;
        return result;
    }
    public synchronized void store(K key,V value){

        put(key,value);
        ++cacheSize;
    }
    // Called after every store call so no need to syncrhonize it
    @Override
    protected boolean removeEldestEntry(Map.Entry<K,V> eldest){
        return cacheSize >= maxSize;
    }

    public long getHits(){return lookups-misses;}
    public long getMisses(){return misses;}
    public long getNumberOfLookUps(){return lookups;}
    public double getHitRatio(){return getHits()/(double)getNumberOfLookUps();}
}