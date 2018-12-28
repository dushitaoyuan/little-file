package com.taoyuanx.littlefile.clean;

import java.io.File;
import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;

import com.taoyuanx.littlefile.clean.FileCleanTask.CheckDelete;

public class FileClean {
	private static ThreadPoolExecutor taskPool;
	private  volatile boolean run=true;
	/**
	 * dir 操作目录
	 * checkDelete 文件判断条件
	 * period 循环周期
	 */
	private File dir;
	private CheckDelete checkDelete;
	private Long period;
	private Thread thread=null;
	public FileClean(Integer num, File dir,CheckDelete checkDelete,Long period) {
		taskPool = new ThreadPoolExecutor(num, num, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
		this.dir = dir;
		this.checkDelete=checkDelete;
		this.period=period;
	}

	public void doClean(){
		while (run) {
			try {
				Thread.sleep(period);
				Collection<File> listFiles = FileUtils.listFiles(dir, FileFileFilter.FILE, DirectoryFileFilter.DIRECTORY);
				if(null==listFiles||listFiles.isEmpty()){
					continue;
				}
				for (File file : listFiles) {
					taskPool.submit(new FileCleanTask(file,checkDelete));
				}
			} catch (Exception e) {
				
			}
			
		}
		taskPool.shutdownNow();
	}
	
	public void start(){
		if(thread==null) {
			thread=new Thread(new Runnable() {
				@Override
				public void run() {
					doClean();
				}
			});
			thread.start();
		}
	}
	public void stop(){
		run=false;
	}
	
}
