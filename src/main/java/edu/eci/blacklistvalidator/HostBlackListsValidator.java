package edu.eci.blacklistvalidator;

import edu.eci.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;
import edu.eci.blacklistvalidator.Threads;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hcadavid
 */
public class HostBlackListsValidator {

    private static final int BLACK_LIST_ALARM_COUNT = 5;
    private static final Logger LOG = Logger.getLogger(HostBlackListsValidator.class.getName());

    /**
     * Check the given host's IP address in all the available black lists,
     * and report it as NOT Trustworthy when such IP was reported in at least
     * BLACK_LIST_ALARM_COUNT lists, or as Trustworthy in any other case.
     * The search is not exhaustive: When the number of occurrences is equal to
     * BLACK_LIST_ALARM_COUNT, the search is finished, the host reported as
     * NOT Trustworthy, and the list of the five blacklists returned.
     * @param ipaddress suspicious host's IP address.
     * @param N number of threads to use
     * @return Blacklists numbers where the given host's IP address was found.
     */
    public List<Integer> checkHost(String ipaddress, int N) {

        LinkedList<Integer> blackListOcurrences = new LinkedList<>();
        HostBlacklistsDataSourceFacade skds = HostBlacklistsDataSourceFacade.getInstance();
        int totalServers = skds.getRegisteredServersCount();
        int checkedListsCount = 0;

        Threads.initializeSharedVariables();

        Threads[] threads = new Threads[N];
        int serversPerThread = totalServers / N;
        int remainingServers = totalServers % N;

        for (int i = 0; i < N; i++) {
            threads[i] = new Threads();

            int startServer = i * serversPerThread;
            int endServer = startServer + serversPerThread - 1;

            if (i == N - 1) {
                endServer += remainingServers;
            }

            threads[i].setInitialServer(startServer);
            threads[i].setFinalServer(endServer);
            threads[i].setIpaddress(ipaddress);
            threads[i].start();
        }

        for (Threads thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOG.log(Level.WARNING, "Thread interrupted while waiting for completion", e);
            }
        }

        for (Threads thread : threads) {
            blackListOcurrences.addAll(thread.getBlackListOcurrences());
            checkedListsCount += thread.getCheckedListsCount();
        }

        int totalOccurrences = Threads.getGlobalOccurrencesCount();

        if (totalOccurrences >= BLACK_LIST_ALARM_COUNT) {
            skds.reportAsNotTrustworthy(ipaddress);
        } else {
            skds.reportAsTrustworthy(ipaddress);
        }

        LOG.log(Level.INFO, "Checked Black Lists:{0} of {1}",
                new Object[]{checkedListsCount, totalServers});

        return blackListOcurrences;
    }
}