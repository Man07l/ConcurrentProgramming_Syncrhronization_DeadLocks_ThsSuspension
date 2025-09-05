package edu.eci.blacklistvalidator;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;
import edu.eci.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;

public class Threads extends Thread {

    private int initialServer;
    private int finalServer;
    private String ipaddress;
    private LinkedList<Integer> blackListOcurrences;
    private static final int BLACK_LIST_ALARM_COUNT = 5;
    private int checkedListsCount;

    private static AtomicInteger globalOccurrencesCount;
    private static AtomicBoolean stopAll;

    public void setInitialServer(int initialServer) {
        this.initialServer = initialServer;
    }

    public void setFinalServer(int finalServer) {
        this.finalServer = finalServer;
    }

    public void setIpaddress(String ipaddress) {
        this.ipaddress = ipaddress;
    }

    public LinkedList<Integer> getBlackListOcurrences() {
        return blackListOcurrences;
    }

    public int getCheckedListsCount() {
        return checkedListsCount;
    }

    public static void initializeSharedVariables() {
        globalOccurrencesCount = new AtomicInteger(0);
        stopAll = new AtomicBoolean(false);
    }

    public static int getGlobalOccurrencesCount() {
        return globalOccurrencesCount.get();
    }

    public static boolean shouldStop() {
        return stopAll.get();
    }

    @Override
    public void run() {
        blackListOcurrences = new LinkedList<>();
        checkedListsCount = 0;
        HostBlacklistsDataSourceFacade skds = HostBlacklistsDataSourceFacade.getInstance();

        for (int i = initialServer; i <= finalServer && !shouldStop(); i++) {
            checkedListsCount++;

            if (skds.isInBlackListServer(i, ipaddress)) {
                blackListOcurrences.add(i);

                int currentCount = globalOccurrencesCount.incrementAndGet();

                if (currentCount >= BLACK_LIST_ALARM_COUNT) {
                    stopAll.set(true);
                    break;
                }
            }
        }
    }
}