package com.Spring.Ioc.Annotation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

@Component("personRich")
public class Person {

    @Value("张三")
    private String name;

    @Value("25")
    private int age;

    private List<String> hobbies;
    private Set<String> skills;
    private Map<String, Double> scores;
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

    @Autowired
    public void setHobbies(@Qualifier("hobbies") List<String> hobbies) {
        this.hobbies = hobbies;
    }

    public Set<String> getSkills() {
        return skills;
    }

    @Autowired
    public void setSkills(@Qualifier("skills") Set<String> skills) {
        this.skills = skills;
    }

    public Map<String, Double> getScores() {
        return scores;
    }

    @Autowired
    public void setScores(@Qualifier("scores") Map<String, Double> scores) {
        this.scores = scores;
    }

    public Properties getHealthInfo() {
        return healthInfo;
    }

    @Autowired
    public void setHealthInfo(@Qualifier("healthInfo") Properties healthInfo) {
        this.healthInfo = healthInfo;
    }

    @PostConstruct
    public void init() {
        System.out.println("  [Person] ★ @PostConstruct 初始化 -> " + name);
    }

    @PreDestroy
    public void destroy() {
        System.out.println("  [Person] ★ @PreDestroy 销毁 -> " + name);
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