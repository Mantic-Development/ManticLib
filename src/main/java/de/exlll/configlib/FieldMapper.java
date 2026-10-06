package de.exlll.configlib;

import de.exlll.configlib.Converter.ConversionInfo;
import de.exlll.configlib.annotation.Comment;
import de.exlll.configlib.annotation.Format;
import de.exlll.configlib.annotation.Optional;
import de.exlll.configlib.filter.FieldFilter;
import de.exlll.configlib.format.FieldNameFormatter;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;

import static de.exlll.configlib.Validator.*;

enum FieldMapper {
    ;

    static Map<String, Object> instanceToMap(Object inst, MappingInfo mappingInfo) {
        if (Reflect.isConfigurationElement(inst.getClass())
                && !mappingInfo.hasOptionalDefaults(inst)) {
            Object defaults = Reflect.newInstance(inst.getClass());
            captureOptionalDefaults(defaults, mappingInfo);
            mappingInfo.copyOptionalDefaults(defaults, inst);
        }
        CommentedMap map = new CommentedMap();
        Configuration.Properties props = mappingInfo.getProperties();
        FieldFilter filter = props.getFilter();
        for (Field field : filter.filterDeclaredFieldsOf(inst.getClass())) {
            Object val = toConvertibleObject(field, inst, mappingInfo);
            String fn = selectFormatter(mappingInfo).fromFieldName(field.getName());
            if (field.isAnnotationPresent(Optional.class)
                    && mappingInfo.hasOptionalDefault(inst, field)
                    && Objects.deepEquals(val, mappingInfo.getOptionalDefault(inst, field))) {
                continue;
            }
            map.put(fn, val);
            Comment comment = field.getAnnotation(Comment.class);
            if (comment != null) {
                map.getFieldComments().put(fn, Arrays.asList(comment.value()));
            }
        }
        return map;
    }

    static void captureOptionalDefaults(Object inst, MappingInfo mappingInfo) {
        if (mappingInfo.hasOptionalDefaults(inst)) {
            return;
        }

        List<? extends Field> fields = mappingInfo.getProperties().getFilter()
                .filterDeclaredFieldsOf(inst.getClass());
        Map<Field, Object> defaults = new HashMap<>();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Optional.class)) {
                defaults.put(field, snapshot(toConvertibleObject(field, inst, mappingInfo)));
            }
        }
        mappingInfo.putOptionalDefaults(inst, defaults);
    }

    private static Object snapshot(Object value) {
        if (value instanceof Map<?, ?>) {
            Map<Object, Object> copy = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                copy.put(snapshot(entry.getKey()), snapshot(entry.getValue()));
            }
            return copy;
        }
        if (value instanceof List<?>) {
            List<Object> copy = new ArrayList<>();
            for (Object element : (List<?>) value) {
                copy.add(snapshot(element));
            }
            return copy;
        }
        if (value instanceof Set<?>) {
            Set<Object> copy = new LinkedHashSet<>();
            for (Object element : (Set<?>) value) {
                copy.add(snapshot(element));
            }
            return copy;
        }
        if (value != null && value.getClass().isArray()) {
            int length = Array.getLength(value);
            Object copy = Array.newInstance(value.getClass().getComponentType(), length);
            for (int i = 0; i < length; i++) {
                Array.set(copy, i, snapshot(Array.get(value, i)));
            }
            return copy;
        }
        return value;
    }

    static void retainOptionalDefaults(Object inst, MappingInfo mappingInfo) {
        IdentityHashMap<Object, Boolean> reachable = new IdentityHashMap<>();
        collectReachable(inst, mappingInfo, reachable);
        mappingInfo.retainOptionalDefaults(reachable);
    }

    private static void collectReachable(
            Object inst, MappingInfo mappingInfo,
            IdentityHashMap<Object, Boolean> reachable
    ) {
        if (inst == null || reachable.put(inst, Boolean.TRUE) != null) {
            return;
        }
        for (Field field : mappingInfo.getProperties().getFilter()
                .filterDeclaredFieldsOf(inst.getClass())) {
            if (!Reflect.hasNoConvert(field) && !Reflect.hasConverter(field)) {
                collectNestedReachable(Reflect.getValue(field, inst), mappingInfo, reachable);
            }
        }
    }

    private static void collectNestedReachable(
            Object value, MappingInfo mappingInfo,
            IdentityHashMap<Object, Boolean> reachable
    ) {
        if (value == null || reachable.containsKey(value)) {
            return;
        }
        if (value instanceof Map<?, ?>) {
            reachable.put(value, Boolean.TRUE);
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                collectNestedReachable(entry.getKey(), mappingInfo, reachable);
                collectNestedReachable(entry.getValue(), mappingInfo, reachable);
            }
        } else if (value instanceof Iterable<?>) {
            reachable.put(value, Boolean.TRUE);
            for (Object element : (Iterable<?>) value) {
                collectNestedReachable(element, mappingInfo, reachable);
            }
        } else if (Reflect.isConfigurationElement(value.getClass())) {
            collectReachable(value, mappingInfo, reachable);
        }
    }

    private static Object toConvertibleObject(
            Field field, Object instance, MappingInfo mappingInfo
    ) {
        checkDefaultValueNull(field, instance);
        ConversionInfo info = ConversionInfo.from(field, instance, mappingInfo);
        checkFieldWithElementTypeIsContainer(info);
        Object converted = Converters.convertTo(info);
        checkConverterNotReturnsNull(converted, info);
        return converted;
    }

    static void instanceFromMap(
            Object inst, Map<String, Object> instMap, MappingInfo mappingInfo
    ) {
        FieldFilter filter = mappingInfo.getProperties().getFilter();
        for (Field field : filter.filterDeclaredFieldsOf(inst.getClass())) {
            FieldNameFormatter fnf = selectFormatter(mappingInfo);
            String fn = fnf.fromFieldName(field.getName());
            Object mapValue = instMap.get(fn);
            if (mapValue != null) {
                fromConvertedObject(field, inst, mapValue, mappingInfo);
            }
        }
    }

    private static void fromConvertedObject(
            Field field, Object instance, Object mapValue,
            MappingInfo mappingInfo
    ) {
        checkDefaultValueNull(field, instance);
        ConversionInfo info = ConversionInfo.from(
                field, instance, mapValue, mappingInfo
        );
        checkFieldWithElementTypeIsContainer(info);
        Object convert = Converters.convertFrom(info);

        if (convert == null) {
            return;
        }

        if (Reflect.isContainerType(info.getFieldType())) {
            checkFieldTypeAssignableFrom(convert.getClass(), info);
        }

        Reflect.setValue(field, instance, convert);
    }

    private static void checkDefaultValueNull(Field field, Object instance) {
        Object val = Reflect.getValue(field, instance);
        checkNotNull(val, field.getName());
    }

    static FieldNameFormatter selectFormatter(MappingInfo info) {
        Configuration<?> configuration = info.getConfiguration();
        Configuration.Properties props = info.getProperties();
        if ((configuration != null) &&
                Reflect.hasFormatter(configuration.getClass())) {
            Format format = configuration.getClass()
                    .getAnnotation(Format.class);
            return (format.formatterClass() != FieldNameFormatter.class)
                    ? Reflect.newInstance(format.formatterClass())
                    : format.value();
        }
        return props.getFormatter();
    }

    static final class MappingInfo {
        private final Configuration<?> configuration;
        private final Configuration.Properties properties;
        private final OptionalDefaults optionalDefaults;

        MappingInfo(
                Configuration<?> configuration,
                Configuration.Properties properties,
                OptionalDefaults optionalDefaults
        ) {
            this.configuration = configuration;
            this.properties = properties;
            this.optionalDefaults = optionalDefaults;
        }

        Configuration<?> getConfiguration() {
            return configuration;
        }

        Configuration.Properties getProperties() {
            return properties;
        }

        boolean hasOptionalDefault(Object instance, Field field) {
            Map<Field, Object> defaults = optionalDefaults.values.get(instance);
            return defaults != null && defaults.containsKey(field);
        }

        Object getOptionalDefault(Object instance, Field field) {
            return optionalDefaults.values.get(instance).get(field);
        }

        void putOptionalDefaults(Object instance, Map<Field, Object> defaults) {
            optionalDefaults.values.put(instance, defaults);
        }

        void copyOptionalDefaults(Object source, Object target) {
            Map<Field, Object> defaults = optionalDefaults.values.get(source);
            optionalDefaults.values.put(target, new HashMap<>(defaults));
        }

        boolean hasOptionalDefaults(Object instance) {
            return optionalDefaults.values.containsKey(instance);
        }

        void retainOptionalDefaults(IdentityHashMap<Object, Boolean> reachable) {
            optionalDefaults.values.keySet().removeIf(instance -> !reachable.containsKey(instance));
        }

        static MappingInfo from(Configuration<?> configuration) {
            return new MappingInfo(configuration, configuration.getProperties(),
                    configuration.getOptionalDefaults());
        }
    }

    static final class OptionalDefaults {
        private final IdentityHashMap<Object, Map<Field, Object>> values =
                new IdentityHashMap<>();
    }
}
