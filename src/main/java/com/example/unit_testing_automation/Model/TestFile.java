package com.example.unit_testing_automation.Model;

import jakarta.persistence.*;


@Entity
@Table(name = "test_file")
public class TestFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String className;
    private String methodName;
    private String testStatus;

    public TestFile(String className, String methodName, String testStatus) {
        this.methodName = methodName;
        this.testStatus = testStatus;
        this.className = className;
    }

    public TestFile(){}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getTestStatus() {
        return testStatus;
    }

    public void setTestStatus(String testStatus) {
        this.testStatus = testStatus;
    }
}
