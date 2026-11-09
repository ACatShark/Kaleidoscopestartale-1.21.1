package com.tequilacat.kaleidoscope_startale.blockentity;

import com.github.ysbbbbbb.kaleidoscopetavern.crafting.recipe.PressingTubRecipe;
import com.github.ysbbbbbb.kaleidoscopetavern.util.FluidUtils;
import com.tequilacat.kaleidoscope_startale.block.JuicerBlock;
import com.tequilacat.kaleidoscope_startale.client.renderer.block.JuicerSounds;
import com.tequilacat.kaleidoscope_startale.crafting.recipe.JuicerRecipe;
import com.tequilacat.kaleidoscope_startale.crafting.serializer.JuicerRecipeInput;
import com.tequilacat.kaleidoscope_startale.init.KSBlockEntities;
import com.tequilacat.kaleidoscope_startale.init.KSItems;
import com.tequilacat.kaleidoscope_startale.init.KSRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

/**
 * 榨汁机方块实体。
 * <p>
 * 管理输入物品与流体罐，按榨汁机配方（或退化的压榨桶配方）进行榨汁：
 * 逐刻推进进度、按比例产出流体并消耗原料，支持分批处理、
 * 开盖暂停、取出物品、导出流体以及方块的保存与同步。
 */
public class JuicerBlockEntity extends BlockEntity {

    /** 输入槽位数量。 */
    public static final int INPUT_SLOTS = 4;

    /** 流体罐容量（毫桶）。 */
    public static final int TANK_CAPACITY = 1000;

    /** 退化压榨桶配方的制作时间（刻）。 */
    public static final int PRESS_CRAFT_TIME = 20;

    /**
     * 输入物品处理器。
     * <p>
     * 内容变化时同步；每槽上限 64；仅允许放入与已有物品相同的物品。
     */
    private final ItemStackHandler items = new ItemStackHandler(INPUT_SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            JuicerBlockEntity.this.sync();
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            for (int i = 0; i < this.getSlots(); ++i) {
                ItemStack existing = this.getStackInSlot(i);
                if (!existing.isEmpty() && !ItemStack.isSameItemSameComponents(existing, stack)) {
                    return false;
                }
            }
            return super.isItemValid(slot, stack);
        }
    };

    /**
     * 流体罐。
     * <p>
     * 内容变化时同步。
     */
    private final FluidTank tank = new FluidTank(TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            JuicerBlockEntity.this.sync();
        }
    };

    /** 榨汁机配方缓存查询。 */
    private final RecipeManager.CachedCheck<JuicerRecipeInput, JuicerRecipe> mixingCheck;

    /** 压榨桶配方缓存查询。 */
    private final RecipeManager.CachedCheck<SingleRecipeInput, PressingTubRecipe> pressingCheck;

    /** 是否正在制作。 */
    private boolean crafting;

    /** 制作开始时刻（游戏时间）。 */
    private long craftStartTime;

    /** 当前进度（刻）。 */
    private int progress;

    /** 本次制作总时长（刻）。 */
    private int maxProgress;

    /** 本批次产出的流体总量。 */
    private int batchOutput;

    /** 制作开始时流体罐已有的流体量。 */
    private int startOutput;

    /** 本次制作的产出流体（单份量）。 */
    private FluidStack craftFluid = FluidStack.EMPTY;

    /** 各槽位计划消耗的物品数量。 */
    private int[] consumePerSlot = new int[INPUT_SLOTS];

    /** 已经消耗的物品总数。 */
    private int consumedItems;

    /**
     * 构造榨汁机方块实体。
     *
     * @param pos   方块位置
     * @param state 方块状态
     */
    public JuicerBlockEntity(BlockPos pos, BlockState state) {
        super(KSBlockEntities.JUICER.get(), pos, state);
        this.mixingCheck = RecipeManager.createCheck(KSRecipes.JUICER_TYPE);
        this.pressingCheck = RecipeManager.createCheck(com.github.ysbbbbbb.kaleidoscopetavern.init.ModRecipes.PRESSING_TUB_RECIPE);
    }

    /**
     * 服务端每刻更新。
     * <p>
     * 开盖时暂停制作并记录已完成进度；制作中则推进进度；
     * 存在未完成批次时恢复制作；否则尝试开始新的制作。
     *
     * @param level  世界
     * @param pos    方块位置
     * @param state  方块状态
     * @param juicer 榨汁机方块实体
     */
    public static void serverTick(Level level, BlockPos pos, BlockState state, JuicerBlockEntity juicer) {
        boolean open = state.hasProperty(JuicerBlock.OPEN) && state.getValue(JuicerBlock.OPEN);

        if (open) {
            // 开盖：暂停（记录已完成的进度）
            if (juicer.crafting) {
                juicer.craftStartTime = level.getGameTime() - (long) juicer.progress;
                juicer.crafting = false;
                juicer.sync();
            }
            return;
        }

        if (juicer.crafting) {
            juicer.tickCrafting();
        } else if (juicer.hasPendingBatch()) {
            juicer.craftStartTime = level.getGameTime() - (long) juicer.progress;
            juicer.crafting = true;
            juicer.sync();
            juicer.tickCrafting();
        } else {
            juicer.tryStartCrafting();
        }
    }

    /**
     * 判断是否存在未完成的批次。
     * <p>
     * 存在有效总时长且产出流体非空时视为有未完成批次。
     *
     * @return 是否存在未完成批次
     */
    public boolean hasPendingBatch() {
        return this.maxProgress > 0 && !this.craftFluid.isEmpty();
    }

    /**
     * 客户端每刻更新。
     * <p>
     * 根据制作状态更新榨汁机工作音效。
     *
     * @param level  世界
     * @param pos    方块位置
     * @param state  方块状态
     * @param juicer 榨汁机方块实体
     */
    public static void clientTick(Level level, BlockPos pos, BlockState state, JuicerBlockEntity juicer) {
        JuicerSounds.tick(level, pos, juicer.crafting);
    }

    /**
     * 方块实体移除时调用。
     * <p>
     * 客户端下停止该位置的榨汁机音效。
     */
    @Override
    public void setRemoved() {
        super.setRemoved();
        if (this.level != null && this.level.isClientSide) {
            JuicerSounds.stop(this.level, this.worldPosition);
        }
    }

    /**
     * 尝试开始制作。
     * <p>
     * 查找可用方案，计算材料总量、单份流体量、罐内剩余空间与可处理批次，
     * 据此裁剪消耗计划并初始化制作状态。
     *
     * @return 是否成功开始制作
     */
    public boolean tryStartCrafting() {
        if (this.crafting || this.level == null) {
            return false;
        }

        Plan plan = this.findPlan();
        if (plan == null) {
            return false;
        }

        int totalItems = 0;
        for (int count : plan.consume()) {
            totalItems += count;
        }
        if (totalItems <= 0) {
            return false;
        }

        int perItem = plan.fluid().getAmount();
        if (perItem <= 0) {
            return false;
        }

        int space = this.tank.getCapacity() - this.tank.getFluidAmount();
        if (space <= 0) {
            return false;
        }

        int batch = Math.min(totalItems, space / perItem);
        if (batch <= 0) {
            if (plan.exactBatch()) {
                return false;
            }
            batch = 1;
        }
        if (plan.exactBatch() && batch < totalItems) {
            return false;
        }

        this.consumePerSlot = trimConsume(plan.consume(), batch);
        this.consumedItems = 0;
        this.crafting = true;
        this.craftStartTime = this.level.getGameTime();
        this.progress = 0;
        this.maxProgress = Math.max(1, plan.craftTime() * batch);
        this.batchOutput = perItem * batch;
        this.startOutput = this.tank.getFluidAmount();
        this.craftFluid = plan.fluid().copyWithAmount(perItem);
        this.sync();
        return true;
    }

    /** 只消耗 batch 份：多出来的从数组末尾往前削。 */
    private static int[] trimConsume(int[] consume, int batch) {
        int[] trimmed = consume.clone();
        int excess = 0;
        for (int count : trimmed) {
            excess += count;
        }
        excess -= batch;

        for (int i = trimmed.length - 1; i >= 0 && excess > 0; --i) {
            int cut = Math.min(trimmed[i], excess);
            trimmed[i] -= cut;
            excess -= cut;
        }
        return trimmed;
    }

    /**
     * 推进制作进度。
     * <p>
     * 计算已过时长与进度比例，按比例产出流体、消耗原料；
     * 达到总时长时完成制作，否则每 4 刻同步一次。
     */
    private void tickCrafting() {
        if (this.level == null) {
            this.progress = Math.min(this.progress + 1, this.maxProgress);
        } else {
            if (this.craftStartTime == 0L) {
                this.craftStartTime = this.level.getGameTime() - (long) this.progress;
            }
            long elapsed = this.level.getGameTime() - this.craftStartTime;
            this.progress = (int) Math.min(elapsed, (long) this.maxProgress);
        }

        float ratio = Math.min((float) this.progress / (float) this.maxProgress, 1.0F);

        int expected = this.startOutput + (int) ((float) this.batchOutput * ratio);
        int add = expected - this.tank.getFluidAmount();
        if (add > 0) {
            this.tank.fill(new FluidStack(this.craftFluid.getFluid(), add), FluidAction.EXECUTE);
        }

        this.consumeUpToRatio(ratio);

        if (this.progress >= this.maxProgress) {
            this.finishCrafting();
        } else if (this.progress % 4 == 0) {
            this.sync();
        }
    }

    /**
     * 按进度比例消耗原料。
     * <p>
     * 根据比例计算应消耗总数，从后往前逐槽取出差额部分，
     * 并更新已消耗计数与各槽剩余计划。
     *
     * @param ratio 进度比例
     */
    private void consumeUpToRatio(float ratio) {
        int totalItems = 0;
        for (int count : this.consumePerSlot) {
            totalItems += count;
        }

        int shouldHaveConsumed = (int) ((float) totalItems * ratio);
        int stillToConsume = shouldHaveConsumed - this.consumedItems;
        if (stillToConsume <= 0) {
            return;
        }

        for (int slot = this.consumePerSlot.length - 1; slot >= 0 && stillToConsume > 0; --slot) {
            int want = this.consumePerSlot[slot];
            if (want <= 0) {
                continue;
            }
            int available = this.items.getStackInSlot(slot).getCount();
            int take = Math.min(want, Math.min(available, stillToConsume));
            if (take > 0) {
                this.items.extractItem(slot, take, false);
                this.consumePerSlot[slot] -= take;
                this.consumedItems += take;
                stillToConsume -= take;
            }
        }
    }

    /**
     * 完成制作。
     * <p>
     * 补齐剩余流体、消耗剩余原料、清除制作状态，播放完成音效并同步。
     */
    private void finishCrafting() {
        int expected = this.startOutput + this.batchOutput;
        int add = expected - this.tank.getFluidAmount();
        if (add > 0) {
            this.tank.fill(new FluidStack(this.craftFluid.getFluid(), add), FluidAction.EXECUTE);
        }

        this.consumeUpToRatio(1.0F);
        this.clearCraftState();

        if (this.level != null) {
            this.level.playSound(null, this.worldPosition, SoundEvents.SLIME_BLOCK_FALL, SoundSource.BLOCKS,
                    0.4F, 0.9F + this.level.random.nextFloat() * 0.2F);
        }
        this.sync();
    }

    /**
     * 清除制作状态。
     * <p>
     * 重置制作标志、进度、产出与消耗计划等所有相关字段。
     */
    private void clearCraftState() {
        this.crafting = false;
        this.craftStartTime = 0L;
        this.progress = 0;
        this.maxProgress = 0;
        this.batchOutput = 0;
        this.startOutput = 0;
        this.craftFluid = FluidStack.EMPTY;
        this.consumePerSlot = new int[INPUT_SLOTS];
        this.consumedItems = 0;
    }

    /**
     * 查找可用的制作方案。
     * <p>
     * 优先匹配榨汁机配方；不匹配时退化为压榨桶配方，
     * 仅能处理全部槽位为同一种材料的情况。
     *
     * @return 制作方案，无可方案时返回 null
     */
    @Nullable
    public Plan findPlan() {
        if (this.level == null) {
            return null;
        }

        JuicerRecipeInput input = new JuicerRecipeInput(this.items);
        JuicerRecipe mixing = this.mixingCheck.getRecipeFor(input, this.level).map(RecipeHolder::value).orElse(null);
        if (mixing != null) {
            int[] consume = mixing.matchConsumption(input);
            if (consume != null) {
                return new Plan(new FluidStack(mixing.fluid(), mixing.fluidAmount()), mixing.craftTime(), consume,
                        mixing.result(), true);
            }
        }

        // 退化成压榨桶配方：只能处理「单一材料」
        SlotStack single = this.findSingleKind();
        if (single == null) {
            return null;
        }
        PressingTubRecipe pressing = this.pressingCheck
                .getRecipeFor(new SingleRecipeInput(single.stack()), this.level)
                .map(RecipeHolder::value)
                .orElse(null);
        if (pressing == null) {
            return null;
        }

        int[] consume = new int[INPUT_SLOTS];
        for (int i = 0; i < INPUT_SLOTS; ++i) {
            ItemStack stack = this.items.getStackInSlot(i);
            if (!stack.isEmpty()) {
                consume[i] = stack.getCount();
            }
        }
        return new Plan(new FluidStack(pressing.getFluid(), pressing.getFluidAmount()), PRESS_CRAFT_TIME,
                consume, ItemStack.EMPTY, false);
    }

    /**
     * 查找单一材料。
     * <p>
     * 遍历所有非空槽位，若均为同一种物品则返回第一个非空槽位，
     * 存在多种物品时返回 null。
     *
     * @return 单一材料槽位信息，无法确定时返回 null
     */
    @Nullable
    private SlotStack findSingleKind() {
        SlotStack found = null;
        for (int i = 0; i < this.items.getSlots(); ++i) {
            ItemStack stack = this.items.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (found == null) {
                found = new SlotStack(i, stack);
            } else if (!ItemStack.isSameItemSameComponents(found.stack(), stack)) {
                return null;
            }
        }
        return found;
    }

    /** 和果盆一样一次投入整叠，装不下的部分留在玩家手里。 */
    public boolean addItem(ItemStack held) {
        if (held.isEmpty()) {
            return false;
        }

        ItemStack remainder = ItemHandlerHelper.insertItemStacked(this.items, held.copy(), false);
        int inserted = held.getCount() - remainder.getCount();
        if (inserted <= 0) {
            return false;
        }
        held.shrink(inserted);
        return true;
    }

    /**
     * 清空输入物品并返还给玩家。
     * <p>
     * 若正在制作或存在未完成批次则先清除制作状态，
     * 然后逐槽取出物品，放不下则掉落。
     *
     * @param player 玩家
     */
    public void emptyInput(Player player) {
        if (this.crafting || this.hasPendingBatch()) {
            this.clearCraftState();
        }

        for (int i = 0; i < this.items.getSlots(); ++i) {
            ItemStack stack = this.items.extractItem(i, 64, false);
            if (!stack.isEmpty() && !player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
        }
        this.sync();
    }

    /**
     * 取出一个输入物品。
     * <p>
     * 若正在制作或存在未完成批次则先清除制作状态，
     * 从后往前找到一个非空槽位取出一个物品并交给玩家。
     *
     * @param player 玩家
     * @return 是否成功取出
     */
    public boolean takeOne(Player player) {
        if (this.crafting || this.hasPendingBatch()) {
            this.clearCraftState();
        }

        for (int i = this.items.getSlots() - 1; i >= 0; --i) {
            ItemStack taken = this.items.extractItem(i, 1, false);
            if (taken.isEmpty()) {
                continue;
            }
            if (!player.getInventory().add(taken)) {
                player.drop(taken, false);
            }
            return true;
        }
        return false;
    }

    /**
     * 拾取方块为物品。
     * <p>
     * 创建携带方块实体数据的物品堆叠，移除方块并交给玩家。
     *
     * @param level  世界
     * @param pos    方块位置
     * @param player 玩家
     */
    public void pickup(Level level, BlockPos pos, Player player) {
        ItemStack stack = this.createDropStack(level.registryAccess());
        level.removeBlock(pos, false);
        ItemHandlerHelper.giveItemToPlayer(player, stack);
    }

    /**
     * 创建掉落用的物品堆叠。
     * <p>
     * 生成榨汁机物品并写入当前方块实体数据。
     *
     * @param registries 注册表提供者
     * @return 携带方块实体数据的物品堆叠
     */
    public ItemStack createDropStack(HolderLookup.Provider registries) {
        ItemStack stack = new ItemStack(KSItems.JUICER.get());
        this.saveToItem(stack, registries);
        return stack;
    }

    /**
     * 将罐内流体导出到手持物品。
     * <p>
     * 仅当流体量不少于 1000 时执行灌注。
     *
     * @param player 玩家
     * @param held   手持物品堆叠
     * @return 是否成功导出
     */
    public boolean drainInto(Player player, ItemStack held) {
        return this.tank.getFluidAmount() >= 1000 && FluidUtils.fillItem(player, held, this.tank, 1000);
    }

    /**
     * 切换开合状态。
     * <p>
     * 翻转方块的 OPEN 属性，播放开盖或合盖音效并同步。
     */
    public void toggleOpen() {
        if (this.level == null) {
            return;
        }
        BlockState state = this.getBlockState();
        if (!state.hasProperty(JuicerBlock.OPEN)) {
            return;
        }

        boolean open = state.getValue(JuicerBlock.OPEN);
        this.level.setBlockAndUpdate(this.worldPosition, state.setValue(JuicerBlock.OPEN, !open));
        this.level.playSound(null, this.worldPosition, open ? SoundEvents.BARREL_CLOSE : SoundEvents.BARREL_OPEN,
                SoundSource.BLOCKS, 0.8F, 1.0F);
        this.sync();
    }

    /**
     * 读取方块实体数据。
     * <p>
     * 还原输入物品、流体罐与制作相关状态；制作开始时刻在读取时重置为 0。
     *
     * @param tag        数据标签
     * @param registries 注册表提供者
     */
    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.items.deserializeNBT(registries, tag.getCompound("items"));
        this.tank.readFromNBT(registries, tag.getCompound("tank"));
        this.crafting = tag.getBoolean("crafting");
        this.craftStartTime = 0L;
        this.progress = tag.getInt("progress");
        this.maxProgress = tag.getInt("max_progress");
        this.batchOutput = tag.getInt("batch_output");
        this.startOutput = tag.getInt("start_output");
        this.craftFluid = readFluid(tag.getCompound("craft_fluid"));
        this.consumePerSlot = new int[INPUT_SLOTS];
        if (tag.contains("consume")) {
            int[] loaded = tag.getIntArray("consume");
            System.arraycopy(loaded, 0, this.consumePerSlot, 0, Math.min(loaded.length, this.consumePerSlot.length));
        }
        this.consumedItems = tag.getInt("consumed");
    }

    /**
     * 保存方块实体数据。
     * <p>
     * 写入输入物品、流体罐与制作相关状态。
     *
     * @param tag        数据标签
     * @param registries 注册表提供者
     */
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("items", this.items.serializeNBT(registries));
        tag.put("tank", this.tank.writeToNBT(registries, new CompoundTag()));
        tag.putBoolean("crafting", this.crafting);
        tag.putLong("craft_start", this.craftStartTime);
        tag.putInt("progress", this.progress);
        tag.putInt("max_progress", this.maxProgress);
        tag.putInt("batch_output", this.batchOutput);
        tag.putInt("start_output", this.startOutput);
        tag.put("craft_fluid", writeFluid(this.craftFluid));
        tag.putIntArray("consume", this.consumePerSlot);
        tag.putInt("consumed", this.consumedItems);
    }

    /**
     * 序列化流体堆叠。
     * <p>
     * 写入流体注册名与数量，空流体返回空标签。
     *
     * @param stack 流体堆叠
     * @return 数据标签
     */
    private static CompoundTag writeFluid(FluidStack stack) {
        CompoundTag tag = new CompoundTag();
        if (stack.isEmpty()) {
            return tag;
        }
        ResourceLocation id = BuiltInRegistries.FLUID.getKey(stack.getFluid());
        tag.putString("id", id.toString());
        tag.putInt("amount", stack.getAmount());
        return tag;
    }

    /**
     * 反序列化流体堆叠。
     * <p>
     * 根据流体注册名与数量还原，无法解析或为空气时返回空流体。
     *
     * @param tag 数据标签
     * @return 流体堆叠
     */
    private static FluidStack readFluid(CompoundTag tag) {
        if (!tag.contains("id")) {
            return FluidStack.EMPTY;
        }
        ResourceLocation id = ResourceLocation.tryParse(tag.getString("id"));
        if (id == null) {
            return FluidStack.EMPTY;
        }
        Fluid fluid = BuiltInRegistries.FLUID.get(id);
        return fluid != Fluids.EMPTY ? new FluidStack(fluid, tag.getInt("amount")) : FluidStack.EMPTY;
    }

    /**
     * 获取更新标签。
     * <p>
     * 包含完整保存数据，用于同步到客户端。
     *
     * @param registries 注册表提供者
     * @return 更新标签
     */
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag, registries);
        return tag;
    }

    /**
     * 获取更新数据包。
     *
     * @return 方块实体数据包
     */
    @Override
    @Nullable
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    /**
     * 同步方块实体状态。
     * <p>
     * 标记数据已变更，服务端下向客户端发送方块更新。
     */
    private void sync() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    /**
     * 获取输入物品处理器。
     *
     * @return 输入物品处理器
     */
    public ItemStackHandler getItems() {
        return this.items;
    }

    /**
     * 获取流体罐。
     *
     * @return 流体罐
     */
    public FluidTank getTank() {
        return this.tank;
    }

    /**
     * 判断是否正在制作。
     *
     * @return 是否正在制作
     */
    public boolean isCrafting() {
        return this.crafting;
    }

    /**
     * 获取当前进度。
     *
     * @return 当前进度（刻）
     */
    public int getProgress() {
        return this.progress;
    }

    /**
     * 获取本次制作总时长。
     *
     * @return 总时长（刻）
     */
    public int getMaxProgress() {
        return this.maxProgress;
    }

    /**
     * 获取本批次产出的流体总量。
     *
     * @return 产出流体总量
     */
    public int getBatchOutput() {
        return this.batchOutput;
    }

    /**
     * 获取制作开始时的流体量。
     *
     * @return 起始流体量
     */
    public int getStartOutput() {
        return this.startOutput;
    }

    /**
     * 获取本次制作的产出流体。
     *
     * @return 产出流体
     */
    public FluidStack getCraftFluid() {
        return this.craftFluid;
    }

    /**
     * 判断罐内是否有流体。
     *
     * @return 是否有流体
     */
    public boolean hasFluid() {
        return !this.tank.getFluid().isEmpty();
    }

    /**
     * 制作方案。
     *
     * @param fluid      产出流体（单份量）
     * @param craftTime  单份制作时间（刻）
     * @param consume    各槽位消耗数量
     * @param extra      附加产物
     * @param exactBatch 是否要求整批材料一次性匹配
     */
    public record Plan(FluidStack fluid, int craftTime, int[] consume, ItemStack extra, boolean exactBatch) {
    }

    /**
     * 槽位材料信息。
     *
     * @param slot  槽位索引
     * @param stack 该槽位的物品堆叠
     */
    private record SlotStack(int slot, ItemStack stack) {
    }
}