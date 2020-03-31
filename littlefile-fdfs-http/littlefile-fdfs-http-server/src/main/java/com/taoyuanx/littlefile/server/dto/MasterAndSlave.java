package com.taoyuanx.littlefile.server.dto;

import java.util.ArrayList;
import java.util.List;

public  class MasterAndSlave{
		private String master;
		private List<String> slaves;
		public MasterAndSlave() {
			super();
		}
		private MasterAndSlave(int slaveSize) {
			super();
			this.slaves = new ArrayList<>(slaveSize);
		}
		public String getMaster() {
			return master;
		}
		public void setMaster(String master) {
			this.master = master;
		}
		public List<String> getSlaves() {
			return slaves;
		}
		public void setSlaves(List<String> slaves) {
			this.slaves = slaves;
		}
		public void addSlave(String slave){
			this.slaves.add(slave);
		}
		public static MasterAndSlave create(int slaveSize){
			return new MasterAndSlave( slaveSize);
		}
		
	}