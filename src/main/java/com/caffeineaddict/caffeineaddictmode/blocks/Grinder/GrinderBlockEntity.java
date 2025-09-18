package com.caffeineaddict.caffeineaddictmode.blocks.Grinder;

import com.caffeineaddict.caffeineaddictmode.registry.ModBlockEntities;
import com.caffeineaddict.caffeineaddictmode.registry.ModItems;
import com.caffeineaddict.caffeineaddictmode.registry.ModSoundEvents;

import javax.annotation.Nullable;
import javax.annotation.Nonnull;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.inventory.ContainerData;


public class GrinderBlockEntity extends BlockEntity implements MenuProvider {
    private final ItemStackHandler itemHandler = new ItemStackHandler(2); // 0=input, 1=output
    private final LazyOptional<IItemHandler> handler = LazyOptional.of(() -> itemHandler);
    private int progress = 0;
    private int maxProgress = 100;
//    private static final ResourceLocation GRINDER_SOUND_LOC = new ResourceLocation("caffeineaddictmode", "block.grinder");
//    private static final SoundEvent GRINDER_SOUND = SoundEvent.createVariableRangeEvent(GRINDER_SOUND_LOC);


    public GrinderBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GRINDER.get(), pos, state);
    }

    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return handler.cast();
        }
        return super.getCapability(cap, side);
    }

    public Component getDisplayName() {
        return Component.literal("Grinder");
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    public int getProgress() {
        return this.progress;
    }
    public int getMaxProgress() {
        return 100;
    }
    public int getScaledProgress() {
        int maxProgress = 100; // 전체 작업 시간
        int progressBarWidth = 24; // 진행 바 너비(px)
        return (progress * progressBarWidth) / maxProgress;
    }

    @Nullable
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new GrinderMenu(id, playerInventory, this);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, GrinderBlockEntity entity) {
        if (level.isClientSide) return;

        ItemStack input = entity.itemHandler.getStackInSlot(0);
        ItemStack output = entity.itemHandler.getStackInSlot(1);

        if (input.getItem() == ModItems.ROASTED_COFFEE_BEAN.get()) {
            if (output.isEmpty() || (output.getItem() == ModItems.COFFEE_POWDER.get() && output.getCount() < output.getMaxStackSize())) {
                entity.progress++;
                if (entity.progress >= 100) {
                    // 커피콩 소비
                    entity.itemHandler.extractItem(0, 1, false);
                    // 커피 가루 추가
                    entity.itemHandler.insertItem(1, new ItemStack(ModItems.COFFEE_POWDER.get(), 1), false);
                    entity.progress = 0;
                }
            } else {
                entity.progress = 0; // 출력 슬롯이 가득 찼다면 리셋
            }
        } else {
            entity.progress = 0; // 입력이 올바르지 않으면 리셋
        }

        if (entity.progress == 1) { // 작업 시작 시 한 번만
            level.playSound(null, pos, ModSoundEvents.GRINDER_SOUND.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
        }

        entity.setChanged(); // 저장 플래그
    }

    private final ContainerData data = new ContainerData() {
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> maxProgress;
                default -> 0;
            };
        }

        public void set(int index, int value) {
            if (index == 0) progress = value;
            if (index == 1) maxProgress = value;
        }

        public int getCount() {
            return 2;
        }
    };

    public ContainerData getContainerData() {
        return data;
    }
}
