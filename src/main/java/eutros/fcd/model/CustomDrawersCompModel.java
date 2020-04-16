package eutros.fcd.model;

import com.google.common.collect.ImmutableList;
import com.jaquadro.minecraft.chameleon.Chameleon;
import com.jaquadro.minecraft.chameleon.model.CachedBuilderModel;
import com.jaquadro.minecraft.chameleon.model.ChamModel;
import com.jaquadro.minecraft.chameleon.model.ProxyBuilderModel;
import com.jaquadro.minecraft.chameleon.render.ChamRender;
import com.jaquadro.minecraft.chameleon.resources.IconUtil;
import com.jaquadro.minecraft.chameleon.resources.register.DefaultRegister;
import com.jaquadro.minecraft.storagedrawers.block.BlockDrawers;
import com.jaquadro.minecraft.storagedrawers.block.BlockDrawersCustom;
import com.jaquadro.minecraft.storagedrawers.block.EnumCompDrawer;
import com.jaquadro.minecraft.storagedrawers.block.modeldata.DrawerStateModelData;
import com.jaquadro.minecraft.storagedrawers.block.modeldata.MaterialModelData;
import com.jaquadro.minecraft.storagedrawers.client.model.component.DrawerDecoratorModel;
import com.jaquadro.minecraft.storagedrawers.client.model.component.DrawerSealedModel;
import eutros.fcd.block.CustomDrawersComp;
import eutros.fcd.registry.ModBlocks;
import eutros.fcd.utils.Reference;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CustomDrawersCompModel extends ChamModel {

    public static class Register extends DefaultRegister<CustomDrawersComp> {

        public static final ResourceLocation iconDefaultSide = new ResourceLocation(Reference.MOD_ID + ":blocks/drawers_comp_raw_side");

        public static final ResourceLocation[] iconDefaultFront = new ResourceLocation[] {
                new ResourceLocation(Reference.MOD_ID + ":blocks/drawers_comp_raw_front_0"),
                new ResourceLocation(Reference.MOD_ID + ":blocks/drawers_comp_raw_front_1"),
                new ResourceLocation(Reference.MOD_ID + ":blocks/drawers_comp_raw_front_2"),
        };
        public static final ResourceLocation[] iconOverlayTrim = new ResourceLocation[] {
                new ResourceLocation(Reference.MOD_ID + ":blocks/overlay/shading_trim_1"),
                new ResourceLocation(Reference.MOD_ID + ":blocks/overlay/shading_trim_2"),
                new ResourceLocation(Reference.MOD_ID + ":blocks/overlay/shading_trim_4"),
        };
        public static final ResourceLocation[] iconOverlayBoldTrim = new ResourceLocation[] {
                new ResourceLocation(Reference.MOD_ID + ":blocks/overlay/shading_boldtrim_1"),
                new ResourceLocation(Reference.MOD_ID + ":blocks/overlay/shading_boldtrim_2"),
                new ResourceLocation(Reference.MOD_ID + ":blocks/overlay/shading_boldtrim_4"),
        };
        public static final ResourceLocation[] iconOverlayFace = new ResourceLocation[] {
                new ResourceLocation(Reference.MOD_ID + ":blocks/overlay/shading_face_1"),
                new ResourceLocation(Reference.MOD_ID + ":blocks/overlay/shading_face_2"),
                new ResourceLocation(Reference.MOD_ID + ":blocks/overlay/shading_face_4"),
        };
        public static final ResourceLocation[] iconOverlayHandle = new ResourceLocation[] {
                new ResourceLocation(Reference.MOD_ID + ":blocks/overlay/handle_1"),
                new ResourceLocation(Reference.MOD_ID + ":blocks/overlay/handle_2"),
                new ResourceLocation(Reference.MOD_ID + ":blocks/overlay/handle_4"),
        };

        public Register() {
            super(ModBlocks.framedCompactDrawer);
        }

        @Override
        public List<IBlockState> getBlockStates() {
            List<IBlockState> states = new ArrayList<>();

            for(EnumCompDrawer drawer : EnumCompDrawer.values())
                for(EnumFacing dir : EnumFacing.HORIZONTALS)
                    states.add(ModBlocks.framedCompactDrawer.getDefaultState()
                            .withProperty(CustomDrawersComp.SLOTS, drawer)
                            .withProperty(BlockDrawers.FACING, dir));

            return states;
        }

        @Override
        public IBakedModel getModel(IBlockState state, IBakedModel existingModel) {
            return new CachedBuilderModel(new Model());
        }

        @Override
        public IBakedModel getModel(ItemStack stack, IBakedModel existingModel) {
            return new CachedBuilderModel(new Model());
        }

        @Override
        public List<ResourceLocation> getTextureResources() {
            List<ResourceLocation> resource = new ArrayList<>();
            resource.add(iconDefaultSide);
            resource.addAll(Arrays.asList(iconDefaultFront));
            resource.addAll(Arrays.asList(iconOverlayTrim));
            resource.addAll(Arrays.asList(iconOverlayBoldTrim));
            resource.addAll(Arrays.asList(iconOverlayFace));
            resource.addAll(Arrays.asList(iconOverlayHandle));
            return resource;
        }

    }

    private static final int[] iconIndex = new int[] {0, 0, 1, 0, 2};

    private TextureAtlasSprite iconParticle;

    public static IBakedModel fromBlock(IBlockState state) {
        if(!(state instanceof IExtendedBlockState))
            return new CustomDrawersCompModel(state, false);

        IExtendedBlockState xstate = (IExtendedBlockState) state;
        DrawerStateModelData stateModel = xstate.getValue(BlockDrawers.STATE_MODEL);
        MaterialModelData matModel = xstate.getValue(BlockDrawersCustom.MAT_MODEL);
        if(stateModel == null || matModel == null)
            return new CustomDrawersCompModel(state, false);

        ItemStack effMatFront = matModel.getEffectiveMaterialFront();
        ItemStack effMatSide = matModel.getEffectiveMaterialSide();
        ItemStack effMatTrim = matModel.getEffectiveMaterialTrim();

        ItemStack matFront = matModel.getMaterialFront();
        ItemStack matSide = matModel.getMaterialSide();
        ItemStack matTrim = matModel.getMaterialTrim();

        return new CustomDrawersCompModel(state, effMatFront, effMatSide, effMatTrim, matFront, matSide, matTrim, false);
    }

    public static IBakedModel fromItem(@Nonnull ItemStack stack) {
        IBlockState state = ModBlocks.framedCompactDrawer.getStateFromMeta(stack.getMetadata());
        if(!stack.hasTagCompound())
            return new CustomDrawersCompModel(state, true);

        NBTTagCompound tag = stack.getTagCompound();
        ItemStack matFront = ItemStack.EMPTY;
        ItemStack matSide = ItemStack.EMPTY;
        ItemStack matTrim = ItemStack.EMPTY;

        assert tag != null;

        if(tag.hasKey("MatF", Constants.NBT.TAG_COMPOUND))
            matFront = new ItemStack(tag.getCompoundTag("MatF"));
        if(tag.hasKey("MatS", Constants.NBT.TAG_COMPOUND))
            matSide = new ItemStack(tag.getCompoundTag("MatS"));
        if(tag.hasKey("MatT", Constants.NBT.TAG_COMPOUND))
            matTrim = new ItemStack(tag.getCompoundTag("MatT"));

        ItemStack effMatFront = !matFront.isEmpty() ? matFront : matSide;
        ItemStack effMatTrim = !matTrim.isEmpty() ? matTrim : matSide;
        ItemStack effMatSide = matSide;

        IBakedModel model = new CustomDrawersCompModel(state, effMatFront, effMatSide, effMatTrim, matFront, matSide, matTrim, true);
        if(!stack.getTagCompound().hasKey("tile", Constants.NBT.TAG_COMPOUND))
            return model;

        return new DrawerSealedModel(model, state, true);
    }

    private CustomDrawersCompModel(IBlockState state, boolean mergeLayers) {
        this(state, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY,
                ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, mergeLayers);
    }

    private CustomDrawersCompModel(IBlockState state, @Nonnull ItemStack effMatFront, @Nonnull ItemStack effMatSide, @Nonnull ItemStack effMatTrim,
                                   @Nonnull ItemStack matFront, @Nonnull ItemStack matSide, @Nonnull ItemStack matTrim, boolean mergeLayers) {
        super(state, mergeLayers, effMatFront, effMatSide, effMatTrim, matFront, matSide, matTrim);
    }

    @Override
    protected void renderMippedLayer(ChamRender renderer, IBlockState state, Object... args) {
        EnumCompDrawer info = state.getValue(CustomDrawersComp.SLOTS);
        int index = iconIndex[info.getDrawerCount()];

        ItemStack itemFront = (ItemStack) args[0];
        ItemStack itemSide = (ItemStack) args[1];
        ItemStack itemTrim = (ItemStack) args[2];

        TextureAtlasSprite iconFront = !itemFront.isEmpty() ? IconUtil.getIconFromStack(itemFront) : null;
        TextureAtlasSprite iconSide = !itemSide.isEmpty() ? IconUtil.getIconFromStack(itemSide) : null;
        TextureAtlasSprite iconTrim = !itemTrim.isEmpty() ? IconUtil.getIconFromStack(itemTrim) : null;

        if(iconFront == null)
            iconFront = iconSide;
        if(iconTrim == null)
            iconTrim = iconSide;

        if(iconFront == null)
            iconFront = Chameleon.instance.iconRegistry.getIcon(Register.iconDefaultFront[index]);
        if(iconSide == null)
            iconSide = Chameleon.instance.iconRegistry.getIcon(Register.iconDefaultSide);
        if(iconTrim == null)
            iconTrim = Chameleon.instance.iconRegistry.getIcon(Register.iconDefaultSide);

        iconParticle = iconSide;

        ForkedDrawerRenderer drawerRenderer = new ForkedDrawerRenderer(renderer);
        drawerRenderer.renderBasePass(null, state, BlockPos.ORIGIN, state.getValue(BlockDrawers.FACING), iconSide, iconTrim, iconFront);
    }

    @Override
    protected void renderTransLayer(ChamRender renderer, IBlockState state, Object... args) {
        EnumCompDrawer info = state.getValue(CustomDrawersComp.SLOTS);
        int index = iconIndex[info.getDrawerCount()];

        TextureAtlasSprite iconOverlayFace = Chameleon.instance.iconRegistry.getIcon(Register.iconOverlayFace[index]);
        TextureAtlasSprite iconOverlayHandle = Chameleon.instance.iconRegistry.getIcon(Register.iconOverlayHandle[index]);

        ItemStack itemTrim = (ItemStack) args[5];

        TextureAtlasSprite iconTrim = !itemTrim.isEmpty() ? IconUtil.getIconFromStack(itemTrim) : null;
        TextureAtlasSprite iconOverlayTrim;

        if(iconTrim == null)
            iconOverlayTrim = Chameleon.instance.iconRegistry.getIcon(Register.iconOverlayBoldTrim[index]);
        else
            iconOverlayTrim = Chameleon.instance.iconRegistry.getIcon(Register.iconOverlayTrim[index]);

        ForkedDrawerRenderer drawerRenderer = new ForkedDrawerRenderer(renderer);
        drawerRenderer.renderOverlayPass(null, state, BlockPos.ORIGIN, state.getValue(BlockDrawers.FACING), iconOverlayTrim, iconOverlayHandle, iconOverlayFace);
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return iconParticle;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return null;
    }

    public static class Model extends ProxyBuilderModel {

        public Model() {
            super(Chameleon.instance.iconRegistry.getIcon(Register.iconDefaultSide));
        }

        @Override
        protected IBakedModel buildModel(IBlockState state, IBakedModel parent) {
            try {
                IBakedModel mainModel = CustomDrawersCompModel.fromBlock(state);
                if(!(state instanceof IExtendedBlockState))
                    return mainModel;

                IExtendedBlockState xstate = (IExtendedBlockState) state;
                DrawerStateModelData stateModel = xstate.getValue(BlockDrawers.STATE_MODEL);

                try {
                    if(!DrawerDecoratorModel.shouldHandleState(stateModel))
                        return mainModel;

                    EnumCompDrawer drawer = state.getValue(CustomDrawersComp.SLOTS);
                    EnumFacing dir = state.getValue(BlockDrawers.FACING);

                    DrawerDecoratorModel decModel = new DrawerDecoratorModel(mainModel, xstate, drawer, dir, stateModel);
                    decModel.addBaseRenderLayer(BlockRenderLayer.TRANSLUCENT);
                    return decModel;
                } catch(Throwable t) {
                    return mainModel;
                }
            } catch(Throwable t) {
                return parent;
            }
        }

        @Override
        public TextureAtlasSprite getParticleTexture() {
            return null;
        }

        @Override
        public ItemOverrideList getOverrides() {
            return itemHandler;
        }

        @Override
        public List<Object> getKey(IBlockState state) {
            try {
                List<Object> key = new ArrayList<>();
                IExtendedBlockState xstate = (IExtendedBlockState) state;
                key.add(xstate.getValue(BlockDrawers.STATE_MODEL));
                key.add(xstate.getValue(BlockDrawersCustom.MAT_MODEL));

                return key;
            } catch(Throwable t) {
                return super.getKey(state);
            }
        }

    }

    private static class ItemHandler extends ItemOverrideList {

        public ItemHandler() {
            super(ImmutableList.of());
        }

        @Override
        public IBakedModel handleItemState(IBakedModel originalModel, @Nonnull ItemStack stack, World world, EntityLivingBase entity) {
            return fromItem(stack);
        }

    }

    private static final ItemHandler itemHandler = new ItemHandler();

}
