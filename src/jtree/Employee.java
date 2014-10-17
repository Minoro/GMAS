package jtree;

import java.util.Random;

public class Employee {

    public String name;
    public int id;
    public boolean isBoss;
    public Employee[] employees;

    public Employee(String name, boolean isBoss) {
        this.name = name;
        this.isBoss = isBoss;
        this.id = new Random(System.currentTimeMillis()).nextInt(Integer.MAX_VALUE);
    }

    @Override
    public String toString() {
        return this.name;
    }

    static String randomName() {
        String chars = "abcdefghijklmnopqrstuvwxyz";
        StringBuilder builder = new StringBuilder();
        Random r = new Random(System.currentTimeMillis());
        int length = r.nextInt(10) + 1;
        for (int i = 0; i < length; i++) {
            builder.append(chars.charAt(r.nextInt(chars.length())));
        }

        return builder.toString();
    }
}
