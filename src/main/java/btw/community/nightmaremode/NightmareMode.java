package btw.community.nightmaremode;

import btw.AddonHandler;
import btw.BTWAddon;
import btw.block.BTWBlocks;
import btw.world.biome.BiomeDecoratorBase;
import net.minecraft.src.*;
import org.lwjgl.input.Keyboard;

import java.util.Arrays;
import java.util.Map;
import java.util.Random;

/**
 * Handles the addon instance and non-playtime stuff (eg.: world gen, settings default values)
 */
public class NightmareMode extends BTWAddon {
    /**
     * The NM instance
     */
    private static NightmareMode instance;

    /**
     * NM world gen utilities (eg.: lava pillows, silverfish)
     */
    public WorldGenerator lavaPillowGenThirdStrata;
    public WorldGenerator silverfishGenFirstStrata;
    public WorldGenerator silverfishGenSecondStrata;
    public WorldGenerator silverfishGenThirdStrata;

    /**
     * NM player utilities (eg.: keybids)
     */
    public static KeyBinding nightmareZoom;
    public static String nightmareZoomKey;
    public static Boolean shouldShowDateTimer;
    public static Boolean shouldShowRealTimer;

    /**
     * NM world utilities (eg.: bloodmoon)
     */
    public static Boolean bloodNightmare;
    public Boolean isBloodMoon;

    /**
     * NM special spawns
     */
    public static Boolean furnaceStart;

    /**
     * Constructor
     *
     * @return {NightmareMode} - The instance
     */
    public NightmareMode(){
        super();
    }

    /**
     * Gets the instance, creates one if not existent
     *
     * @return {NightmareMode} - The NM mod instance
     */
    public static NightmareMode getInstance() {
        if (instance == null) {
            instance = new NightmareMode();
        }

        return instance;
    }

    /**
     * Runs pre-initialization stuff (eg.: setting default values)
     * This happens before MC is ran
     *
     * @return {void}
     */
    @Override
    public void preInitialize() {
        this.registerProperty("NmMinecraftDayTimer", "True", "Set if the minecraft date should show up or not");
        this.registerProperty("NmTimer", "True", "Set if the real time timer should show up or not");
        this.registerProperty("NmZoomKey", "C", "The zoom keybind");
        this.registerProperty("BloodNightmare", "False", "...");
        this.registerProperty("FurnaceStart", "False", "Start with a furnace and half hunger");
    }

    /**
     * Parses the pre-inited properties onto the addon instance
     *
     * @param {Map<String, String>} propertyValues - The property values
     *
     * @return {void}
     */
    @Override
    public void handleConfigProperties(Map<String, String> propertyValues) {
        shouldShowDateTimer = Boolean.parseBoolean(propertyValues.get("NmMinecraftDayTimer"));
        shouldShowRealTimer = Boolean.parseBoolean(propertyValues.get("NmTimer"));
        bloodNightmare = Boolean.parseBoolean(propertyValues.get("BloodNightmare"));
        nightmareZoomKey = propertyValues.get("NmZoomKey");

        //TODO: validate multiple starts
        furnaceStart = Boolean.parseBoolean(propertyValues.get("FurnaceStart"));
    }

    /**
     * Inits the addon (eg.: world gen stuff)
     * This happens before MC is ran
     *
     * @return {void}
     */
    @Override
    public void initialize() {
        AddonHandler.logMessage(this.getName() + " Version " + this.getVersionString() + " Initializing...");

        this.lavaPillowGenThirdStrata = new WorldGenMinable(BTWBlocks.lavaPillow.blockID, 10);
        this.silverfishGenFirstStrata = new WorldGenMinable(BTWBlocks.infestedStone.blockID, 8);
        this.silverfishGenSecondStrata = new WorldGenMinable(BTWBlocks.infestedMidStrataStone.blockID, 8);
        this.silverfishGenThirdStrata = new WorldGenMinable(BTWBlocks.infestedDeepStrataStone.blockID, 16);
    }

    /**
     * Decorates the world with NM stuff (eg.: lava pillows, silverfish),
     * using the initialized decorators
     *
     * @param {BiomeDecoratorBase} decorator - The world decorator
     * @param {World} world - The world
     * @param {Random} rand - Random generator
     * @param {int} x - X coord
     * @param {int} z - Z coord
     * @param {BiomeGenBase} biome - The biome
     *
     * @return {void}
     */
    @Override
    public void decorateWorld(BiomeDecoratorBase decorator, World world, Random rand, int x, int z, BiomeGenBase biome) {
        for(int var5 = 0; var5 < 24; ++var5) {
            int var6 = x + rand.nextInt(16);
            int var7 = rand.nextInt(20)+5;
            int var8 = z + rand.nextInt(16);
            this.lavaPillowGenThirdStrata.generate(world, rand, var6, var7, var8);
        }
        for(int var5 = 0; var5 < 8; ++var5) {
            int var6 = x + rand.nextInt(16);
            int var7 = rand.nextInt(30)+50;
            int var8 = z + rand.nextInt(16);
            this.silverfishGenFirstStrata.generate(world, rand, var6, var7, var8);
        }
        for(int var5 = 0; var5 < 8; ++var5) {
            int var6 = x + rand.nextInt(16);
            int var7 = rand.nextInt(26)+24;
            int var8 = z + rand.nextInt(16);
            this.silverfishGenSecondStrata.generate(world, rand, var6, var7, var8);
        }
        for(int var5 = 0; var5 < 8; ++var5) {
            int var6 = x + rand.nextInt(16);
            int var7 = rand.nextInt(23)+1;
            int var8 = z + rand.nextInt(16);
            this.silverfishGenThirdStrata.generate(world, rand, var6, var7, var8);
        }
    }

    /**
     * Inits keybinds
     * TODO: figure out where it's called and why
     *
     * @return {void}
     */
    public void initKeybind(){
        nightmareZoom = new KeyBinding(StatCollector.translateToLocal("key.nightmaremode.zoom"), Keyboard.getKeyIndex(nightmareZoomKey));

        GameSettings settings = Minecraft.getMinecraft().gameSettings;
        KeyBinding[] keyBindings = settings.keyBindings;
        keyBindings = Arrays.copyOf(keyBindings, keyBindings.length + 1);
        keyBindings[keyBindings.length - 1] = nightmareZoom;
        settings.keyBindings = keyBindings;
    }

    /**
     * Sets the bloodmoon to false on the addon instance
     *
     * @return {void}
     */
    public static void setBloodMoonFalse(){
        if (instance != null) {
            instance.isBloodMoon = false;
        }
    }

    /**
     * Sets the bloodmoon to true on the addon instance
     *
     * @return {void}
     */
    public static void setBloodMoonTrue(){
        if (instance != null) {
            instance.isBloodMoon = true;
        }
    }
}
