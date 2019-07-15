package net.hangyas.antpaint.app;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Created by hangyas on 2015-06-24
 */
public class WeirdHeap<T> {

    int size = 1;
    List<T> data;

    public WeirdHeap(){
        data = new ArrayList<T>(256);
        data.add(0, null);
    }

    public WeirdHeap(int capacity){
        data = new ArrayList<T>(capacity);
        data.add(0, null);
    }

    public void push(T e){
        data.add(size, e);
        ++size;
    }

    private int right(int i){
        return i * 2 + 1;
    }

    private int left(int i){
        return i * 2;
    }

    public List<T> getPreFix(){
        List<T> r = new ArrayList<T>(size);
        if (size == 1)
            return r;
        int counter = 0;

        Stack<Integer> stack = new Stack<Integer>();
        stack.push(1);

        while (!stack.empty()) {
            int node = stack.pop();
            int lc, rc;

            r.add(counter++, data.get(node));

            lc = node * 2;
            if (lc >= size)
                continue;
            stack.push(lc);

            rc = node * 2 + 1;
            if (rc >= size)
                continue;
            stack.push(rc);
        }

        return r;
    }


    public boolean isEmpty(){
        return size == 0;
    }

}
