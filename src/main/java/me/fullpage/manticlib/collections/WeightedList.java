package me.fullpage.manticlib.collections;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class WeightedList<T> implements Collection<T> {

    private final java.util.List<WeightedItem<T>> items = new java.util.ArrayList<>();

    public void add(WeightedItem<T> item) {
        items.add(item);
    }

    public void add(T item, double weight) {
        items.add(new WeightedItem<>(item, weight));
    }


    @Override
    public int size() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        return items.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        for (WeightedItem<T> item : items) {
            if (item.getItem().equals(o)) {
                return true;
            }
        }
        return false;
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private final Iterator<WeightedItem<T>> iterator = items.iterator();

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public T next() {
                return iterator.next().getItem();
            }
        };
    }

    @NotNull
    @Override
    public Object @NotNull [] toArray() {
        Object[] temp = new Object[items.size()];
        int i = 0;
        for (WeightedItem<T> item : items) {
            temp[i] = item.getItem();
            i++;
        }
        return temp;
    }

    @NotNull
    @Override
    public <T1> T1[] toArray(@NotNull T1[] a) {
        List<T1> list = new ArrayList<>(size());
        for (WeightedItem<T> item : items) {
            list.add((T1) item.getItem());
        }
        return list.toArray(a);
    }

    public boolean add(T item) {;
        return items.add(new WeightedItem<>(item));
    }

    @Override
    public boolean remove(Object o) {
        return items.removeIf(weightedItem -> weightedItem.getItem().equals(o));
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        for (Object o : c) {
            if (!contains(o)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends T> c) {
        for (T t : c) {
            add(t);
        }
        return true;
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        for (Object o : c) {
            remove(o);
        }
        return true;
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        for (WeightedItem<T> item : items) {
            if (!c.contains(item.getItem())) {
                remove(item.getItem());
            }
        }
        return true;
    }

    public T getHighest() {
        WeightedItem<T> highest = null;
        for (WeightedItem<T> item : items) {
            if (highest == null || item.getWeight() > highest.getWeight()) {
                highest = item;
            }
        }
        return highest == null ? null : highest.getItem();
    }

    public T getLowest() {
        WeightedItem<T> lowest = null;
        for (WeightedItem<T> item : items) {
            if (lowest == null || item.getWeight() < lowest.getWeight()) {
                lowest = item;
            }
        }
        return lowest == null ? null : lowest.getItem();
    }


    public void clear() {
        items.clear();
    }

    public Object getRandom() {
        int totalWeight = 0;
        for (WeightedItem<T> item : items) {
            totalWeight += item.getWeight();
        }

        int random = (int) (Math.random() * totalWeight);
        for (WeightedItem<T> item : items) {
            random -= item.getWeight();
            if (random < 0) {
                return item.getItem();
            }
        }

        return null;
    }

    public java.util.List<WeightedItem<T>> getItems() {
        return items;
    }

    public static class WeightedItem<T> {

        private final T item;
        private final double weight;

        public WeightedItem(T item, double weight) {
            this.item = item;
            this.weight = weight;
        }

        public WeightedItem(T item) {
            this(item, 1);
        }

        public T getItem() {
            return item;
        }

        public double getWeight() {
            return weight;
        }
    }
}