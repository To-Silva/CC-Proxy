/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientmonitorudp;

/**
 *
 * @author To_si
 */
public class Benchmark implements Runnable {
    static long fibo(int n) {
        if (n < 2) {
            return n;
        } else {
            return fibo(n - 1) + fibo(n - 2);
        }
    }

    @Override
    public void run() {
        System.out.print("Performing benchmark");
	for (int i = 0; i < 42; i++) {
            if (i==39||i==40){System.out.print(".");}
            Benchmark.fibo(i);
        }
        System.out.println(".\nFinished.");
    }
}
