package com.lita;
public class LtaPluginHelper {
    public static final String    pluginMainName = "WildyAgilityLootTracker";
    public static final String pluginMainPkgName = "com.lita";
    public static final String pluginMainClsName = pluginMainName.concat("Plugin");
    public static final String       displayName = "Wildy Agility Loot Tracker";
    public static final String        configName = "wildyagilityloottracker";
    public static final String         authorRSN = "Karol920";
    public static final String      authorGithub = "Mr920";
    public static final String    authorFullName = "Charles Lentz";
    public static final String            author;
    public static final String supportBugReports = "";
    public static final String     supportReadme = "";
    public static final String    supportUrlMain = "";
    public static final String       description = "Keeps track of loot received at wildy agility course, on a per-session basis";
    public static final String             TAG_1 = "wilderness";
    public static final String             TAG_2 = "wildy";
    public static final String             TAG_3 = "agility";
    public static final String             TAG_4 = "loot";
    public static final String             TAG_5 = "tracker";
    public static final String[]            tags = { TAG_1, TAG_2, TAG_3, TAG_4, TAG_5 };
    public static final String[]       conflicts = {};
    public static final String           version = "2.0.0";
    public static final String           plugins = pluginMainPkgName + "." + pluginMainClsName;
    public static final String             build = "standard";
    public static final boolean enabledByDefault = true;
    public static final boolean           hidden = false;
    public static final boolean  developerPlugin = false;
    public static final boolean   loadInSafeMode = false;
    static {
        author = String.format("RSN:%s/GitHub:%s/Name:%s", authorRSN, authorGithub, authorFullName);
    }


}
/*
@Slf4j
@PluginDescriptor(
                name = LtaPluginHelper.pluginMainName,
          configName = LtaPluginHelper.configName,
         description = LtaPluginHelper.description,
                tags = {},
           conflicts = {},
    enabledByDefault = true,
              hidden = false,
     developerPlugin = false,
      loadInSafeMode = false
)
 */
