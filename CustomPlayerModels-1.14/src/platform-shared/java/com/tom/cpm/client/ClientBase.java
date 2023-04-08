package com.tom.cpm.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.ElytraModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CCustomPayloadPacket;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.blaze3d.platform.GlStateManager;

import com.tom.cpl.text.FormatText;
import com.tom.cpm.CustomPlayerModels;
import com.tom.cpm.common.PlayerAnimUpdater;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.model.RenderManager;
import com.tom.cpm.shared.model.TextureSheetType;
import com.tom.cpm.shared.network.NetHandler;

import io.netty.buffer.Unpooled;

public class ClientBase {
	public static final ResourceLocation DEFAULT_CAPE = new ResourceLocation("cpm:textures/template/cape.png");
	public static final CustomPlayerModelsClient INSTANCE = new CustomPlayerModelsClient();
	public static MinecraftObject mc;
	public Minecraft minecraft;
	public RenderManager<GameProfile, PlayerEntity, Model, Void> manager;
	public NetHandler<ResourceLocation, PlayerEntity, ClientPlayNetHandler> netHandler;

	public void init0() {
		minecraft = Minecraft.getInstance();
		mc = new MinecraftObject(minecraft);
	}

	public void init1() {
		manager = new RenderManager<>(mc.getPlayerRenderManager(), mc.getDefinitionLoader(), PlayerEntity::getGameProfile);
		manager.setGPGetters(GameProfile::getProperties, Property::getValue);
		netHandler = new NetHandler<>(ResourceLocation::new);
		netHandler.setExecutor(() -> minecraft);
		netHandler.setSendPacketClient(d -> new PacketBuffer(Unpooled.wrappedBuffer(d)), (c, rl, pb) -> c.send(new CCustomPayloadPacket(rl, pb)));
		netHandler.setPlayerToLoader(PlayerEntity::getGameProfile);
		netHandler.setGetPlayerById(id -> {
			Entity ent = Minecraft.getInstance().level.getEntity(id);
			if(ent instanceof PlayerEntity) {
				return (PlayerEntity) ent;
			}
			return null;
		});
		netHandler.setGetClient(() -> minecraft.player);
		netHandler.setGetNet(c -> ((ClientPlayerEntity)c).connection);
		netHandler.setDisplayText(f -> minecraft.player.displayClientMessage(f.remap(), false));
		netHandler.setGetPlayerAnimGetters(new PlayerAnimUpdater());
	}

	public static void apiInit() {
		CustomPlayerModels.api.buildClient().voicePlayer(PlayerEntity.class, PlayerEntity::getUUID).
		renderApi(Model.class, GameProfile.class).
		localModelApi(GameProfile::new).init();
	}

	public void playerRenderPre(PlayerEntity player, PlayerModel model) {
		manager.bindPlayer(player, null, model);
		manager.bindSkin(model, TextureSheetType.SKIN);
	}

	public void playerRenderPost(PlayerModel model) {
		manager.unbindClear(model);
	}

	public void renderHand(PlayerModel model) {
		manager.bindHand(Minecraft.getInstance().player, null, model);
		manager.bindSkin(model, TextureSheetType.SKIN);
	}

	public void renderHandPost(BipedModel model) {
		CustomPlayerModelsClient.INSTANCE.manager.unbindClear(model);
	}

	public void renderSkull(Model skullModel, GameProfile profile) {
		manager.bindSkull(profile, null, skullModel);
		manager.bindSkin(skullModel, TextureSheetType.SKIN);
	}

	public void renderSkullPost(Model model) {
		CustomPlayerModelsClient.INSTANCE.manager.unbindFlush(model);
	}

	public void renderElytra(BipedModel<LivingEntity> player, ElytraModel<LivingEntity> model) {
		manager.bindElytra(player, model);
		manager.bindSkin(model, TextureSheetType.ELYTRA);
	}

	public void renderArmor(BipedModel<LivingEntity> modelArmor, BipedModel<LivingEntity> modelLeggings,
			BipedModel<LivingEntity> player) {
		manager.bindArmor(player, modelArmor, 1);
		manager.bindArmor(player, modelLeggings, 2);
		manager.bindSkin(modelArmor, TextureSheetType.ARMOR1);
		manager.bindSkin(modelLeggings, TextureSheetType.ARMOR2);
	}

	public void updateJump() {
		if(minecraft.player.onGround && minecraft.player.input.jumping) {
			manager.jump(minecraft.player);
		}
	}

	//Copy from CapeLayer
	public static void renderCape(AbstractClientPlayerEntity playerIn, float partialTicks, PlayerModel<AbstractClientPlayerEntity> model,
			ModelDefinition modelDefinition) {
		GlStateManager.pushMatrix();

		float f1, f2, f3;

		if(playerIn != null) {
			double d0 = MathHelper.lerp(partialTicks, playerIn.xCloakO,
					playerIn.xCloak)
					- MathHelper.lerp(partialTicks, playerIn.xo, playerIn.x);
			double d1 = MathHelper.lerp(partialTicks, playerIn.yCloakO,
					playerIn.yCloak)
					- MathHelper.lerp(partialTicks, playerIn.yo, playerIn.y);
			double d2 = MathHelper.lerp(partialTicks, playerIn.zCloakO,
					playerIn.zCloak)
					- MathHelper.lerp(partialTicks, playerIn.zo, playerIn.z);
			float f = playerIn.yBodyRotO
					+ (playerIn.yBodyRot - playerIn.yBodyRotO);
			double d3 = MathHelper.sin(f * 0.017453292F);
			double d4 = (-MathHelper.cos(f * 0.017453292F));
			f1 = (float) d1 * 10.0F;
			f1 = MathHelper.clamp(f1, -6.0F, 32.0F);
			f2 = (float) (d0 * d3 + d2 * d4) * 100.0F;
			f2 = MathHelper.clamp(f2, 0.0F, 150.0F);
			f3 = (float) (d0 * d4 - d2 * d3) * 100.0F;
			f3 = MathHelper.clamp(f3, -20.0F, 20.0F);
			if (f2 < 0.0F) {
				f2 = 0.0F;
			}

			float f4 = MathHelper.lerp(partialTicks, playerIn.oBob, playerIn.bob);
			f1 += MathHelper.sin(MathHelper.lerp(partialTicks, playerIn.walkDistO,
					playerIn.walkDist) * 6.0F) * 32.0F * f4;
			if (playerIn.isVisuallySneaking()) {
				f1 += 25.0F;
			}
			if (playerIn.getItemBySlot(EquipmentSlotType.CHEST).isEmpty()) {
				if (playerIn.isVisuallySneaking()) {
					model.cloak.z = 1.4F + 0.125F * 3;
					model.cloak.y = 1.85F + 1 - 0.125F * 4;
				} else {
					model.cloak.z = 0.0F + 0.125F * 16f;
					model.cloak.y = 0.0F;
				}
			} else if (playerIn.isVisuallySneaking()) {
				model.cloak.z = 0.3F + 0.125F * 16f;
				model.cloak.y = 0.8F + 0.3f;
			} else {
				model.cloak.z = -1.1F + 0.125F * 32f;
				model.cloak.y = -0.85F + 1;
			}
		} else {
			f1 = 0;
			f2 = 0;
			f3 = 0;
		}
		model.cloak.xRot = (float) -Math.toRadians(6.0F + f2 / 2.0F + f1);
		model.cloak.yRot = (float) Math.toRadians(180.0F - f3 / 2.0F);
		model.cloak.zRot = (float) Math.toRadians(f3 / 2.0F);
		mc.getPlayerRenderManager().setModelPose(model);
		model.cloak.xRot = 0;
		model.cloak.yRot = 0;
		model.cloak.zRot = 0;
		model.renderCloak(0.0625F);
		GlStateManager.popMatrix();
	}

	public static interface PlayerNameTagRenderer<E extends Entity> {
		void cpm$renderNameTag(E entityIn, String displayNameIn, double x, double y, double z, double i);
		EntityRendererManager cpm$entityRenderDispatcher();
	}

	public static <E extends Entity> void renderNameTag(PlayerNameTagRenderer<E> r, E entityIn, GameProfile gprofile, String unique, double x, double y, double z, double d0) {
		if (d0 < 32*32) {
			FormatText st = INSTANCE.manager.getStatus(gprofile, unique);
			if(st != null) {
				GlStateManager.pushMatrix();
				GlStateManager.translatef(0.0F, 1.3F, 0.0F);
				GlStateManager.scalef(0.5f, 0.5f, 0.5f);
				r.cpm$renderNameTag(entityIn, st.<ITextComponent>remap().getColoredString(), x, y, z, d0);
				GlStateManager.popMatrix();
			}
		}
	}
}
