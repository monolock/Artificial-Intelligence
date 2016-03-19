package chess;

/**
 * Created by gejing on 2/7/16.
 */

/**
 * Created by gejing on 2/6/16.
 */
public class Entry{
    public int value;
    public int depth;

    public Entry(int value, int depth){
        this.value = value;
        this.depth = depth;
    }

    public void setEntry(int value, int depth){
        this.value = value;
        this.depth = depth;
    }
}