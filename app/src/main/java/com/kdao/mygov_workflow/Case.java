package com.kdao.mygov_workflow;

/**
 * Created by YuexingYin on 10/04/2017.
 */

public class Case {
    private String caseID;
    private String caseName;
    private String status;
    private String comment;

    public Case(String caseName, String status, String comment){
        this.caseName = caseName;
        this.status = status;
        this.comment = comment;
    }

    public String getCaseID() {
        return caseID;
    }

    public void setCaseID(String caseID) {
        this.caseID = caseID;
    }

    public String getCaseName() {
        return caseName;
    }

    public void setCaseName(String caseName) {
        this.caseName = caseName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
