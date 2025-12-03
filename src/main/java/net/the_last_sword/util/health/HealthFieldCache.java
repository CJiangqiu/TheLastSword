package net.the_last_sword.util.health;

import net.minecraft.world.entity.LivingEntity;

import java.util.function.BiFunction;
import java.util.function.Function;

//缓存实体类的生命值字段访问信息
public class HealthFieldCache {

    //统一的访问模式（字段和容器都用这个）
    public ContainerAccessPattern accessPattern;

    //逆向公式：将目标血量转换为需要写入字段的值
    public Function<Float, Float> reverseTransform;

    //统一的写入函数
    public BiFunction<LivingEntity, Float, Boolean> writePath;

    //容器检测信息（用于主动扫描）
    public boolean containerDetected = false;
    public String containerClass;
    public String containerGetterMethod;
    public String containerType;
}
