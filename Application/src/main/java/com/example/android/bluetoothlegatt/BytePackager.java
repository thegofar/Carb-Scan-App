package com.example.android.bluetoothlegatt;

/**
 * Created by craig on 17/12/2017.
 */

public class BytePackager {
    int mArraySize;
    byte[] mArray;

    public BytePackager(byte[] arr, int len)
    {
        mArray=arr;
        mArraySize = len;
    }

    public int getByte(int index)
    {
        // a Java byte is signed so by converting to a 4 byte int we essentially make it unsigned
        if (index+1 > mArraySize)
        {
            return 0;
        }
        return mArray[index] &0xFF ;
    }

    public int getU16(int index)
    {
        // we can still use an int here because they are 4 bytes and a u16 is only 2
        int rtn;
        if (index+2 > mArraySize)
        {
            return 0;
        }
        int ii = index+1;
        int c1 = mArray[ii]& 0xFF;
        int c2 = mArray[--ii]& 0xFF;

        rtn = ((c1 << 8) | (c2)) & 0xFFFF;
        return rtn;
    }

    public long getU32(int index)
    {
        // the data is packed on the micro such that the little end is at the start of the array
        long rtn;
        if (index+4 > mArraySize)
        {
            return 0;
        }
        int ii = index+3;
        int c1 = mArray[ii] & 0xFF;
        int c2 = mArray[--ii] & 0xFF;
        long c3 = mArray[--ii] & 0xFF;
        long c4 = mArray[--ii] & 0xFF;
        rtn = ((c1 << 24) | (c2 << 16) | (c3 << 8) | (c4)) & 0xFFFFFFFFL;
        return rtn;
    }
}
