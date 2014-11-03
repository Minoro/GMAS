/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidor;

/**
 *
 * @author mastelini
 */
public class Teste {
    public static void main(String[] args) throws InterruptedException {
        long t1 = System.nanoTime();
        Thread.sleep(2000);
        System.out.println((System.nanoTime() - t1)/1000000000);
    }
}
