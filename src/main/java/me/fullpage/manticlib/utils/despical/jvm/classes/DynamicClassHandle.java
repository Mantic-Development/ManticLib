package me.fullpage.manticlib.utils.despical.jvm.classes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * <a href="https://github.com/CryptoMorin/XSeries/tree/master/src/main/java/com/cryptomorin/xseries/reflection">Original Source Code</a>
 *
 * @author CryptoMorin
 * @author Despical
 * <p>
 * Created at 23.05.2024
 */
public class DynamicClassHandle extends ClassHandle {

    protected String packageName;
    protected final List<String> classNames = new ArrayList<>(5);
    protected int array;

    public DynamicClassHandle inPackage(String packageName) {
        this.packageName = packageName;
        return this;
    }

    public DynamicClassHandle named(String... clazzNames) {
        this.classNames.addAll(Arrays.asList(clazzNames));
        return this;
    }

    public String[] reflectClassNames() {
        Objects.requireNonNull(packageName, "Package name is null");
        String[] classNames = new String[this.classNames.size()];

        for (int i = 0; i < this.classNames.size(); i++) {
            @SuppressWarnings("NonConstantStringShouldBeStringBuffer")
            String clazz = packageName + '.' + this.classNames.get(i);
            if (array != 0) clazz = repeat("[", array) + 'L' + clazz + ';';

            classNames[i] = clazz;
        }

        return classNames;
    }

    @Override
    public Class<?> reflect() throws ClassNotFoundException {
        ClassNotFoundException errors = null;

        for (String className : reflectClassNames()) {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException ex) {
                if (errors == null) errors = new ClassNotFoundException("None of the classes were found");
                errors.addSuppressed(ex);
            }
        }

        throw errors;
    }

    @Override
    public DynamicClassHandle asArray(int dimension) {
        if (dimension < 0) throw new IllegalArgumentException("Array dimension cannot be negative: " + dimension);
        this.array = dimension;
        return this;
    }

    @Override
    public boolean isArray() {
        return this.array > 0;
    }

    private String repeat(String string, int count) {
        if (count <= 1) {
            return count == 0 ? "" : string;
        } else {
            int len = string.length();
            long longSize = (long) len * (long) count;
            int size = (int) longSize;

            if ((long) size != longSize) {
                throw new ArrayIndexOutOfBoundsException("Required array size too large: " + longSize);
            } else {
                char[] array = new char[size];
                string.getChars(0, len, array, 0);
                int n;

                for (n = len; n < size - n; n <<= 1) {
                    System.arraycopy(array, 0, array, n, n);
                }

                System.arraycopy(array, 0, array, n, size - n);
                return new String(array);
            }
        }
    }
}