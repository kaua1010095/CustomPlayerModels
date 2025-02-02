package com.tom.cpm;

import java.io.File;
import java.util.EnumSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;

import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms.IMCEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;

import com.tom.cpl.config.ModConfigFile;
import com.tom.cpl.text.TextRemapper;
import com.tom.cpl.text.TextStyle;
import com.tom.cpl.util.ILogger;
import com.tom.cpm.api.CPMApiManager;
import com.tom.cpm.api.ICPMPlugin;
import com.tom.cpm.common.Command;
import com.tom.cpm.common.ServerHandler;
import com.tom.cpm.shared.MinecraftCommonAccess;
import com.tom.cpm.shared.MinecraftObjectHolder;
import com.tom.cpm.shared.PlatformFeature;
import com.tom.cpm.shared.config.ModConfig;
import com.tom.cpm.shared.util.IVersionCheck;
import com.tom.cpm.shared.util.VersionCheck;

@Mod(modid = CustomPlayerModels.ID, acceptableRemoteVersions = "*",
updateJSON = MinecraftObjectHolder.VERSION_CHECK_URL,
guiFactory = "com.tom.cpm.Config")
public class CustomPlayerModels implements MinecraftCommonAccess {
	public static final String ID = "customplayermodels";

	@SidedProxy(clientSide = "com.tom.cpm.client.ClientProxy", serverSide = "com.tom.cpm.CommonProxy")
	public static CommonProxy proxy;

	public static final Logger LOG = LogManager.getLogger("CPM");
	public static final ILogger log = new Log4JLogger(LOG);
	public static CPMApiManager api;

	@EventHandler
	public void init(FMLInitializationEvent evt) {
		api = new CPMApiManager();
		proxy.init();
		MinecraftForge.EVENT_BUS.register(new ServerHandler());
		LOG.info("Customizable Player Models Initialized");
	}

	@EventHandler
	public void processIMC(IMCEvent event) {
		event.getMessages().forEach(m -> {
			try {
				if(m.key.equals("api")) {
					ICPMPlugin plugin = (ICPMPlugin) Class.forName(m.getStringValue()).newInstance();
					api.register(plugin);
				}
			} catch (Throwable e) {
				LOG.error("Mod {} provides a broken implementation of CPM api", m.getSender(), e);
			}
		});
		LOG.info("Customizable Player Models IMC processed: " + api.getPluginStatus());
		api.buildCommon().player(EntityPlayer.class).init();
		proxy.apiInit();
	}

	@EventHandler
	public void serverStart(FMLServerStartingEvent evt) {
		MinecraftObjectHolder.setServerObject(new MinecraftServerObject(evt.getServer()));
		new Command(evt::registerServerCommand);
	}

	@EventHandler
	public void serverStop(FMLServerStoppingEvent evt) {
		ModConfig.getWorldConfig().save();
		MinecraftObjectHolder.setServerObject(null);
	}

	private ModConfigFile cfg;

	@EventHandler
	public void preInit(FMLPreInitializationEvent evt) {
		cfg = new ModConfigFile(new File(evt.getModConfigurationDirectory(), "cpm.json"));
		MinecraftObjectHolder.setCommonObject(this);
	}

	@Override
	public ModConfigFile getConfig() {
		return cfg;
	}

	@Override
	public ILogger getLogger() {
		return log;
	}

	private static final EnumSet<PlatformFeature> features = EnumSet.noneOf(PlatformFeature.class);

	@Override
	public EnumSet<PlatformFeature> getSupportedFeatures() {
		return features;
	}

	@Override
	public String getMCVersion() {
		return "1.8.9";
	}

	@Override
	public String getMCBrand() {
		return "(" + FMLCommonHandler.instance().getModName() + ")";
	}

	@Override
	public String getModVersion() {
		return Loader.instance().getIndexedModList().get(CustomPlayerModels.ID).getDisplayVersion();
	}

	@Override
	public TextRemapper<IChatComponent> getTextRemapper() {
		return new TextRemapper<>(ChatComponentTranslation::new, ChatComponentText::new, IChatComponent::appendSibling, null,
				CustomPlayerModels::styleText);
	}

	private static IChatComponent styleText(IChatComponent in, TextStyle style) {
		ChatStyle s = new ChatStyle();
		s.setBold(style.bold);
		s.setItalic(style.italic);
		s.setUnderlined(style.underline);
		s.setStrikethrough(style.strikethrough);
		return in.setChatStyle(s);
	}

	@Override
	public CPMApiManager getApi() {
		return api;
	}

	@Override
	public IVersionCheck getVersionCheck() {
		return VersionCheck.get(() -> ForgeVersion.getResult(Loader.instance().getIndexedModList().get(CustomPlayerModels.ID)).changes);
	}
}
