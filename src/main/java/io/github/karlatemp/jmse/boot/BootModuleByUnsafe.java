package io.github.karlatemp.jmse.boot;

import io.github.karlatemp.unsafeaccessor.Root;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodType;
import java.lang.module.ModuleDescriptor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BootModuleByUnsafe {
    public static Set<String> readPkgs() throws IOException {
        try (var stream = Files.walk(
                        Path.of("build", "classes", "java", "main")
                )
                .filter(it -> !Files.isDirectory(it))
                .map(Path::getParent)
                .map(it -> it.subpath(4, it.getNameCount()))
        ) {
            var pkgs = new HashSet<String>();
            var iter = stream.iterator();
            while (iter.hasNext()) {
                pkgs.add(iter.next().toString()
                        .replace(File.separatorChar, '.')
                );
            }
            pkgs.remove("io.github.karlatemp.jmse.boot");
            return pkgs;
        }
    }

    public static void boot(ClassLoader classLoader) throws Throwable {
        Thread.currentThread().setContextClassLoader(classLoader);
        var c = Class.forName("io.github.karlatemp.jmse.main.ModuleMain", false, classLoader);
        Root.getTrusted(c)
                .findStatic(c, "main", MethodType.methodType(void.class))
                .invoke();
    }

    public static ModuleDescriptor newMD() throws Throwable {
        return ModuleDescriptor.newModule("my.custom_module")
                .packages(readPkgs())
                .uses("io.github.karlatemp.jmse.main.ServiceInterface")
                .provides("io.github.karlatemp.jmse.main.ServiceInterface", List.of(
                        "io.github.karlatemp.jmse.main.ServiceImpl"
                ))
                .build();
    }

    public static void main(String[] args) throws Throwable {
//        var usf = Unsafe.getUnsafe();
        var MODULE_ACCESS = Root.getModuleAccess();
        var module = MODULE_ACCESS.defineModule(
                BootModuleByUnsafe.class.getClassLoader(),
                newMD(),
                null
        );
        MODULE_ACCESS.addReads(module, Object.class.getModule());
        boot(ClassLoader.getSystemClassLoader());
    }
}
