package com.taoyuanx.littlefile.server.dto;

/**
 * @author dushitaoyuan
 * 预览枚举 ,如果有别的预览需求,可自行探索
 *  这里提出思想: zip,tar,gzip等压缩包预览,可先下载临时文件到本地,解压缩,然后逐项预览
 *  word,ppt,等办公文档可借助openoffice 转成html或者(办公->html, 办公->pdf->img)
 *  pdf 在线预览 也可参考pdf.js 支持移动端 注意签名签章丢失问题 
 */
public enum PreviewType {
	PDF_TO_IMG(1);//pdf转图片查看类型;
	public Integer code;
	private PreviewType(Integer code) {
		this.code = code;
	}
	
	public static PreviewType getByCode(Integer code){
		for(PreviewType p:PreviewType.values()){
			if(p.code.equals(code)){
				return p;
			}
		}
		return null;
	}
}
