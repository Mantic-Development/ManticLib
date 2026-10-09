package de.exlll.configlib.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a non-vital field that may be omitted from saved configuration while
 * its value matches its initial default. The field is still read when loading,
 * and non-default values are written back.
 *
 * <p>Prefer this for optional leaf settings, such as item name, lore, or custom
 * metadata. Keep a parent section or collection visible when users need it to
 * discover where those settings belong.</p>
 *
 * <p>A comment attached to an omitted field is omitted with it. Put the field's
 * serialized key, its location, and a copyable YAML example in a comment on an
 * always-saved parent field. The serialized key follows the configured
 * {@code FieldNameFormatter}; for example, {@code crateItemData} may be written
 * as {@code crate-item-data} with lower-hyphen formatting. Include examples
 * for the value's shape, such as a scalar, list, or map.</p>
 *
 * <p>For example, if {@code lore} is optional inside an item entry, document
 * {@code lore:} and a YAML list such as {@code - '&7Example line'} on the
 * always-saved {@code items} field.</p>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Optional {}
