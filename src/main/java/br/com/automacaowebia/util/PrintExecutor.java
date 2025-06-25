package br.com.automacaowebia.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class PrintExecutor {
    public static final ExecutorService POOL =
        Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors());

    private PrintExecutor() {} 
}
