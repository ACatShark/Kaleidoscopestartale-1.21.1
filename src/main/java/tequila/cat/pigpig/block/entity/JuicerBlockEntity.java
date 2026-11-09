package tequila.cat.pigpig.block.entity;

import com.github.ysbbbbbb.kaleidoscopetavern.crafting.recipe.PressingTubRecipe;
import com.github.ysbbbbbb.kaleidoscopetavern.util.FluidUtils;

import org.jetbrains.annotations.Nullable;

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

import tequila.cat.pigpig.block.JuicerBlock;
import tequila.cat.pigpig.client.JuicerSounds;
import tequila.cat.pigpig.crafting.JuicerRecipe;
import tequila.cat.pigpig.crafting.JuicerRecipeInput;
import tequila.cat.pigpig.init.ModBlockEntities;
import tequila.cat.pigpig.init.ModItems;
import tequila.cat.pigpig.init.ModRecipes;

/**
 * 榨汁机逻辑：
 *  1) 先按本模组的 juicer 配方（可多材料）匹配；
 *  2) 匹配不到则退化成 tavern 压榨桶配方（单一材料，20 tick）；
 *  3) 液体按进度条渐进注入储罐，物品按比例渐进消耗；
 *  4) 开盖（OPEN=true）时暂停并保留进度。
 */
public class JuicerBlockEntity extends BlockEntity {
    public static final int INPUT_SLOTS = 4;
    public static final int TANK_CAPACITY = 1000;
    /** 退化压榨桶配方的处理时间。原为 40 tick，按 2 倍速缩短为 20 tick。 */
    public static final int PRESS_CRAFT_TIME = 20;

    private final ItemStackHandler items = new ItemStackHandler(INPUT_SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            JuicerBlockEntity.this.sync();
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        /** 和果盆一样只装一种物品：混装会让压榨桶配方匹配失败，机器直接停摆。 */
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

    private final FluidTank tank = new FluidTank(TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            JuicerBlockEntity.this.sync();
        }
    };

    private final RecipeManager.CachedCheck<JuicerRecipeInput, JuicerRecipe> mixingCheck;
    private final RecipeManager.CachedCheck<SingleRecipeInput, PressingTubRecipe> pressingCheck;

    private boolean crafting;
    private long craftStartTime;
    private int progress;
    private int maxProgress;
    private int batchOutput;
    private int startOutput;
    private FluidStack craftFluid = FluidStack.EMPTY;
    private int[] consumePerSlot = new int[INPUT_SLOTS];
    private int consumedItems;

    public JuicerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.JUICER.get(), pos, state);
        this.mixingCheck = RecipeManager.createCheck(ModRecipes.JUICER_TYPE);
        this.pressingCheck = RecipeManager.createCheck(com.github.ysbbbbbb.kaleidoscopetavern.init.ModRecipes.PRESSING_TUB_RECIPE);
    }

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

    public boolean hasPendingBatch() {
        return this.maxProgress > 0 && !this.craftFluid.isEmpty();
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, JuicerBlockEntity juicer) {
        JuicerSounds.tick(level, pos, juicer.crafting);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (this.level != null && this.level.isClientSide) {
            JuicerSounds.stop(this.level, this.worldPosition);
        }
    }

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
     * 取回 1 个原料。
     * 正在压榨时先清掉当前批次，否则各槽的待消耗数量会与槽内物品对不上。
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

    public void pickup(Level level, BlockPos pos, Player player) {
        ItemStack stack = this.createDropStack(level.registryAccess());
        level.removeBlock(pos, false);
        ItemHandlerHelper.giveItemToPlayer(player, stack);
    }

    public ItemStack createDropStack(HolderLookup.Provider registries) {
        ItemStack stack = new ItemStack(ModItems.JUICER.get());
        this.saveToItem(stack, registries);
        return stack;
    }

    public boolean drainInto(Player player, ItemStack held) {
        return this.tank.getFluidAmount() >= 1000 && FluidUtils.fillItem(player, held, this.tank, 1000);
    }

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

    private static CompoundTag writeFluid(FluidStack stack) {
        CompoundTag tag = new CompoundTag();
        if (stack.isEmpty()) {
            return tag;
        }
        ResourceLocation id = BuiltInRegistries.FLUID.getKey(stack.getFluid());
        if (id != null) {
            tag.putString("id", id.toString());
            tag.putInt("amount", stack.getAmount());
        }
        return tag;
    }

    private static FluidStack readFluid(CompoundTag tag) {
        if (!tag.contains("id")) {
            return FluidStack.EMPTY;
        }
        ResourceLocation id = ResourceLocation.tryParse(tag.getString("id"));
        if (id == null) {
            return FluidStack.EMPTY;
        }
        Fluid fluid = BuiltInRegistries.FLUID.get(id);
        return fluid != null && fluid != Fluids.EMPTY ? new FluidStack(fluid, tag.getInt("amount")) : FluidStack.EMPTY;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag, registries);
        return tag;
    }

    @Override
    @Nullable
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private void sync() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public ItemStackHandler getItems() {
        return this.items;
    }

    public FluidTank getTank() {
        return this.tank;
    }

    public boolean isCrafting() {
        return this.crafting;
    }

    public int getProgress() {
        return this.progress;
    }

    public int getMaxProgress() {
        return this.maxProgress;
    }

    public int getBatchOutput() {
        return this.batchOutput;
    }

    public int getStartOutput() {
        return this.startOutput;
    }

    public FluidStack getCraftFluid() {
        return this.craftFluid;
    }

    public boolean hasFluid() {
        return !this.tank.getFluid().isEmpty();
    }

    public record Plan(FluidStack fluid, int craftTime, int[] consume, ItemStack extra, boolean exactBatch) {
    }

    private record SlotStack(int slot, ItemStack stack) {
    }
}
