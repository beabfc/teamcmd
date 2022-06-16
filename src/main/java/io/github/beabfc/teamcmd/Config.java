package io.github.beabfc.teamcmd;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;


public class Config {
    public String commandName = "t";
    public boolean allowDuplicateColors = true;
    public boolean allowDuplicateDisplaynames = false;
    public String prefixFormat = "[%.1s] ";
    public boolean prefixSecondaryColor = true;
    public String suffixFormat = "";
    public boolean suffixSecondaryColor = true;


    public static Config load(String configName) {
        try {
            File configFile = FabricLoader.getInstance().getConfigDir().resolve(configName).toFile();
            //noinspection ResultOfMethodCallIgnored
            configFile.createNewFile();
            Config config = new Toml().read(configFile).to(Config.class);
            new TomlWriter().write(config, configFile);
            return config;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
