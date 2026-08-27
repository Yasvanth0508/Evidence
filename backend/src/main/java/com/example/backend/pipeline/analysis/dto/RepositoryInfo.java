package com.example.backend.pipeline.analysis.dto;

import java.util.ArrayList;
import java.util.List;

public class RepositoryInfo {
    private String interfaceName;
    private String packageName;
    private String domainEntity;
    private String idType;
    private List<String> methods = new ArrayList<>();

    public RepositoryInfo() {}

    public RepositoryInfo(String interfaceName, String packageName, String domainEntity, String idType) {
        this.interfaceName = interfaceName;
        this.packageName = packageName;
        this.domainEntity = domainEntity;
        this.idType = idType;
    }

    public String getInterfaceName() { return interfaceName; }
    public void setInterfaceName(String interfaceName) { this.interfaceName = interfaceName; }

    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }

    public String getDomainEntity() { return domainEntity; }
    public void setDomainEntity(String domainEntity) { this.domainEntity = domainEntity; }

    public String getIdType() { return idType; }
    public void setIdType(String idType) { this.idType = idType; }

    public List<String> getMethods() { return methods; }
    public void setMethods(List<String> methods) { this.methods = methods; }
}
