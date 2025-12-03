package net.the_last_sword.util.health;

import net.minecraft.world.entity.LivingEntity;

import java.lang.invoke.VarHandle;

//容器访问模式：描述如何访问和修改容器中的值
public class ContainerAccessPattern {

    //容器获取方式（静态方法/实例字段/静态字段）
    public ContainerGetter containerGetter;

    //Key 构造方式（this/getId()/静态字段/实例字段）
    public KeyBuilder keyBuilder;

    //值定位器：根据容器和 Key 找到值的持有者对象
    public ValueLocator valueLocator;

    //值的 VarHandle（通用）
    public VarHandle valueHandle;

    //标记是否是数组元素访问（ArrayList 等）
    public boolean isArrayElement;

    //容器获取器
    @FunctionalInterface
    public interface ContainerGetter {
        Object getContainer(LivingEntity entity) throws Exception;
    }

    //Key 构造器
    @FunctionalInterface
    public interface KeyBuilder {
        Object buildKey(LivingEntity entity) throws Exception;
    }

    //值定位器
    @FunctionalInterface
    public interface ValueLocator {
        //定位到值的持有者对象
        //对于 HashMap：返回 Node
        //对于 ArrayList：返回 elementData 数组
        //对于 EntityData：返回 DataItem
        //对于字段：返回 Entity 本身
        Object locateValueHolder(Object container, Object key) throws Exception;
    }
}
