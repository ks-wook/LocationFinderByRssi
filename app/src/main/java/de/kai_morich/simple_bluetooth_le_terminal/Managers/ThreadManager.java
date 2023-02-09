package de.kai_morich.simple_bluetooth_le_terminal.Managers;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

// 쓰레드풀에서 쓰레드를 끌어다 쓰기 위한 Manager 필요시 메소드 구현 가능
// 최대 쓰레드 개수는 필요시 조절
public class ThreadManager {

    private static final int MaxThread = 10;

    static ThreadPoolExecutor _threadPool =
            (ThreadPoolExecutor) Executors.newFixedThreadPool(MaxThread);

    public static void startThread(Thread thread)
    {
        _threadPool.submit(thread);
    }

}
