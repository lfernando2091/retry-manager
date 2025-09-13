package com.lfernando.retrymanager;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        String  res = RetryManager.supply(() -> getValue())
                .accept(it -> it.contains("2"))
                .delay(Duration.ofSeconds(5))
                .timeout(Duration.ofSeconds(5))
                .get();
        System.out.println(res);
    }

    private static String getValue(){
        try {
            TimeUnit.MILLISECONDS.sleep(5000);
        } catch (java.lang.InterruptedException e) {

        }
        return "1";
    }
}
