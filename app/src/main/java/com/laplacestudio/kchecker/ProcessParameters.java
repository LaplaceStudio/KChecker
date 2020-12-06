package com.laplacestudio.kchecker;

import android.util.Size;

public class ProcessParameters {

    public int ctrlRegBinThresh=127;
    public int textBinThresh = 127;
    public int diskSESize = 10;
    public Size lineSESize;
    public double areaProportion=0.62;
    public double largeNoiseThreshold = 0.4;
    public int textThreshold = 32;
    public int erodeSize = 2;
    public int dilateSize = 1;


    public ProcessParameters(int ctrlRegBinThresh, int textBinThresh, int diskSESize, Size lineSESize){
        this.ctrlRegBinThresh=ctrlRegBinThresh;
        this.textBinThresh =textBinThresh;
        this.diskSESize=diskSESize;
        this.lineSESize=lineSESize;

    }

}
