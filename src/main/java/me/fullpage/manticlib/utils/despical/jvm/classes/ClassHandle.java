package me.fullpage.manticlib.utils.despical.jvm.classes;

import me.fullpage.manticlib.utils.despical.Handle;
import me.fullpage.manticlib.utils.despical.jvm.ConstructorMemberHandle;
import me.fullpage.manticlib.utils.despical.jvm.FieldMemberHandle;
import me.fullpage.manticlib.utils.despical.jvm.MethodMemberHandle;

/**
 * <a href="https://github.com/CryptoMorin/XSeries/tree/master/src/main/java/com/cryptomorin/xseries/reflection">Original Source Code</a>
 *
 * @author CryptoMorin
 * @author Despical
 * <p>
 * Created at 23.05.2024
 */
public abstract class ClassHandle implements Handle<Class<?>> {

	public abstract ClassHandle asArray(int dimensions);

	public final ClassHandle asArray() {
		return asArray(1);
	}

	public abstract boolean isArray();

	public int getDimensionCount() {
		int count = -1;
		Class<?> clazz = unreflect();
		if (clazz == null) return count;

		do {
			clazz = clazz.getComponentType();
			count++;
		} while (clazz != null);

		return count;
	}

	public MethodMemberHandle method() {
		return new MethodMemberHandle(this);
	}

	public FieldMemberHandle field() {
		return new FieldMemberHandle(this);
	}

	public FieldMemberHandle getterField() {
		return field().getter();
	}

	public FieldMemberHandle setterField() {
		return field().setter();
	}

	public ConstructorMemberHandle constructor() {
		return new ConstructorMemberHandle(this);
	}

	public ConstructorMemberHandle constructor(Class<?>... parameters) {
		return constructor().parameters(parameters);
	}

	public ConstructorMemberHandle constructor(ClassHandle... parameters) {
		return constructor().parameters(parameters);
	}
}