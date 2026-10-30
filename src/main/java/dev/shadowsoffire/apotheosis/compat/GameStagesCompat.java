package dev.shadowsoffire.apotheosis.compat;

import java.util.Set;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import dev.shadowsoffire.apotheosis.Apotheosis;
import net.minecraft.world.entity.player.Player;

public class GameStagesCompat {

    public static boolean hasStage(Player player, Set<String> stages) {
        return !Apotheosis.STAGES_LOADED || stages.isEmpty();
    }

    public static Set<String> getStages(Player player) {
        return Set.of();
    }

    public interface IStaged {

        @Nullable
        Set<String> getStages();

        static <T extends IStaged> Predicate<T> matches(Player player) {
            return obj -> hasStage(player, obj.getStages());
        }
    }
}
