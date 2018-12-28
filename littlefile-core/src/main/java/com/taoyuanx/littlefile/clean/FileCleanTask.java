package com.taoyuanx.littlefile.clean;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileCleanTask implements Runnable{
	private File file;
	private CheckDelete checkDelete;
	public static final Logger LOG=LoggerFactory.getLogger(FileCleanTask.class);
	public FileCleanTask(File file,CheckDelete delete) {
		super();
		this.file = file;
		this.checkDelete=delete;
	}

	@Override
	public void run() {
		if(checkDelete.delete(file)){
			file.delete();
			LOG.debug("删除文件{}",file);
		}
	}
	
	
	public static abstract class CheckDelete{
		public abstract boolean delete(File file);
	}
	
	/**
	 * @author 都市桃源
	 * 2018年11月13日 下午1:50:08
	 * 默认实现,删除损坏文件和历史文件
	*/
	public static class BadFileAndPeriodDelte extends CheckDelete{
		private Long cacheTime;
		
		public BadFileAndPeriodDelte(Long cacheTime) {
			super();
			this.cacheTime = cacheTime;
		}
		@Override
		public boolean delete(File file) {
			try {
				if(file.getTotalSpace()<1000){
					return true;
				}
				if(System.currentTimeMillis()-file.lastModified()>cacheTime){
					return true;
				}
				return true;
			} catch (Exception e) {
				return false;
			}
		}
		
	}

}
