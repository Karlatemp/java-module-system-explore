package io.github.karlatemp.jmse.boot;

import java.net.URL;
import java.net.URLClassLoader;

public class MyCustomClassLoader extends URLClassLoader {
    String moduleName;

    public MyCustomClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    @Override
    protected Class<?> findClass(String moduleName, String name) {
        // System.out.println("Find class: " + moduleName + "/" + name);
        if (this.moduleName.equals(moduleName)) {
            try {
                return findClass(name);
            } catch (ClassNotFoundException ignored) {
            }
        }
        return super.findClass(moduleName, name);
    }
}
