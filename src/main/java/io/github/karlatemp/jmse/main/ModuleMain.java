package io.github.karlatemp.jmse.main;

import java.util.ServiceLoader;

public class ModuleMain {
    public static void main() throws Throwable {
        System.out.println("Module Main: " + ModuleMain.class.getClassLoader());
        System.out.println("Crt Module: " + ModuleMain.class.getModule());
        System.out.println("Crt Module Layer: " + ModuleMain.class.getModule().getLayer());
        new Throwable("Stack trace").printStackTrace(System.out);

        var serviceLoader = ServiceLoader.load(ServiceInterface.class);
        serviceLoader.reload();
        for (var service : serviceLoader) {
            System.out.println("Service: " + service);
            System.out.println(" `- " + service.getClass());
            System.out.println(" `- " + service.getClass().getModule());
        }
    }
}
