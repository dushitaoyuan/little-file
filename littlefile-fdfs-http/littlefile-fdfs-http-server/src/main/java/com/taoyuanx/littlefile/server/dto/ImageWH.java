package com.taoyuanx.littlefile.server.dto;


/**
 * @author 都市桃源
 * 2019年3月5日 下午5:31:26
 * @description 图片宽高
 *
*/
public class ImageWH {


    public ImageWH(int w, int h) {
        this.w = w;
        this.h = h;
    }
    private int w;

    private int h;

    public int getW() {
        return w;
    }

    public void setW(int w) {
        this.w = w;
    }

    public int getH() {
        return h;
    }

    public void setH(int h) {
        this.h = h;
    }
}
