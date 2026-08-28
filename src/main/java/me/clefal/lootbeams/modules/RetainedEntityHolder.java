//? >=26.2 {
/*package me.clefal.lootbeams.modules;

import com.clefal.nirvana_lib.relocated.io.vavr.Tuple3;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

// MC 26.2 split LevelRenderer.extractVisibleEntities/extractEntity out into a separate
// LevelExtractor class, so the retained-item-entities-until-submitEntities pattern can no
// longer live as a single mixin instance field (LevelExtractor and LevelRenderer are two
// distinct per-client singleton objects). Both are only ever touched from the render thread,
// so a plain static list is safe - matches the original design's single-threaded assumption.
//
// Deliberately NOT in the me.clefal.lootbeams.mixin.* package: Mixin treats that whole package
// as reserved for its own mixin definitions (declared via mixins.json's "package" field) and
// throws IllegalClassLoadError if application code tries to load a class from inside it directly
// rather than through Mixin's own merge process - even a plain, non-@Mixin-annotated helper class.
public class RetainedEntityHolder {
    public static final List<Tuple3<ItemEntity, Float, Vec3>> RETAINED = new ArrayList<>();
}
*///?}
