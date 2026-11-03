package com.mybatis02.entity;

public class Vo {

    private Integer p1;
    private String p2;
    private String p3;

    public Vo() {
    }

    public Vo(Integer p1, String p2, String p3) {
        this.p1 = p1;
        this.p2 = p2;
        this.p3 = p3;
    }

    public Integer getP1() {
        return p1;
    }

    public void setP1(Integer p1) {
        this.p1 = p1;
    }

    public String getP2() {
        return p2;
    }

    public void setP2(String p2) {
        this.p2 = p2;
    }

    public String getP3() {
        return p3;
    }

    public void setP3(String p3) {
        this.p3 = p3;
    }

    @Override
    public String toString() {
        return "Vo{" +
                "p1=" + p1 +
                ", p2='" + p2 + '\'' +
                ", p3='" + p3 + '\'' +
                '}';
    }
}