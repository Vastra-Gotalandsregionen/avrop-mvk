package se._1177.lmn.model;

import riv.crm.selfservice.medicalsupply._2.CVType;

public class ServicePointProvider implements Comparable<ServicePointProvider> {

    public static final ServicePointProvider INGEN = new ServicePointProvider("INGEN");
    private String name;

    private ServicePointProvider(String name) {
        this.name = name;
    }

    public static ServicePointProvider from(CVType cvType) {
        return new ServicePointProvider(cvType.getOriginalText());
    }

    public static ServicePointProvider from(String name) {
        return new ServicePointProvider(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServicePointProvider that = (ServicePointProvider) o;

        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public int compareTo(ServicePointProvider o) {
        return this.getName().compareTo(o.getName());
    }
}
