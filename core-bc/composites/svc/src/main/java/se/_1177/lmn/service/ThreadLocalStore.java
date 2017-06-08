package se._1177.lmn.service;

public class ThreadLocalStore {

    private static ThreadLocal<String> countyCode = new ThreadLocal<>();

    public static void setCountyCode(String county) {
        ThreadLocalStore.countyCode.set(county);
    }

    public static String getCountyCode() {
        return ThreadLocalStore.countyCode.get();
    }
}
