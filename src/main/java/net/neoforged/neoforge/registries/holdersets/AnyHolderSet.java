package net.neoforged.neoforge.registries.holdersets;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.mojang.datafixers.util.Either;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;

public class AnyHolderSet<T> implements HolderSet<T> {

    private final Registry<T> registry;

    public AnyHolderSet(Registry<T> registry) {
        this.registry = registry;
    }

    public Registry<T> registry() {
        return this.registry;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Stream<Holder<T>> stream() {
        return this.registry.listElements().map(h -> (Holder<T>) h);
    }

    @Override
    public int size() {
        return this.registry.size();
    }

    @Override
    public boolean isBound() {
        return true;
    }

    @Override
    public Iterator<Holder<T>> iterator() {
        return this.stream().iterator();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Holder<T> get(int index) {
        return (Holder<T>) this.registry.listElements().skip(index).findFirst().orElseThrow();
    }

    @Override
    public Optional<Holder<T>> getRandomElement(RandomSource random) {
        int size = this.size();
        return size == 0 ? Optional.empty() : Optional.of(this.get(random.nextInt(size)));
    }

    @Override
    public boolean contains(Holder<T> holder) {
        return true;
    }

    @Override
    public boolean canSerializeIn(HolderOwner<T> owner) {
        return true;
    }

    @Override
    public Optional<TagKey<T>> unwrapKey() {
        return Optional.empty();
    }

    @Override
    public Either<TagKey<T>, List<Holder<T>>> unwrap() {
        return Either.right(this.stream().toList());
    }
}
