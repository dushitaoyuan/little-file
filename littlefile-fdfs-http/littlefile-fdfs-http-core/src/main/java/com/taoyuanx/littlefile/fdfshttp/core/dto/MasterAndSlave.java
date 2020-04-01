package com.taoyuanx.littlefile.fdfshttp.core.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
public class MasterAndSlave {
    private String master;
    private List<String> slaves;

    public MasterAndSlave() {
        super();
        this.slaves = new ArrayList<>();
    }

    public MasterAndSlave(int slaveSize) {
        super();
        this.slaves = new ArrayList<>(slaveSize);
    }

    public  void addSlave(String slaveFileId){
        slaves.add(slaveFileId);
    }


}