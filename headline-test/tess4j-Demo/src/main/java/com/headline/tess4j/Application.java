package com.headline.tess4j;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class Application {
    //识别图片中的文字
    public static void main(String[] args) throws TesseractException, MalformedURLException {
        //创建实例
        ITesseract tesseract = new Tesseract();
        //设置值
        //设置字体库路径
        tesseract.setDatapath("D:\\workspace\\tessdata");
        //设置语言
        tesseract.setLanguage("chi_sim");
        File file = new File("E:\\image-20210524161243572.png");
        //识别图片
        String result = tesseract.doOCR(file);
        System.out.println("识别的结果为"+result.replaceAll("\\r|\\n","-"));
    }
}
