package com.taoyuanx.littlefile.server.utils;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

public class PdfToImage {
    //经过测试,dpi为96,100,105,120,150,200中,105显示效果较为清晰,体积稳定,dpi越高图片体积越大
    //一般电脑显示分辨率为96
    public static final float DEFAULT_DPI = 105;

    /**
     * pdf转一张图片
     *
     * @param pdfPath
     */
    public static void pdfToOneImage(File pdfPath, File imgPath) {
        try {

            if (!pdfPath.exists()) {
                return;
            }
            //图像合并使用参数
            int width = 0; // 总宽度
            int[] singleImgRGB; // 保存一张图片中的RGB数据
            int shiftHeight = 0;
            BufferedImage imageResult = null;//保存每张图片的像素值
            //利用PdfBox生成图像
            PDDocument pdDocument = PDDocument.load(pdfPath);
            PDFRenderer renderer = new PDFRenderer(pdDocument);
            //循环每个页码
            for (int i = 0, len = pdDocument.getNumberOfPages(); i < len; i++) {
                BufferedImage image = renderer.renderImageWithDPI(i, DEFAULT_DPI, ImageType.RGB);
                int imageHeight = image.getHeight();
                int imageWidth = image.getWidth();
                if (i == 0) {//计算高度和偏移量
                    width = imageWidth;//使用第一张图片宽度;
                    //保存每页图片的像素值
                    imageResult = new BufferedImage(width, imageHeight * len, BufferedImage.TYPE_INT_RGB);
                } else {
                    shiftHeight += imageHeight; // 计算偏移高度
                }
                singleImgRGB = image.getRGB(0, 0, width, imageHeight, null, 0, width);
                imageResult.setRGB(0, shiftHeight, width, imageHeight, singleImgRGB, 0, width); // 写入流中
            }
            pdDocument.close();
            ImageIO.write(imageResult, "jpg", imgPath);// 写图片
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**pdf 一页转一张图片
     * @param pdfPath
     *//*
	public static void pdfToPageImage(String pdfPath){
		try{
			if(pdfPath==null||"".equals(pdfPath)||!pdfPath.endsWith(".pdf"))
				return;
	    	    //图像合并使用参数
		        int width = 0; // 总宽度
		        int[] pageImgRGB; // 保存一张图片中的RGB数据
		        int shiftHeight = 0;
		        BufferedImage imageResult = null;//保存每张图片的像素值
		        //利用PdfBox生成图像
		        PDDocument pdDocument = PDDocument.load(new File(pdfPath));
		        PDFRenderer renderer = new PDFRenderer(pdDocument);
		       //循环每个页码
		       for(int i=0,len=pdDocument.getNumberOfPages(); i<len; i++){
		         BufferedImage image=renderer.renderImageWithDPI(i, DEFAULT_DPI,ImageType.RGB);
			     int imageHeight=image.getHeight();
			     int imageWidth=image.getWidth();
		         if(i==0){//计算高度和偏移量
		        	 width=imageWidth;//使用第一张图片宽度; 
		        	 //保存每页图片的像素值
		        	 imageResult= new BufferedImage(width, imageHeight*len, BufferedImage.TYPE_INT_RGB);
		         }else{
		        	 shiftHeight += imageHeight; // 计算偏移高度
		         }
		         imageResult.setRGB(0, shiftHeight, width, imageHeight, pageImgRGB, 0, width); // 写入流中
		         File outFile = new File(pdfPath.replace(".pdf", i+".jpg"));
			     ImageIO.write(imageResult, "jpg", outFile);// 写图片
		     }
		        pdDocument.close();
		        
		     }catch (Exception e) {
		    	 e.printStackTrace();
			}
			 
	}*/
}
