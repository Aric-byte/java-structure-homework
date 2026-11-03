package com.Spring.Ioc.xml;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class Person {

    private String name;
    private int age;
    private List<String> hobbies;//链式注入 数组
    private Set<String> skills; //堆集合 去重复
    private Map<String, Double> scores; //map 键值对
    private Properties healthInfo;

    public Person() {
    }

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public List<String> getHobbies() {
        return hobbies;
    }

    public void setHobbies(List<String> hobbies) {
        this.hobbies = hobbies;
    }

    public Set<String> getSkills() {
        return skills;
    }

    public void setSkills(Set<String> skills) {
        this.skills = skills;
    }

    public Map<String, Double> getScores() {
        return scores;
    }

    public void setScores(Map<String, Double> scores) {
        this.scores = scores;
    }

    public Properties getHealthInfo() {
        return healthInfo;
    }

    public void setHealthInfo(Properties healthInfo) {
        this.healthInfo = healthInfo;
    }

    @Override
    public String toString() {
        return "Person{"
                + "name='" + name + '\''
                + ", age=" + age
                + ", hobbies=" + hobbies
                + ", skills=" + skills
                + ", scores=" + scores
                + ", healthInfo=" + healthInfo
                + '}';
    }
}