package io.github.karlatemp.jmse.boot;

import io.github.karlatemp.unsafeaccessor.Unsafe;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReader;
import java.lang.module.ModuleReference;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class BootModuleByStandard {
    public static class MyModuleReader implements ModuleReader {

        @Override
        public Optional<URI> find(String name) throws IOException {
            return Optional.empty();
        }

        @Override
        public Optional<InputStream> open(String name) throws IOException {
            File file = new File("build/classes/java/main", name);
            if (file.isFile()) {
                return Optional.of(new FileInputStream(file));
            }
            return Optional.empty();
        }

        @Override
        public Stream<String> list() throws IOException {
            return Stream.of();
        }

        @Override
        public void close() throws IOException {
        }
    }

    public static void main(String[] args) throws Throwable {
        var moduleDescriptor = BootModuleByUnsafe.newMD();

        var myModuleReference = new ModuleReference(
                moduleDescriptor, null
        ) {
            @Override
            public ModuleReader open() throws IOException {
                return new MyModuleReader();
            }
        };
        var myModuleFinder = new ModuleFinder() {
            @Override
            public Optional<ModuleReference> find(String name) {
                if (name.equals(moduleDescriptor.name())) {
                    return Optional.of(myModuleReference);
                }
                return Optional.empty();
            }

            @Override
            public Set<ModuleReference> findAll() {
                return Set.of(myModuleReference);
            }
        };
        var bootLayer = ModuleLayer.boot();
        var myConfiguration = bootLayer.configuration().resolve(
                myModuleFinder, ModuleFinder.of(), Set.of(moduleDescriptor.name())
        );
        /*
        var classLoader = new MyCustomClassLoader(new URL[]{
                new File("build/classes/java/main").toURI().toURL()
        }, ClassLoader.getSystemClassLoader().getParent());
        classLoader.moduleName = moduleDescriptor.name();
        var controller = ModuleLayer.defineModules(
                myConfiguration,
                List.of(bootLayer),
                $ -> classLoader
        );
        */
        var controller = ModuleLayer.defineModulesWithOneLoader(
                myConfiguration,
                List.of(bootLayer),
                ClassLoader.getSystemClassLoader().getParent()
        );
        var classLoader = controller.layer().findLoader(moduleDescriptor.name());

        {
            var usf = Unsafe.getUnsafe();
            var offset = usf.objectFieldOffset(ClassLoader.class, "name");
            usf.putReference(classLoader, offset, "ModuleCCL");
        }

        BootModuleByUnsafe.boot(classLoader);
    }
}
