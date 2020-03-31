package com.taoyuanx.littlefile.server.fdfs;

import java.util.Arrays;

/**
 * 
 * @author 都市桃源
 * 2018年11月13日 下午3:23:28
 *
*/
public class MasterSlave {
	/**
	 * masterId 主文件id
	 * slaves 从文件id数组
	 */
	private String masterId;
	private String[] slaves;
	public String getMasterId() {
		return masterId;
	}
	public void setMasterId(String masterId) {
		this.masterId = masterId;
	}
	public String[] getSlaves() {
		return slaves;
	}
	public void setSlaves(String[] slaves) {
		this.slaves = slaves;
	}
	@Override
	public String toString() {
		return "MasterSlave [masterId=" + masterId + ", slaves=" + Arrays.toString(slaves) + "]";
	}
	
}
