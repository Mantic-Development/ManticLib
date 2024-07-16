package me.fullpage.manticlib.utils.despical.jvm;

import me.fullpage.manticlib.utils.despical.jvm.classes.ClassHandle;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.Objects;

public class FieldMemberHandleJava8 extends FieldMemberHandle {

    private static final MethodHandle MODIFIERS_FIELD;

    static {
        MethodHandle modifierFieldJvm = null;
        try {
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifierFieldJvm = MethodHandles.lookup().unreflectSetter(modifiersField);
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
        }
        MODIFIERS_FIELD = modifierFieldJvm;
    }

    protected Boolean getter;

    public FieldMemberHandleJava8(ClassHandle clazz) {
        super(clazz);
    }

    @Override
    public FieldMemberHandleJava8 named(String... names) {
        super.named(names);
        return this;
    }

    public FieldMemberHandleJava8 getter() {
        this.getter = true;
        return this;
    }

    public FieldMemberHandleJava8 asStatic() {
        super.asStatic();
        return this;
    }

    public FieldMemberHandleJava8 asFinal() {
        this.isFinal = true;
        return this;
    }

    public FieldMemberHandleJava8 setter() {
        this.getter = false;
        return this;
    }

    @Override
    public FieldMemberHandleJava8 returns(Class<?> clazz) {
        super.returns(clazz);
        return this;
    }

    @Override
    public FieldMemberHandleJava8 returns(ClassHandle clazz) {
        super.returns(clazz);
        return this;
    }

    @Override
    public MethodHandle reflect() throws ReflectiveOperationException {
        Field jvm = reflectJvm();
        if (getter) {
            return lookup.unreflectGetter(jvm);
        } else {
            return lookup.unreflectSetter(jvm);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T extends AccessibleObject & Member> T handleAccessible(T field) throws ReflectiveOperationException {
        field = super.handleAccessible(field);
        if (field == null) return null;
        if (isFinal && isStatic) {
            try {
                int unfinalModifiers = field.getModifiers() & ~Modifier.FINAL;
                if (MODIFIERS_FIELD != null) {
                    MODIFIERS_FIELD.invoke(field, unfinalModifiers);
                } else {
                    throw new IllegalAccessException("Current Java version doesn't support modifying final fields. " + this);
                }
            } catch (Throwable e) {
                throw new ReflectiveOperationException("Cannot unfinal field " + this, e);
            }
        }
        return field;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Field reflectJvm() throws ReflectiveOperationException {
        Objects.requireNonNull(returnType, "Return type not specified");
        Objects.requireNonNull(getter, "Not specified whether the method is a getter or setter");
        if (names.isEmpty()) throw new IllegalStateException("No names specified");
        NoSuchFieldException errors = null;
        Field field = null;

        Class<?> clazz = this.clazz.unreflect();
        for (String name : this.names) {
            if (field != null) break;
            try {
                field = clazz.getDeclaredField(name);
                if (field.getType() != this.returnType) {
                    throw new NoSuchFieldException("Field named '" + name + "' was found but the types don't match: " + field + " != " + this);
                }
                if (isFinal && !Modifier.isFinal(field.getModifiers())) {
                    throw new NoSuchFieldException("Field named '" + name + "' was found but it's not final: " + field + " != " + this);
                }
            } catch (NoSuchFieldException ex) {
                field = null;
                if (errors == null) errors = new NoSuchFieldException("None of the fields were found for " + this);
                errors.addSuppressed(ex);
            }
        }

        if (field == null) throw errors;
        return handleAccessible(field);
    }

    @Override
    public String toString() {
        String str = this.getClass().getSimpleName() + '{';
        if (makeAccessible) str += "protected/private ";
        if (isFinal) str += "final ";
        if (returnType != null) str += returnType.getSimpleName() + ' ';
        str += String.join("/", names);
        return str + '}';
    }
}