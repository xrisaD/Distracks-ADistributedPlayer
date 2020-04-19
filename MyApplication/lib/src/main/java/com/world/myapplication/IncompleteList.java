package com.world.myapplication;


import javax.swing.plaf.multi.MultiInternalFrameUI;
import java.util.ArrayList;
import java.util.Iterator;

public class IncompleteList<T> implements  Iterable<T> {
    /**
     *  A class that represents a music file that doesn't have all available chunks and are instead added little by
     *  little
     *
     *  This list supports only additions and iterations
     *
     *  This class is meant to be used like this
     *
     *  for(MusicFile chunk : IncompleteMusicFile) {
     *
     *  }
     *
     *  This for each is supposed to block each time until a new musicfile becomes available
     *
     */

    //The array of chunks
    private final ArrayList<T> chunks = new ArrayList<>();
    int numItems;

    public IncompleteList(int numItems){ this.numItems = numItems;}

    public void add(T e){
        synchronized (chunks) {
            chunks.add(e);
            chunks.notifyAll();
        }
    }

    public int size() {
        return numItems;
    }

    public Iterator<T> iterator(){
        return new Iterator<T>() {
            int currChunk = 0;
            @Override
            public boolean hasNext() {
                return currChunk < numItems;
            }

            @Override
            public T next() {
                //We need to acquire the arraylist's monitor
                synchronized (chunks) {
                    while (currChunk >= chunks.size() ) {
                        try {
                            //While there is no chunk available
                            chunks.wait();
                        }
                        //We got interrupted so we try again
                        catch (InterruptedException e) {
                        }
                    }
                }
                //There is a chunk available
                //Return it and increase the current chunk pointer
                return chunks.get(currChunk++);
            }

        };
    }

    @Override
    public String toString() {
        return "IncompleteList{" +
                "chunks=" + chunks +
                ", numItems=" + numItems +
                '}';
    }
}

