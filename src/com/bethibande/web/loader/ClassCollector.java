package com.bethibande.web.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class ClassCollector {

    private Function<String, Boolean> filter;

    private final Collection<Class<?>> classes = new ArrayList<>();

    public static class AnnotationFilter implements Function<String, Boolean> {

        private final Class<? extends Annotation> annotation;

        public AnnotationFilter(Class<? extends Annotation> annotation) {
            this.annotation = annotation;
        }

        @Override
        public Boolean apply(String s) {
            try {
                return Class.forName(s).isAnnotationPresent(annotation);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static String pathToClassPath(String path) {
        return path.replace('\\', '.').replace(".class", "");
    }

    private void handleJarFile(File file) throws IOException, ClassNotFoundException {
        JarInputStream is = new JarInputStream(new FileInputStream(file));

        JarEntry entry;
        while( (entry = is.getNextJarEntry()) != null) {
            if(entry.getName().endsWith(".class")) {
                String classPath = pathToClassPath(entry.getName());
                if(!filter.apply(classPath)) continue;

                classes.add(Class.forName(classPath));
            }
        }
    }

    private void handleDirectory(File root, File dir) throws IOException, ClassNotFoundException {
        for(File file : Objects.requireNonNull(dir.listFiles())) {
            if(file.isFile()) {
                if(file.getName().endsWith(".class")) handleClassFile(root, file);
                if(file.getName().endsWith(".jar")) handleJarFile(file);
                continue;
            }

            handleDirectory(root, file);
        }
    }

    private void handleClassFile(File root, File file) throws ClassNotFoundException {
        String classFile = pathToClassPath(root.toPath().relativize(file.toPath()).toString());
        if(!filter.apply(classFile)) return;

        classes.add(Class.forName(classFile));
    }

    private void clear() {
        classes.clear();
    }

    public Collection<Class<?>> collect(Class<?> clazz, Class<? extends Annotation> annotation) {
        return collect(clazz, new AnnotationFilter(annotation));
    }

    public synchronized Collection<Class<?>> collect(Class<?> clazz, Function<String, Boolean> filter) {
        this.filter = filter;
        clear();

        try {
            File file = new File(clazz.getProtectionDomain().getCodeSource().getLocation().toURI());

            if (file.isFile()) {
                if (file.getName().endsWith(".class")) handleClassFile(file, file);
                if (file.getName().endsWith(".jar")) handleJarFile(file);
                return classes;
            }

            handleDirectory(file, file);
        } catch(IOException | URISyntaxException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return classes;
    }

}
